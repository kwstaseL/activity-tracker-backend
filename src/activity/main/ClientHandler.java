package activity.main;

import activity.calculations.ActivityStats;
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

    // totalActivityStats: Includes all the results from all the route calculations
    private static ArrayList<ActivityStats> totalActivityStats = new ArrayList<>();     // TODO: Possibly no longer necessary?

    // userActivityStats: Links all users to the list of routes recorded by them
    private static HashMap<String, ArrayList<ActivityStats>> userActivityStats = new HashMap<>();

    // routeHashmap: Matches the route IDs with a list of the chunks they contain
    private static HashMap<Integer, ArrayList<Pair<Chunk, ActivityStats>>> routeHashmap = new HashMap<>();

    /* totalDistance/Elevation/ActivityTime per user: A hashmap matching each user (as a String) with the sum of all
     * their respective stats. Used in order to divide by the number of a user's recorded routes to fetch the average      */
    private static HashMap<String, Double> totalDistancePerUser = new HashMap<>();
    private static HashMap<String, Double> totalElevationPerUser = new HashMap<>();
    private static HashMap<String, Double> totalActivityTimePerUser = new HashMap<>();

    // routesRecorded: A counter for how many routes we have recorded in total in our app.
    private static int routesRecorded = 0;

    // totalAverageDistance/Elevation/ActivityTime: Likewise, for recording the sum of all the respective stats across all users.
    private static double totalDistance = 0.0;
    private static double totalElevation = 0.0;
    private static double totalActivityTime = 0.0;
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
                    if (routeHashmap.containsKey(routeID))
                    {
                        ArrayList<Pair<Chunk, ActivityStats>> activityList = routeHashmap.get(routeID);
                        if (activityList.size() >= chunk.getTotalChunks())
                        {
                            throw new RuntimeException("Found more chunks than expected!");
                        }
                        else if (activityList.size() == chunk.getTotalChunks() -1)
                        {
                            activityList.add(stats);
                            routeHashmap.put(routeID, activityList);
                            ++routesRecorded;

                            // TODO: Cleanup

                            // fetching a list of all the stats we gathered for this specific route
                            ArrayList<ActivityStats> statsArrayList = new ArrayList<>();
                            for (Pair<Chunk, ActivityStats> pair : activityList) {
                                statsArrayList.add(pair.getValue());
                            }

                            new Thread(() -> handleReducing(new Pair<>(routeID, statsArrayList), chunk.getRoute().getUser())).start();

                        } else
                        {
                            activityList.add(stats);
                            routeHashmap.put(routeID, activityList);
                        }
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
                updateStats(finalResults, user);
                out.writeObject(finalResults);
                out.flush();
            }


        } catch (IOException e)
        {
            System.out.println("Could not send object to the client");
            throw new RuntimeException(e);
        }
    }

    // updateStats: Called when receiving new stats for a specific user. Updates all ClientHandler stats-related data structures and member variables accordingly
    private void updateStats(ActivityStats stats, String user)
    {
        ArrayList<ActivityStats> userStats;
        double distance = stats.getDistance();
        double elevation = stats.getElevation();
        double time = stats.getTime();

        if (userActivityStats.containsKey(user)) {
            userStats = userActivityStats.get(user);
            totalDistancePerUser.put(user, totalDistancePerUser.get(user) + distance);
            totalElevationPerUser.put(user, totalElevationPerUser.get(user) + elevation);
            totalActivityTimePerUser.put(user, totalActivityTimePerUser.get(user) + time);
        } else {
            userStats = new ArrayList<>();
            totalDistancePerUser.put(user, distance);
            totalElevationPerUser.put(user, elevation);
            totalActivityTimePerUser.put(user, time);
        }
        userStats.add(stats);
        userActivityStats.put(user, userStats);
        totalActivityStats.add(stats);
        
        totalDistance += distance;
        totalElevation += elevation;
        totalActivityTime += time;
    }

    // getAverageDistanceForUser: Calculates the average distance for a user by dividing their total distance with the # of routes they have recorded
    public double getAverageDistanceForUser(String user)
    {
        if (!userActivityStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        double totalDistanceForUser = totalDistancePerUser.get(user);
        return totalDistanceForUser / userActivityStats.get(user).size();
    }

    // getAverageElevationForUser: Similarly to getAverageDistanceForUser
    public double getAverageElevationForUser(String user)
    {
        if (!userActivityStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        double totalElevationForUser = totalElevationPerUser.get(user);
        return totalElevationForUser / userActivityStats.get(user).size();
    }

    // getAverageActivityTimeForUser: Similarly to getAverageDistanceForUser
    public double getAverageActivityTimeForUser(String user)
    {
        if (!userActivityStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        double totalActivityTimeForUser = totalActivityTimePerUser.get(user);
        return totalActivityTimeForUser / userActivityStats.get(user).size();
    }

    // getAverageDistance: Calculates the average distance recorded across all routes.
    public double getAverageDistance()
    {
        return totalDistance / routesRecorded;
    }

    // getAverageElevation: Similarly to getAverageDistance
    public double getAverageElevation()
    {
        return totalElevation / routesRecorded;
    }

    // getAverageDistance: Similarly to getAverageDistance
    public double getAverageActivityTime()
    {
        return totalActivityTime / routesRecorded;
    }


    // addStats: Adds the stats to the queue
    public void addStats(Pair<Chunk, ActivityStats> stats)
    {
        synchronized (statsQueue)
        {
            statsQueue.add(stats);
            statsQueue.notify();
        }
    }
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