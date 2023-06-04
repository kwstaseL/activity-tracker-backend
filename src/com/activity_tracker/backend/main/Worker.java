package com.activity_tracker.backend.main;



import com.activity_tracker.backend.calculations.ActivityStats;
import com.activity_tracker.backend.mapreduce.Map;
import com.activity_tracker.backend.misc.Pair;
import com.activity_tracker.backend.parser.Chunk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * The worker class is responsible for handling the mapping of the data.
 */
public class Worker
{
    // This is the socket that the worker is connected to
    private Socket connection;
    // Input stream for the worker to receive objects from the master
    private ObjectInputStream in;
    // Output stream for the worker to send objects to the master
    private ObjectOutputStream out;
    // This is the lock that will be used to ensure that only one thread can write to the output stream at a time
    private final Object writeLock = new Object();

    /*
     * Initializes a new instance of the Worker class.
     */
    public Worker()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            final String masterIP = config.getProperty("master_ip");
            final int workerPort = Integer.parseInt(config.getProperty("worker_port"));
            // Creating a socket that will connect to the master and creating the input and output streams
            connection = new Socket(masterIP, workerPort);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            System.out.println("Worker: Connection to master established");
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            shutdown();
            System.out.println("Error: " + e.getMessage());
        }

    }

    /**
     * Starts the worker and begins listening for jobs from the master.
     */
    private void start()
    {
        Thread readData = new Thread(this::readForData);
        readData.start();
    }

    /**
     * Reads data from the master and handles the mapping of the data.
     * For each chunk that is received, a new thread will be created to handle the mapping of the data.
     * @throws RuntimeException if the received object is not a chunk.
     */
    private void readForData()
    {
        while (!connection.isClosed())
        {
            try
            {
                Object receivedObject = in.readObject();
                System.out.println("Worker: Received object from master");
                Chunk chunk;

                // If the received object is a chunk, a new thread will be created to handle the mapping of the data
                if (receivedObject instanceof Chunk)
                {
                    chunk = (Chunk) receivedObject;
                    new Thread(() -> handleMapping(chunk)).start();
                }
                else
                {
                    throw new RuntimeException("Received unknown object.");
                }

            }
            catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Could not receive object");
                shutdown();
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the mapping of the data.
     * Calls the map method from the Map class and sends the result back to the master.
     *
     * @param chunk The chunk of data to be mapped.
     */
    private void handleMapping(Chunk chunk)
    {
        if (chunk == null)
        {
            throw new RuntimeException("The chunk appears null.");
        }

        // intermediateResult: the mapping process returns a key-value pair,
        // where key is the client id, and the value is another pair of chunk, activityStats
        Pair<Integer, Pair<Chunk, ActivityStats>> intermediateResult = Map.map(chunk.getRoute().getClientID(), chunk);
        try
        {
            // Send the result back to the worker-handler
            synchronized (writeLock)
            {
                out.writeObject(intermediateResult);
                out.flush();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not send object to the worker handler");
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the connection to the master and closes the input and output streams.
     */
    private void shutdown()
    {
        try
        {
            in.close();
        }
        catch (IOException e)
        {
            System.out.println("Could not close input stream");
            System.out.println("Error: " + e.getMessage());
        }
        try
        {
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("Could not close output stream");
            System.out.println("Error: " + e.getMessage());
        }
        try
        {
            connection.close();
        }
        catch (IOException e)
        {
            System.out.println("Could not close connection");
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        Worker worker = new Worker();
        worker.start();
    }

}