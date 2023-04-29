package activity.main;

import activity.calculations.ActivityStats;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

// This class will handle the client connection
public class ClientHandler implements Runnable
{
    // This is the socket that the client is connected to
    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // This is the queue that the routes will be added to
    private Queue<Route> routeQueue;

    // routes: Represents all routes received
    private static final ArrayList<Route> routes = new ArrayList<>();
    // statsQueue: the queue that will contain all the activity stats calculated from each chunk respectively
    private final Queue<Pair<Chunk, ActivityStats>> statsQueue = new LinkedList<>();
    private static final Statistics statistics = new Statistics();

    // The unique id of the client, generated through a static id generator
    private int clientID;
    private static int clientIDGenerator = 0;

    // The directories where we keep unprocessed and processed gpx files
    private String unprocessedDirectory;
    private String processedDirectory;
    // segments: a queue containing all the segments to be checked for intersections with the routes of users.
    private Queue<Segment> segments;

    // routeHashmap: Matches the route IDs with the list of the chunks they contain
    private static final HashMap<Integer, ArrayList<Pair<Chunk, ActivityStats>>> routeHashmap = new HashMap<>();
    private final Object writeLock = new Object();

    public ClientHandler(Socket clientSocket , Queue<Route> routes, String unprocessedDirectory, String processedDirectory,Queue<Segment> segments)
    {
        this.clientSocket = clientSocket;
        try
        {
            this.clientID = clientIDGenerator++;
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.unprocessedDirectory = unprocessedDirectory;
            this.processedDirectory = processedDirectory;
            this.routeQueue = routes;
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
                        else if (activityList.size() == (chunk.getTotalChunks() -1))
                        {
                            activityList.add(stats);
                            routeHashmap.put(routeID, activityList);

                            // fetching a list of all the stats we gathered for this specific route
                            ArrayList<ActivityStats> statsArrayList = new ArrayList<>();
                            for (Pair<Chunk, ActivityStats> pair : activityList)
                            {
                                statsArrayList.add(pair.getValue());
                            }
                            // Creating a new thread to handle the reducing phase
                            new Thread(() -> handleReducing(new Pair<>(routeID, statsArrayList), chunk.getRoute().getUser())).start();

                            // Finds the path of the file we want to move and the path of the destination
                            // And then moves the already processed file from the unprocessed directory to the processed directory
                            //TODO: This file should not be moved, but just added to the processed directory of the master server
                            // Because the master, does not about the available gpx's that reside in the user's directory
                            /*
                            System.out.println("Moving file: " + chunk.getRoute().getFileName());
                            Path sourcePath = Paths.get(unprocessedDirectory + File.separator + chunk.getRoute().getFileName());
                            Path destPath = Paths.get(processedDirectory + File.separator + chunk.getRoute().getFileName());
                            try
                            {
                                Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                            catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                            */

                        // Else, we just add the chunk to the list
                        }
                        else
                        {
                            activityList.add(stats);
                            routeHashmap.put(routeID, activityList);
                        }

                    // Else, we create a new entry in the hashmap for this route
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

    // TODO: Implement the else condition from the method above as a separate method
    private void registerStats()
    {

    }

    // This method is used to get the file from the client
    // And to send it to the work-dispatcher that will dispatch it to the workers.
    private void readFromClient()
    {
        try
        {
            while (!clientSocket.isClosed())
            {
                // Receive the file object from the client
                System.out.println("ClientHandler: Waiting for file from client");

                Object obj = in.readObject();

                if (obj instanceof GPXData)
                {
                    GPXData gpxData = (GPXData) obj;
                    ByteArrayInputStream gpxContent = new ByteArrayInputStream(gpxData.getFileContent());
                    // Parse the file
                    // Create a new thread to handle the parsing of the file
                    Route route = GPXParser.parseRoute(gpxContent,segments);
                    route.setClientID(clientID);
                    // Add the route to the queue
                    synchronized (routeQueue)
                    {
                        // Add the route to the queue and notify the dispatcher
                        routes.add(route);
                        routeQueue.add(route);
                        routeQueue.notify();
                    }
                }
            }

        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("ClientHandler: Connection to client lost");
        }
        finally
        {
            shutdown();
        }
    }

    // This is the method that will handle the reducing phase and send the result back to the client
    // Parameters: The integer of the pair represents the id of the route, and the arraylist of activity stats represents all the intermediary chunks
    private void handleReducing(Pair<Integer, ArrayList<ActivityStats>> intermediateResults, String user)
    {
        System.out.println("ClientHandler: " + "Route: " + intermediateResults.getKey() + " is about to be reduced with " + intermediateResults.getValue().size() + " chunks");

        // finalResults: The reduce process returns the final ActivityStats associated with a specific route.
        ActivityStats finalResults = Reduce.reduce(intermediateResults);
        try
        {
            // Send the result back to the worker-handler
            synchronized (writeLock)
            {
                statistics.registerRoute(user, finalResults);
                out.writeObject(finalResults);
                out.flush();
                out.writeObject(statistics.getUserStats(user));
                out.flush();
                out.writeObject(statistics.getGlobalStats());
                out.flush();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not send object to the client");
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
        try
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
            if (clientSocket != null)
            {
                clientSocket.close();
            }
            System.out.println("Client disconnected");
        }
        catch (IOException e)
        {
            System.out.println("Client disconnected");
        }
    }

    public int getClientID()
    {
        return clientID;
    }

}