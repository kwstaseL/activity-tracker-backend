import java.net.ServerSocket;
import java.net.Socket;

public class Master
{
    // This will be the port that the client will connect to
    private static final int CLIENT_PORT = 4321;
    // This will be the port that the worker will connect to
    private static final int WORKER_PORT = 4322;

    private ServerSocket clientSocket;
    private ServerSocket workerSocket;

    public Master()
    {
        try
        {
            clientSocket = new ServerSocket(CLIENT_PORT);
            workerSocket = new ServerSocket(WORKER_PORT);
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
        Thread handleClient = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!clientSocket.isClosed())
                {
                    try
                    {
                        System.out.println("Waiting for client connection");
                        // Accept a client connection
                        Socket client = clientSocket.accept();
                        System.out.println("Client connected");
                        // Create a new thread to handle the client
                        // Start the thread
                    } catch (Exception e)
                    {
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
                        System.out.println("Waiting for worker connection");
                        // Accept a worker connection
                        Socket worker = workerSocket.accept();
                        System.out.println("Worker connected");
                        // Create a new thread to handle the worker
                        WorkerHandler workerHandler = new WorkerHandler(worker);
                        Thread workerThread = new Thread(workerHandler);
                        workerThread.start();

                    } catch (Exception e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        });

        handleClient.start();
        handleWorker.start();
    }

    public static void main(String[] args)
    {
        Master master = new Master();
        master.start();
    }



}
