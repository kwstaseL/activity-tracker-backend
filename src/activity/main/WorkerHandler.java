package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.Route;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

// This class will handle the worker connection
public class WorkerHandler implements Runnable
{
    // These are the input and output streams for the worker
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // This is the socket that the worker is connected to
    private final Socket workerSocket;
    private final Object lock = new Object();
    private HashMap<String,ClientHandler> clients;
    private ResultDispatcher resultDispatcher;
    private HashMap<Integer,Boolean> routeStatus;
    private Queue<Pair<String,ActivityStats>> intermediateResults;

    private static HashMap<Integer,Integer> chunksPerRoute;


    public WorkerHandler(Socket workerSocket,HashMap<String,ClientHandler> clients,HashMap<Integer,Boolean> routeStatus)
    {
        this.workerSocket = workerSocket;
        // Add the worker to the queue
        try
        {
            this.clients = clients;
            this.in = new ObjectInputStream(workerSocket.getInputStream());
            this.out = new ObjectOutputStream(workerSocket.getOutputStream());
            this.routeStatus = routeStatus;
            this.chunksPerRoute = new HashMap<>();
            this.intermediateResults = new LinkedList<>();
            this.resultDispatcher = new ResultDispatcher(clients,routeStatus,intermediateResults,chunksPerRoute);
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
        Thread listenToWorker = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                listenToWorker();
            }
        });

        Thread handleResults = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                resultDispatcher.handleResults();
            }
        });

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
                System.out.println("WorkerHandler: Waiting for message from worker");
                Object receivedObject = in.readObject();
                Pair<String, ActivityStats> stats = (Pair<String, ActivityStats>) receivedObject;


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


    public void processJob(Route route)
    {
        try
        {
            synchronized (chunksPerRoute)
            {
                Integer routeID = route.getRouteID();
                if (chunksPerRoute.containsKey(routeID)) {
                    int count = chunksPerRoute.get(routeID);
                    count++;
                    chunksPerRoute.put(routeID, count);
                } else {
                    chunksPerRoute.put(routeID, 1);
                }
            }
            out.writeObject(route);
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