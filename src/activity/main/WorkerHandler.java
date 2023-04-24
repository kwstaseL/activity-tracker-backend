package activity.main;

import activity.calculations.ActivityStats;
import activity.misc.Pair;
import activity.parser.Chunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

// This class will handle the worker connection
public class WorkerHandler implements Runnable
{
    // These are the input and output streams for the worker
    private ObjectInputStream in;
    private ObjectOutputStream out;
    // This is the socket that the worker is connected to
    private final Socket workerSocket;
    private ResultDispatcher resultDispatcher;
    // This is the queue that will hold the intermediate results from the workers
    // The results will be sent to the appropriate client-handler
    private Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults;

    public WorkerHandler(Socket workerSocket,HashMap<Integer,ClientHandler> clients)
    {
        this.workerSocket = workerSocket;
        // Add the worker to the queue
        try
        {
            // Creating the input and output streams for the worker
            this.in = new ObjectInputStream(workerSocket.getInputStream());
            this.out = new ObjectOutputStream(workerSocket.getOutputStream());
            this.intermediateResults = new LinkedList<>();

            // initialising a ResultDispatcher with the clients matched to their unique IDs
            this.resultDispatcher = new ResultDispatcher(clients,intermediateResults);
        }
        catch (IOException e)
        {
            System.out.println("Could not create input and output streams");
            System.out.println("Error: " + e.getMessage());
        }
    }

    // This is where the worker will be handled
    // A thread will be created to listen for messages from the worker
    // and a thread will be created to handle the results from the worker
    public void run()
    {
        Thread listenToWorker = new Thread(this::listenToWorker);
        Thread handleResults = new Thread(() -> resultDispatcher.handleResults());

        listenToWorker.start();
        handleResults.start();
    }
    // This method will listen for messages from the worker
    // and add the intermediate results to the queue for the ResultDispatcher to handle
    @SuppressWarnings("unchecked")
    private void listenToWorker()
    {
        try
        {
            while (!workerSocket.isClosed())
            {
                // Receive message from worker
                Object receivedObject = in.readObject();
                Pair<Integer, Pair<Chunk, ActivityStats>> stats = (Pair<Integer, Pair<Chunk, ActivityStats>>) receivedObject;

                synchronized (intermediateResults)
                {
                    intermediateResults.add(stats);
                    intermediateResults.notify();
                }
            }

        } catch (IOException | ClassNotFoundException e)
        {
            System.out.println("WorkerHandler: Connection to worker lost");
            e.printStackTrace();
        } finally
        {
            shutdown();
        }
    }
    // This method will send a chunk to the worker to be mapped
    public void processJob(Chunk chunk)
    {
        try
        {
            // Send the chunk to the worker to be mapped
            out.writeObject(chunk);
            out.flush();
        }
        catch (IOException e)
        {
            System.out.println("WorkerHandler: Could not send route to worker");
            e.printStackTrace();
        }
    }
    // This method will close the connection to the worker
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
            if (workerSocket != null)
            {
                workerSocket.close();
            }

            System.out.println("Worker disconnected");
        }
        catch (IOException e)
        {
            System.out.println("Could not close connection to worker");
            throw new RuntimeException(e);
        }

    }
}