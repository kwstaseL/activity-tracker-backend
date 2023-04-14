package activity.main;

import activity.parser.Route;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Master
{
    // This will be the port that the client will connect to
    public static final int CLIENT_PORT = 4445;
    // This will be the port that the worker will connect to
    public static final int WORKER_PORT = 4444;
    // This is the socket that the client will connect to
    private ServerSocket clientSocket;
    // This is the socket that the worker will connect to
    private ServerSocket workerSocket;
    // Queue containing the routes that will be sent to the workers
    private Queue<Route> routes;
    // Queue containing all the worker handlers
    private Queue<WorkerHandler> workerHandlers;
    // Lookup table that will map the client id to the appropriate client handler
    private HashMap<Integer,ClientHandler> clientMap;
    private int numOfWorkers;

    public Master(int numOfWorkers)
    {
        try
        {
            this.numOfWorkers = numOfWorkers;
            clientSocket = new ServerSocket(CLIENT_PORT);
            workerSocket = new ServerSocket(WORKER_PORT);
            workerHandlers = new LinkedList<>();
            clientMap = new HashMap<>();
            routes = new LinkedList<>();
        }
        catch (Exception e)
        {
            System.out.println("Could not create sockets");
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void start()
    {
        // Thread that will create the workers
        Thread init = new Thread(() ->
        {
            for (int i = 0; i < numOfWorkers; i++)
            {
                Worker worker = new Worker();
                worker.start();
            }
        });
        // Thread that will handle the clients
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
                    ClientHandler clientHandler = new ClientHandler(client,routes);
                    int clientID = clientHandler.getClientID();
                    clientMap.put(clientID,clientHandler);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();

                } catch (Exception e)
                {
                    System.out.println("Could not accept client connection");
                    try
                    {

                        clientSocket.close();

                    } catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("Client connection closed");
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });
        // Thread that will handle the workers
        Thread handleWorker = new Thread(() ->
        {
            while (!workerSocket.isClosed())
            {
                try
                {
                    System.out.println("MASTER: Waiting for worker connection");
                    // Accept a worker connection
                    Socket worker = workerSocket.accept();
                    System.out.println("MASTER: Worker connected");
                    // Create a new thread to handle the worker
                    WorkerHandler workerHandler = new WorkerHandler(worker,clientMap);
                    workerHandlers.add(workerHandler);
                    Thread workerThread = new Thread(workerHandler);
                    workerThread.start();

                } catch (Exception e)
                {
                    System.out.println("Could not accept worker connection");
                    e.printStackTrace();
                    try
                    {

                        workerSocket.close();

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("Worker connection closed");
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });
        // Thread that will start dispatching work to the workers
        Thread dispatchWork = new Thread(() ->
        {
            WorkDispatcher workDispatcher = new WorkDispatcher(workerHandlers, routes);
            Thread workDispatcherThread = new Thread(workDispatcher);
            workDispatcherThread.start();
        });

        handleClient.start();
        handleWorker.start();
        dispatchWork.start();
        init.start();
    }

    public static void main(String[] args)
    {
        Master master = new Master(5);
        master.start();
    }

}