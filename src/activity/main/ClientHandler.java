package activity.main;

import activity.calculations.ActivityStats;
import activity.calculations.Statistics;
import activity.calculations.UserStatistics;
import activity.mapreduce.Pair;
import activity.mapreduce.Reduce;
import activity.parser.Chunk;
import activity.parser.GPXParser;
import activity.parser.Route;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

// This class will handle the client connection
public class ClientHandler implements Runnable
{

    // This is the socket that the client is connected to
    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // The unique id of the client, generated through a static id generator
    private int clientID;
    private static int clientIDGenerator = 0;

    // This is the queue that the routes will be added to
    private Queue<Route> routes;

    // statsQueue: the queue that will contain all the activity stats calculated from each chunk respectively
    private Queue<Pair<Chunk, ActivityStats>> statsQueue;

    //
    private static Statistics statistics = new Statistics();

    // routeHashmap: Matches the route IDs with the list of the chunks they contain
    private static HashMap<Integer, ArrayList<Pair<Chunk, ActivityStats>>> routeHashmap = new HashMap<>();
    private final Object writeLock = new Object();

    public ClientHandler(Socket clientSocket , Queue<Route> routes)
    {
        this.clientSocket = clientSocket;
        try
        {
            this.clientID = clientIDGenerator++;
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());

            this.routes = routes;
            this.statsQueue = new LinkedList<>();
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
                            // TODO: Cleanup

                            // fetching a list of all the stats we gathered for this specific route
                            ArrayList<ActivityStats> statsArrayList = new ArrayList<>();
                            for (Pair<Chunk, ActivityStats> pair : activityList)
                            {
                                statsArrayList.add(pair.getValue());
                            }
                            // Creating a new thread to handle the reducing phase
                            new Thread(() -> handleReducing(new Pair<>(routeID, statsArrayList), chunk.getRoute().getUser())).start();
                        // Else, we just add the chunk to the list
                        } else
                        {
                            activityList.add(stats);
                            routeHashmap.put(routeID, activityList);
                        }
                    // Else, we create a new entry in the hashmap for this route
                    } else
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
            while (!clientSocket.isClosed())
            {
                // Receive the file object from the client
                System.out.println("ClientHandler: Waiting for file from client");
                Object receivedObject = in.readObject();

                if (receivedObject instanceof File receivedFile)
                {
                    System.out.println("ClientHandler: Received file from client  " + receivedFile.getName());
                    // Dispatching the file to the workers
                    // Parse the file
                    Route route = GPXParser.parse(receivedFile);
                    route.setClientID(clientID);
                    // Add the route to the queue
                    synchronized (routes)
                    {
                        routes.add(route);
                        routes.notify();
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e)
        {
            System.out.println("ClientHandler: Connection to client lost");
            e.printStackTrace();
        }
        finally
        {
            shutdown();
        }
    }
    // This is the method that will handle the reducing phase and send the result back to the client
    private void handleReducing(Pair<Integer, ArrayList<ActivityStats>> intermediateResults, String user)
    {
        System.out.println("ClientHandler: " + "Route: " + intermediateResults.getKey() + " is about to be reduced with " + intermediateResults.getValue().size() + " chunks");

        // TODO: Cleanup?
        // intermediate_result: the mapping process returns a key-value pair, where key is the client id, and the value is another pair of chunk, activityStats
        ActivityStats finalResults = Reduce.reduce(intermediateResults);
        try
        {
            // Send the result back to the worker-handler
            synchronized (writeLock)
            {
                statistics.registerRoute(user, finalResults);
                out.writeObject(finalResults);
                out.flush();
            }


        } catch (IOException e)
        {
            System.out.println("Could not send object to the client");
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }

    }

    public int getClientID()
    {
        return clientID;
    }
    
}