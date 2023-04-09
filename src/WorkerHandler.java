import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        workers.add(this);
    }

    // This is where the worker will be handled
    public void run()
    {
        try
        {
            in = new ObjectInputStream(workerSocket.getInputStream());
            out = new ObjectOutputStream(workerSocket.getOutputStream());

            while (!workerSocket.isClosed())
            {
                // Receive the file object from the worker
                Object receivedObject = in.readObject();

            }

        }
        catch (Exception e)
        {
            System.out.println("Connection to worker lost");
            close();
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

            workers.remove(this);
            System.out.println("Worker disconnected");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }



}