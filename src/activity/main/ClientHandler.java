package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.GPXParser;
import activity.parser.Route;
import activity.parser.Waypoint;

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
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // This is the unique ID of the client so we can
    private String clientID;
    GPXParser parser;
    // This is the queue that the routes will be added to
    private Queue<Route> routes;
    private Queue<ActivityStats> statsQueue;
    private HashMap<Integer,Integer> routeidToChunkCount;

    public ClientHandler(Socket clientSocket , Queue<Route> routes)
    {
        this.clientSocket = clientSocket;
        try
        {
            this.clientID = UUID.randomUUID().toString();
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());

            this.parser = new GPXParser();

            this.routes = routes;
            this.statsQueue = new LinkedList<>();
            this.routeidToChunkCount = new HashMap();
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
        Thread readFromClient = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                readFromClient();
            }

        });

        Thread readFromWorkerHandler = new Thread(new Runnable() {
            @Override
            public void run() {
                readFromWorkerHandler();
            }
        });

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
                ActivityStats stats = statsQueue.poll();

                if (stats.isFlag())
                {
                    System.err.println("ClientHandler: " + clientID + " received the final chunk");
                    System.err.println("The number of chunks received for route " + stats.getRouteID() + " is " + routeidToChunkCount.get(stats.getRouteID()));
                }
                else
                {   // TODO: Remove this after testing
                    // For each route id increment the number of times it has been received
                    if (routeidToChunkCount.containsKey(stats.getRouteID())) {
                        routeidToChunkCount.put(stats.getRouteID(), routeidToChunkCount.get(stats.getRouteID()) + 1);
                    } else {
                        routeidToChunkCount.put(stats.getRouteID(), 1);
                    }
                }
            }
        }
    }

    // addStats: Adds the stats to the queue
    public void addStats(ActivityStats stats)
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
                    Route route = parser.parse(receivedFile);
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
        catch  (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public String getClientID()
    {
        return clientID;
    }



}