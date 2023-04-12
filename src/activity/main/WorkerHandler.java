package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.Route;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

// This class will handle the worker connection
public class WorkerHandler implements Runnable
{
    // These are the input and output streams for the worker
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // This is the socket that the worker is connected to
    private final Socket workerSocket;
    private final Object lock = new Object();

    private static HashMap<Integer,Integer> chunksPerRoute;

    private HashMap<String,ClientHandler> clients;

    public WorkerHandler(Socket workerSocket,HashMap<String,ClientHandler> clients)
    {
        this.workerSocket = workerSocket;
        // Add the worker to the queue
        try
        {
            this.clients = clients;
            this.in = new ObjectInputStream(workerSocket.getInputStream());
            this.out = new ObjectOutputStream(workerSocket.getOutputStream());
            chunksPerRoute = new HashMap<>();
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

        listenToWorker.start();
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
                System.out.println("WorkerHandler: Received intermediate results from worker: " + stats);

                // TODO: Get the intermediate results from the worker and send them to the client handler
                handleRequests(stats);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("WorkerHandler: Connection to worker lost");
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    // handleRequests: Receives the <K, V> pair the worker generates, and sends it to the appropriate ClientHandler
    private void handleRequests(Pair<String, ActivityStats> activityStatsPair)
    {
        // Send the result back to the client-handler
        synchronized (lock)
        {
            String clientID = activityStatsPair.getKey();
            ClientHandler appropriateHandler = clients.get(clientID);
            appropriateHandler.addStats(activityStatsPair.getValue());
        }
    }

    public void processJob(Route route)
    {
        try
        {
            Integer routeID = route.getRouteID();
            int count = chunksPerRoute.getOrDefault(routeID,0);
            chunksPerRoute.put(routeID,count+1);
            System.out.println("WORKERHANDLER: Route id : " + routeID + " count : " + chunksPerRoute.get(routeID));
            // Send the route to the worker
            System.out.println("WorkerHandler: Sending route to worker: " + route);
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