import activity.parser.Route;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Master
{
    // This will be the port that the client will connect to
    public static final int CLIENT_PORT = 4321;
    // This will be the port that the worker will connect to
    public static final int WORKER_PORT = 4322;
    private ServerSocket clientSocket;
    private ServerSocket workerSocket;
    private Queue<WorkerHandler> workerHandlers;
    private Queue<ClientHandler> clientHandlers;

    private Queue<Route> routes;

    private int numOfWorkers;

    public Master(int numOfWorkers)
    {
        try
        {
            this.numOfWorkers = numOfWorkers;
            clientSocket = new ServerSocket(CLIENT_PORT);
            workerSocket = new ServerSocket(WORKER_PORT);
            workerHandlers = new LinkedList<>();
            clientHandlers = new LinkedList<>();
            routes = new LinkedList<>();
        }
        catch (Exception e)
        {
            System.out.println("Could not create sockets");
            System.out.println("Error: " + e.getMessage());
        }
    }

    // This method will start the master server and listen for connections
    private void start()
    {
        Thread init = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < numOfWorkers; i++)
                {
                    Worker worker = new Worker();
                }

                WorkDispatcher workDispatcher = new WorkDispatcher(workerHandlers, routes);
                Thread workDispatcherThread = new Thread(workDispatcher);
                workDispatcherThread.start();
            }
        });

        Thread handleClient = new Thread(new Runnable()
        {
            @Override
            public void run()
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
                        clientHandlers.add(clientHandler);
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
            }
        });

        Thread handleWorker = new Thread(new Runnable()
        {
            public void run()
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
                        WorkerHandler workerHandler = new WorkerHandler(worker);
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
            }
        });

        handleClient.start();
        handleWorker.start();
        init.start();
    }

    public static void main(String[] args)
    {
        Master master = new Master(5);
        master.start();
    }

}