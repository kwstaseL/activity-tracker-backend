package activity.main;

import activity.calculations.ActivityStats;
import activity.calculations.SegmentLeaderboard;
import activity.calculations.Statistics;
import activity.mapreduce.Reduce;
import activity.misc.GPXData;
import activity.misc.Pair;
import activity.parser.Chunk;
import activity.parser.GPXParser;
import activity.parser.Route;
import activity.parser.Segment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

// This class will handle the client connection
public class ClientHandler implements Runnable
{
    // This is the socket that the client is connected to
    private final Socket clientSocket;
    // This is the input which will be used to receive objects from the client
    private ObjectInputStream in;
    // This is the output which will be used to send objects to the client
    private ObjectOutputStream out;
    // The unique id of the client, generated through a static id generator
    private int clientID;
    // Used to generate the clientIDs
    private static int clientIDGenerator = 0;
    // The username of the client that is connected
    private String clientUsername;
    // This is the queue that the routes will be added to and the worker dispatcher will take from
    private Queue<Route> routeQueue;
    // segments: a queue containing all the segments to be checked for intersections with the routes of users.
    private Queue<Segment> segments;
    // statsQueue: the queue that will contain all the activity stats calculated from each chunk respectively
    private final Queue<Pair<Chunk, ActivityStats>> statsQueue = new LinkedList<>();
    // routeHashmap: Matches the route IDs with the list of the chunks they contain
    private static final HashMap<Integer, ArrayList<Pair<Chunk, ActivityStats>>> routeHashmap = new HashMap<>();
    // connectedClients: A list of all the connected clients
    private static final ArrayList<String> connectedClients = new ArrayList<>();
    private final Object writeLock = new Object();
    private static final Statistics statistics = new Statistics();

    /**
     * Constructor for the ClientHandler
     * @param clientSocket the socket that the client is connected to
     * @param routeQueue the queue that the routes will be added to and the worker dispatcher will take from
     * @param segments a queue containing all the segments to be checked for intersections with the routes of users.
     */
    public ClientHandler(Socket clientSocket, Queue<Route> routeQueue, Queue<Segment> segments)
    {
        this.clientSocket = clientSocket;
        try
        {
            this.clientID = clientIDGenerator++;
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.routeQueue = routeQueue;
            this.segments = segments;
        }
        catch (IOException e)
        {
            System.out.println("Could not create input and output streams");
            shutdown();
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Method used to start the threads that will handle messages from the client and the worker handlers
     */
    public void run()
    {
        Thread readFromClient = new Thread(this::readFromClient);
        Thread readFromWorkerHandler = new Thread(this::readFromWorkerHandler);

        readFromClient.start();
        readFromWorkerHandler.start();
    }

    /**
     * This method is used to receive back all the chunks from the file we dispatched to the worker.
     * In order to start the reduce phase for that route (and thus, the client).
     *
     * @throws RuntimeException if the chunks received are more than expected
     */
    private void readFromWorkerHandler()
    {
        synchronized (statsQueue)
        {
            while (true)
            {
                while (statsQueue.isEmpty())
                {
                    try
                    {
                        statsQueue.wait();
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                // We get the chunk and the activity stats from the queue
                Pair<Chunk, ActivityStats> stats = statsQueue.poll();
                Chunk chunk = stats.getKey();
                int routeID = chunk.getRoute().getRouteID();

                System.out.println();

                synchronized (routeHashmap)
                {
                    ArrayList<Pair<Chunk, ActivityStats>> activityList;

                    /* if the route already exists in the hashmap, fetch the list of the results for the chunks this
                     * route contains. Else, create a new list for this route's chunks                         */
                    if (routeHashmap.containsKey(routeID))
                    {
                        activityList = routeHashmap.get(routeID);
                        // If the chunks received are more than expected, throw an exception
                        if (activityList.size() >= chunk.getTotalChunks())
                        {
                            throw new RuntimeException("Found more chunks than expected!");
                        }
                    }
                    else
                    {
                        // If the route is not in the hashmap, create a new list for it
                        activityList = new ArrayList<>();
                    }
                    activityList.add(stats);
                    routeHashmap.put(routeID, activityList);

                    // If we have received all the chunks for this route
                    if (activityList.size() == (chunk.getTotalChunks() - 1) || (activityList.size() == 1 && chunk.getTotalChunks() == 1))
                    {
                        // we start the reducing phase
                        processChunks(chunk, activityList);
                    }
                }
            }
        }
    }

    /**
     * This method is used to get the file from the client
     * And to send it to the work-dispatcher that will dispatch it to the workers.
     */
    private void readFromClient()
    {
        try
        {
            // Get the clients username
            String username = (String) in.readObject();
            // Check if the user is already connected
            if (connectedClients.contains(username))
            {
                // If the user is already connected, we send a message to the client and close the connection
                System.out.println("ClientHandler: User already connected!");
                out.writeObject("User already connected!");
                out.flush();
                throw new RuntimeException("User already connected!");
            }
            else
            {
                System.out.println("ClientHandler: User " + username + " connected!");
                out.writeObject("OK");
                connectedClients.add(username);
                clientUsername = username;
            }
            // Receive the file object from the client
            while (!clientSocket.isClosed())
            {
                System.out.println("ClientHandler: Waiting for file from client");
                Object obj = in.readObject();

                if (obj instanceof GPXData)
                {
                    GPXData gpxData = (GPXData) obj;
                    // Create a new input stream from the file content
                    ByteArrayInputStream gpxContent = new ByteArrayInputStream(gpxData.getFileContent());
                    // Parse the file
                    Route route = GPXParser.parseRoute(gpxContent,segments);
                    route.setClientID(clientID);
                    // Add the route to the queue
                    //noinspection SynchronizeOnNonFinalField
                    synchronized (routeQueue)
                    {
                        // Add the route to the queue and notify the dispatcher that will dispatch it to the workers
                        routeQueue.add(route);
                        routeQueue.notify();
                    }
                }
            }

        }
        catch (Exception e)
        {
            System.out.println("ClientHandler: Connection to client lost");
        }
        finally
        {
            shutdown();
        }
    }

    /**
     * Process the given chunk by collecting all the intermediate statistics and
     * starting a new thread to handle the reducing phase.
     *
     * @param chunk The chunk to process
     * @param activityList The list of pairs of chunks and their activity statistics
     */
    private void processChunks(Chunk chunk, ArrayList<Pair<Chunk, ActivityStats>> activityList)
    {
        int routeId = chunk.getRoute().getRouteID();

        // Collect all the activity statistics for this specific route
        ArrayList<ActivityStats> statsArrayList = new ArrayList<>();
        for (Pair<Chunk, ActivityStats> pair : activityList)
        {
            statsArrayList.add(pair.getValue());
        }

        // TODO: Check whats going on here
        // Start a new thread to handle the reducing phase
        new Thread(() -> handleReducing(new Pair<>(routeId, statsArrayList), chunk.getRoute().getUser())).start();
    }

    /**
     * Handle the reducing phase for the given intermediate results and send the
     * final result back to the client.
     *
     * @param intermediateResults The pair of route ID and intermediate activity statistics
     * @param user The user associated with the route
     */
    private void handleReducing(Pair<Integer, ArrayList<ActivityStats>> intermediateResults, String user)
    {
        // Reduce the intermediate activity statistics to get the final result
        ActivityStats finalResults = Reduce.reduce(intermediateResults);

        try
        {
            synchronized (writeLock)
            {
                // Register the route with the final activity statistics
                statistics.registerRoute(user, finalResults);

                // Send the final activity statistics to the client
                out.writeObject(finalResults);
                out.flush();

                // Send the user statistics to the client
                out.writeObject(statistics.getUserStats(user));
                out.flush();

                // Send the global statistics to the client
                out.writeObject(statistics.getGlobalStats());
                out.flush();

                // Get the segment leaderboards for each segment in the route
                ArrayList<Integer> segmentsInRoute = finalResults.getSegmentHashes();
                ArrayList<SegmentLeaderboard> segmentLeaderboards = new ArrayList<>();

                for (int segmentHashID : segmentsInRoute)
                {
                    segmentLeaderboards.add(statistics.getLeaderboard(segmentHashID));
                }

                // Send the segment leaderboards to the client
                out.writeObject(segmentLeaderboards);
                out.flush();

                // Reset the output stream to make sure all changes are sent
                out.reset();
            }

        } catch (IOException e)
        {
            // Log any errors that occur during sending of data to the client
            System.out.println("Error sending data to the client: " + e.getMessage());
        }
    }


    /**
     * Called by the worker handler  to add a pair of chunk and activity statistics to the queue
     * @param stats The pair of chunk and activity statistics to add to the queue
     */
    public void addStats(Pair<Chunk, ActivityStats> stats)
    {
        synchronized (statsQueue)
        {
            statsQueue.add(stats);
            statsQueue.notify();
        }
    }

    /**
     * Method that will be called when the client disconnects.
     * It will close all the streams and the socket,
     * and it will also save the statistics for the client.
     */
    private void shutdown()
    {
        if (clientUsername != null)
        {
            System.out.println("ClientHandler: Saving statistics for user " + clientUsername);
            synchronized (statistics)
            {
                statistics.createFile();
            }
            // Remove the client from the list of connected clients
            connectedClients.remove(clientUsername);
        }
        try
        {
            in.close();
        }
        catch (IOException e)
        {
            System.out.println("ClientHandler: Could not close input stream");
        }
        try
        {
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("ClientHandler: Could not close output stream");
        }
        try
        {
            clientSocket.close();
        }
        catch (IOException e)
        {
            System.out.println("ClientHandler: Could not close client socket");
        }
        System.out.println("ClientHandler: Client disconnected");
    }

    public int getClientID()
    {
        return clientID;
    }

}