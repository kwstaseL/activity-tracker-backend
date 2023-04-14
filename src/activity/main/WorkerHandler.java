package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
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
    private Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults;

    public WorkerHandler(Socket workerSocket,HashMap<Integer,ClientHandler> clients)
    {
        this.workerSocket = workerSocket;
        // Add the worker to the queue
        try
        {
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
    public void run()
    {
        Thread listenToWorker = new Thread(this::listenToWorker);

        Thread handleResults = new Thread(() -> resultDispatcher.handleResults());

        listenToWorker.start();
        handleResults.start();
    }

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
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("WorkerHandler: Connection to worker lost");
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }


    public void processJob(Chunk chunk)
    {
        try
        {
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