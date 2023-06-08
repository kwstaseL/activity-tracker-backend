package com.activity_tracker.backend.main;

import com.activity_tracker.backend.calculations.ActivityStats;
import com.activity_tracker.backend.calculations.SegmentLeaderboard;
import com.activity_tracker.backend.calculations.Statistics;
import com.activity_tracker.backend.mapreduce.Reduce;
import com.activity_tracker.backend.misc.GPXData;
import com.activity_tracker.backend.misc.Pair;
import com.activity_tracker.backend.parser.Chunk;
import com.activity_tracker.backend.parser.GPXParser;
import com.activity_tracker.backend.parser.Route;
import com.activity_tracker.backend.parser.Segment;

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
    private static final Object writeLock = new Object();
    private static final Object routeHashmapLock = new Object();
    private static final Statistics statistics = new Statistics();

    /**
     * Constructor for the ClientHandler
     * @param clientSocket the socket that the client is connected to
     * @param routeQueue the queue that the routes will be added to and that the work dispatcher will take from
     * @param segments the queue containing all the segments Master holds, to check for intersections with users' routes.
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
     * This method is used to get the file from the client
     * And to send it to the work-dispatcher that will dispatch it to the workers.
     *
     * @throws RuntimeException if the user is already connected
     */
    private void readFromClient(){
        try{
            String username = (String) in.readObject(); // Receive the username from the client

            while (!clientSocket.isClosed())
            {
                Object object = in.readObject(); // Receive the service from the client

                if (object instanceof GPXData)
                {
                    GPXData gpxData = (GPXData) object;
                    ByteArrayInputStream gpxContent = new ByteArrayInputStream(gpxData.getFileContent());
                    Route route = GPXParser.parseRoute(gpxContent, segments);
                    route.setClientID(clientID);
                    synchronized (routeQueue)
                    {
                        routeQueue.add(route);
                        routeQueue.notify();
                    }
                }
                else if (object instanceof String)
                {
                    String service = (String) object;

                    if (service.equalsIgnoreCase("LEADERBOARD"))
                    {
                        // Handle the leaderboard request
                        ArrayList<SegmentLeaderboard> leaderboards = statistics.getSegmentLeaderboardsForUser(username);
                        if (leaderboards == null)
                        {
                            out.writeObject("NO LEADERBOARDS");
                            out.flush();
                        }
                        else
                        {
                            out.writeObject(leaderboards);
                            out.flush();
                        }

                    }
                    else if (service.equalsIgnoreCase("STATISTICS"))
                    {
                        // Handle the statistics request
                        out.writeObject(statistics.getUserStats(username));
                        out.flush();

                    }
                    else
                    {

                    }
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            System.out.println("Client disconnected");
        }
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

                Pair<Chunk, ActivityStats> stats = statsQueue.poll();
                Chunk chunk = stats.getKey();
                int routeID = chunk.getRoute().getRouteID();

                synchronized (routeHashmapLock)
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

    /**
     * Process the given chunk by collecting all the intermediate statistics and
     * starting a new thread to handle the reducing phase.
     *
     * @param chunk The chunk to process
     * @param activityList The list of pairs of chunks and their activity statistics
     */
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

    /**
     * Handle the reducing phase for the given intermediate results and send the
     * final result back to the client.
     *
     * @param intermediateResults The pair of route ID and intermediate activity statistics
     * @param user The user associated with the route
     */
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

                ArrayList<Integer> segmentsInRoute = finalResults.getSegmentHashes();
                ArrayList<SegmentLeaderboard> segmentLeaderboards = new ArrayList<>();

                for (int segmentHash : segmentsInRoute)
                {
                    segmentLeaderboards.add(statistics.getLeaderboard(segmentHash));
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

    /**
     * Called by the worker handler. Adds a pair of chunk and activity statistics to the queue
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