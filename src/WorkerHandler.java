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
    private Socket workerSocket;

    public WorkerHandler(Socket workerSocket)
    {
        this.workerSocket = workerSocket;

        // Add the worker to the queue
        workers.add(this);
    }
    // This is where the worker will be handled
    public void run()
    {


    }



}
