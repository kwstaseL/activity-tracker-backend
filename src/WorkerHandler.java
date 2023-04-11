import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

// This class will handle the worker connection
public class WorkerHandler implements Runnable
{
    // This is a queue of all the workers, we use a queue to make the round-robin
    // scheduling easier
    private static Queue<WorkerHandler> workers = new LinkedList<>();

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
        workers.add(this);
    }

    // This is where the worker will be handled
    public void run()
    {
        Thread readData = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                readForData();
            }
        });

        readData.start();
    }

    private void readForData()
    {
        try
        {
            System.out.println("WorkerHandler: Waiting for waypoints");
            // Someone will send me here data and I need to send it to my worker
            while (!workerSocket.isClosed())
            {
                // Receive the file object from the master
                Object receivedObject = in.readObject();

                // Send the file object to the worker
                out.writeObject(receivedObject);
                out.flush();
            }
        }
        catch (Exception e)
        {
            System.out.println("WorkerHandler: Connection to master lost");
            close();
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

            workers.remove(this);
            System.out.println("Worker disconnected");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }



}