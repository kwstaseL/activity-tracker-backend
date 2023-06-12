package com.activity_tracker.backend.main;

import com.activity_tracker.backend.parser.GPXParser;
import com.activity_tracker.backend.parser.Route;
import com.activity_tracker.backend.parser.Segment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

public class Master
{
    // This is the socket that the client will connect to
    private ServerSocket clientSocket;
    // This is the socket that the worker will connect to
    private ServerSocket workerSocket;
    // Queue containing the routes that will be sent to the workers
    private Queue<Route> routes;
    // Queue containing the segments
    private Queue<Segment> segments;
    // Queue containing all the worker handlers
    private Queue<WorkerHandler> workerHandlers;
    // Lookup table that will map the client id to the appropriate client handler
    private HashMap<Integer,ClientHandler> clientMap;
    private int maxWorkers;

    // Master will be a singleton class, with instance marking the only Master instantiation we will be allowing.
    private static Master instance = null;

    // Creates a new instance of Master, and loads the configuration file
    private Master()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            final int WORKER_PORT = Integer.parseInt(config.getProperty("worker_port"));
            final int CLIENT_PORT = Integer.parseInt(config.getProperty("client_port"));

            maxWorkers = Integer.parseInt(config.getProperty("number_of_workers"));

            clientSocket = new ServerSocket(CLIENT_PORT);
            workerSocket = new ServerSocket(WORKER_PORT);
            workerHandlers = new LinkedList<>();
            clientMap = new HashMap<>();
            routes = new LinkedList<>();
            segments = new LinkedList<>();
        }
        catch (Exception e)
        {
            System.out.println("Could not create sockets");
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     *  This method will start the master server and will create two threads:
     *  1. Thread that will accept client connections and create a new thread to handle the client
     *  2. Thread that will accept worker connections and create a new thread to handle the worker
     * and some additional "helper" threads.
     */
    private void start()
    {
        /*
          Thread that will accept client connections and create a new thread to handle the client
          by creating a new ClientHandler object.
          We also pass shared resources to the ClientHandler for processing reasons.
         */
        Thread handleClient = new Thread(() ->
        {
            while (!clientSocket.isClosed())
            {
                try
                {
                    System.out.println("MASTER: Waiting for client connection");
                    // Accept a client connection
                    Socket client = clientSocket.accept();
                    System.out.println("MASTER: Client connected");
                    // Create a new thread to handle the client
                    ClientHandler clientHandler = new ClientHandler(client, routes, segments);

                    // Add the client handler to the lookup table
                    int clientID = clientHandler.getClientID();
                    clientMap.put(clientID, clientHandler);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();

                }
                catch (Exception e)
                {
                    System.out.println("MASTER: Could not accept client connection");
                    try
                    {
                        clientSocket.close();
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("MASTER: Client connection closed");
                }
            }
        });

        /*
           Thread that will accept worker connections and create a new thread to handle the worker
           by creating a new WorkerHandler object.
           We also pass the clientMap to the WorkerHandler so that the worker can send the results back to the corresponding
           component that handles the client.
         */
        Thread handleWorker = new Thread(() ->
        {
            while (workerHandlers.size() != maxWorkers)
            {
                try
                {
                    System.out.println("MASTER: Waiting for worker connection");
                    // Accept a worker connection
                    Socket worker = workerSocket.accept();
                    System.out.println("MASTER: Worker connected");

                    // Create a new thread to handle the worker. Also, passing the client map
                    // so that the worker can send the results to the appropriate client
                    WorkerHandler workerHandler = new WorkerHandler(worker, clientMap);
                    workerHandlers.add(workerHandler);
                    Thread workerThread = new Thread(workerHandler);
                    workerThread.start();

                }
                catch (Exception e)
                {
                    System.out.println("MASTER: Could not accept worker connection");
                    e.printStackTrace();
                    try
                    {
                        workerSocket.close();

                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("MASTER: Worker connection closed");
                    System.out.println("MASTER: Error: " + e.getMessage());
                }
            }
        });

        /*
          Thread that will start dispatching work to the workers.
          We are passing the worker handler so that the work dispatcher can send work to the workers.
          We are also passing the routes, which is a shared memory between the client handler and the work dispatcher.
          The client-handler will upload the routes to the work dispatcher and the work dispatcher will send the routes to the workers.
         */
        Thread dispatchWork = new Thread(() ->
        {
            WorkDispatcher workDispatcher = new WorkDispatcher(workerHandlers, routes);
            Thread workDispatcherThread = new Thread(workDispatcher);
            workDispatcherThread.start();
        });

        /*
          Thread that will create the segments from the files in the directory of the master.
         */
        Thread createSegments = new Thread(() ->
        {
            // Get the segment files from the directory.
            Properties config = new Properties();
            try
            {
                config.load(new FileInputStream("config.properties"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            String segmentDirectory = config.getProperty("segment_directory");

            // Create the segments
            File directory = new File(segmentDirectory);
            File[] files = directory.listFiles();

            if (files == null)
            {
                throw new RuntimeException("Could not find the directory.");
            }

            for (File file : files)
            {
                Segment segment = GPXParser.parseSegment(file);
                segments.add(segment);
            }
        });

        createSegments.start();
        try
        {
            createSegments.join();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        handleWorker.start();
        handleClient.start();
        dispatchWork.start();
    }

    /**
     * getInstance: The method used to get the only Master instance that will be running in our app.
     * @return The singleton Master instance.
     */
    public synchronized static Master getInstance()
    {
        if (instance == null)
        {
            instance = new Master();
        }
        return instance;
    }

    public static void main(String[] args)
    {
        Master master = Master.getInstance();
        master.start();
    }
}