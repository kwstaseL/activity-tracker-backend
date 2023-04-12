package activity.main;

import activity.calculations.ActivityStats;
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
    private void listenToWorker()
    {
        try
        {
            while (!workerSocket.isClosed())
            {
                // Receive message from worker
                System.out.println("WorkerHandler: Waiting for message from worker");
                Object receivedObject = in.readObject();
                HashMap<String, ActivityStats> stats = (HashMap<String, ActivityStats>) receivedObject;
                System.out.println("WorkerHandler: Received intermediate results from worker: " + stats);
                // TODO: Get the intermediate results from the worker and send them to the client handler

                // Receives the intermediate results from the worker and finds who the client handler of the client is and
                // sends the intermediate results to the client handler
                /*
                HashMap<String, ActivityStats> stats = (HashMap<String, ActivityStats>) receivedObject;

                // find the client that sent the route
                // extract the key from the stats hashmap
                String clientID = stats.keySet().iterator().next();
                // get the client handler from the clients hashmap
                ClientHandler clientHandler = clients.get(clientID);
                // send the intermediate results to the client handler
                clientHandler.sendIntermediateResults(stats);
                 */

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