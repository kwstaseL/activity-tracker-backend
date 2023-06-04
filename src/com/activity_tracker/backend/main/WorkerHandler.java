package com.activity_tracker.backend.main;

import com.activity_tracker.backend.calculations.ActivityStats;
import com.activity_tracker.backend.misc.Pair;
import com.activity_tracker.backend.parser.Chunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * This class is responsible to handle the connection to a worker node.
 */
public class WorkerHandler implements Runnable
{
    // These are the input and output streams for the worker
    private ObjectInputStream in;
    private ObjectOutputStream out;
    // This is the socket that the worker is connected to
    private final Socket workerSocket;
    private HashMap<Integer,ClientHandler> clients;

    public WorkerHandler(Socket workerSocket, HashMap<Integer, ClientHandler> clients)
    {
        this.workerSocket = workerSocket;
        // Add the worker to the queue
        try
        {
            // Creating the input and output streams for the worker
            this.in = new ObjectInputStream(workerSocket.getInputStream());
            this.out = new ObjectOutputStream(workerSocket.getOutputStream());
            this.clients = clients;
        }
        catch (IOException e)
        {
            System.out.println("Could not create input and output streams");
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Starts the thread to listen for messages from the worker.
     */
    @Override
    public void run()
    {
        Thread listenToWorker = new Thread(this::listenToWorker);
        listenToWorker.start();
    }

    /**
     * Listens for messages from the worker and adds the intermediate results
     * to the appropriate client handler.
     */
    @SuppressWarnings("unchecked")
    private void listenToWorker()
    {
        try
        {
            while (!workerSocket.isClosed())
            {
                // Receive the results from the worker
                Object receivedObject = in.readObject();

                if (receivedObject instanceof Pair<?,?>)
                {
                    Pair<Integer, Pair<Chunk, ActivityStats>> activityStatsPair = (Pair<Integer, Pair<Chunk, ActivityStats>>) receivedObject;

                    // extracting the pair associated with the client
                    Pair<Chunk, ActivityStats> stats = activityStatsPair.getValue();

                    ClientHandler appropriateHandler = clients.get(activityStatsPair.getKey());
                    // sending the results to the appropriate client by writing them to shared memory
                    appropriateHandler.addStats(stats);
                }
                else
                {
                    throw new ClassNotFoundException("Received object is not of type Pair");
                }
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("WorkerHandler: Connection to worker lost");
        }
        finally
        {
            shutdown();
        }
    }

    /**
     * Sends a chunk to the worker to be processed.
     */
    public void processJob(Chunk chunk)
    {
        if (chunk == null)
        {
            throw new RuntimeException("The chunk appears null.");
        }

        try
        {
            // Send the chunk to the worker to be mapped
            out.writeObject(chunk);
            out.flush();
        }
        catch (IOException e)
        {
            System.out.println("WorkerHandler: Could not send route to worker because the connection is lost");
        }
    }
    /**
     * Closes the connection to the worker and cleans up resources.
     */
    private void shutdown()
    {
        try
        {
            in.close();
        }
        catch (IOException e)
        {
            System.out.println("WorkerHandler: Could not close input stream");
        }
        try
        {
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("WorkerHandler: Could not close output stream");
        }
        try
        {
            workerSocket.close();
        }
        catch (IOException e)
        {
            System.out.println("WorkerHandler: Could not close socket");
        }
        System.out.println("WorkerHandler: Worker disconnected.");
    }
}