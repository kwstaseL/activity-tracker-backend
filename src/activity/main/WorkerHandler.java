package activity.main;

import activity.parser.Route;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// This class will handle the worker connection
public class WorkerHandler implements Runnable
{
    // These are the input and output streams for the worker
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // This is the socket that the worker is connected to
    private final Socket workerSocket;

    public WorkerHandler(Socket workerSocket)
    {
        this.workerSocket = workerSocket;
        // Add the worker to the queue
        try
        {
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

                if (receivedObject instanceof String receivedMessage)
                {
                    System.out.println("WorkerHandler: Received message from worker: " + receivedMessage);
                    // Handle received message
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("WorkerHandler: Connection to worker lost");
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void processJob(Route route)
    {
        try
        {
            // Send the route to the worker
            System.out.println("WorkerHandler: Sending route to worker");
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
    private void close()
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
            throw new RuntimeException(e);
        }

    }



}