package activity.main;

import activity.calculations.ActivityStats;
import activity.calculations.SegmentLeaderboard;
import activity.calculations.Statistics;
import activity.misc.GPXData;
import activity.misc.Pair;
import activity.mapreduce.Reduce;
import activity.parser.Chunk;
import activity.parser.GPXParser;
import activity.parser.Route;
import activity.parser.Segment;

import java.io.*;
import java.net.Socket;
import java.util.*;

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

    // This is where the client will be handled
    public void run()
    {
        Thread readFromClient = new Thread(this::readFromClient);
        Thread readFromWorkerHandler = new Thread(this::readFromWorkerHandler);

        readFromClient.start();
        readFromWorkerHandler.start();
    }

    // This method is used to receive back all the chunks from the file we dispatched to the worker
    // In order to start the reduce phase for that route (and thus, the client)
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
                Pair<Chunk, ActivityStats> stats = statsQueue.poll();
                Chunk chunk = stats.getKey();
                int routeID = chunk.getRoute().getRouteID();

                synchronized (routeHashmap)
                {
                    // If the route already exists in the hashmap, we add the chunk to the list of chunks
                    if (routeHashmap.containsKey(routeID))
                    {
                        ArrayList<Pair<Chunk, ActivityStats>> activityList = routeHashmap.get(routeID);
                        // If the chunks we received are more than what we expected, we throw an exception
                        if (activityList.size() >= chunk.getTotalChunks())
                        {
                            throw new RuntimeException("Found more chunks than expected!");
                        }
                        // Else, if we have accumulated all the chunks we need, we can start reducing
                        else if (activityList.size() == (chunk.getTotalChunks() - 1))
                        {
                            // We add the last chunk to the list
                            activityList.add(stats);
                            // We add the list to the hashmap for the specific route
                            routeHashmap.put(routeID, activityList);
                            // and then we start the reducing phase
                            processChunks(chunk, activityList);
                        }
                        // Else, we just add the chunk to the list
                        else
                        {
                            activityList.add(stats);
                            routeHashmap.put(routeID, activityList);
                        }

                    // Else if the route does not exist in the hashmap, we create a new list and add the chunk to it
                    }
                    else
                    {
                        ArrayList<Pair<Chunk, ActivityStats>> activityList = new ArrayList<>();
                        activityList.add(stats);
                        routeHashmap.put(routeID, activityList);
                    }
                }
            }
        }
    }

    // This method is used to get the file from the client
    // And to send it to the work-dispatcher that will dispatch it to the workers.
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

                if (obj instanceof GPXData gpxData)
                {
                    ByteArrayInputStream gpxContent = new ByteArrayInputStream(gpxData.getFileContent());
                    // Parse the file
                    // Create a new thread to handle the parsing of the file
                    Route route = GPXParser.parseRoute(gpxContent,segments);
                    route.setClientID(clientID);
                    // Add the route to the queue
                    synchronized (routeQueue)
                    {
                        // Add the route to the queue and notify the dispatcher
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

    private void processChunks(Chunk chunk, ArrayList<Pair<Chunk, ActivityStats>> activityList)
    {
        int routeID = chunk.getRoute().getRouteID();
        // fetching a list of all the stats we gathered for this specific route
        ArrayList<ActivityStats> statsArrayList = new ArrayList<>();
        for (Pair<Chunk, ActivityStats> pair : activityList)
        {
            statsArrayList.add(pair.getValue());
        }
        // Creating a new thread to handle the reducing phase
        new Thread(() -> handleReducing(new Pair<>(routeID, statsArrayList), chunk.getRoute().getUser())).start();
    }

    // This is the method that will handle the reducing phase and send the result back to the client
    // Parameters: The integer of the pair represents the id of the route, and the arraylist of activity stats represents all the intermediary chunks
    private void handleReducing(Pair<Integer, ArrayList<ActivityStats>> intermediateResults, String user)
    {
        // finalResults: The reduce process returns the final ActivityStats associated with a specific route.
        ActivityStats finalResults = Reduce.reduce(intermediateResults);
        try
        {
            // Send the result back to the client
            synchronized (writeLock)
            {
                statistics.registerRoute(user, finalResults);
                out.writeObject(finalResults);
                out.flush();
                out.writeObject(statistics.getUserStats(user));
                out.flush();
                out.writeObject(statistics.getGlobalStats());
                out.flush();

                ArrayList<Integer> segmentsInRoute = finalResults.getSegmentIDs();
                ArrayList<SegmentLeaderboard> segmentLeaderboards = new ArrayList<>();

                for (int segmentID : segmentsInRoute)
                {
                    segmentLeaderboards.add(statistics.getLeaderboard(segmentID));
                }

                out.writeObject(segmentLeaderboards);
                out.flush();

                // Here we reset the output stream to make sure
                // that the object is sent with all the changes we made
                out.reset();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not send object to the client");
            System.out.println(e.getMessage());
        }
    }

    // addStats: Adds the stats to the queue to be processed by the readFromWorkerHandler method
    public void addStats(Pair<Chunk, ActivityStats> stats)
    {
        synchronized (statsQueue)
        {
            statsQueue.add(stats);
            statsQueue.notify();
        }
    }

    // This method will close the connection to the client
    // and clean up the resources
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