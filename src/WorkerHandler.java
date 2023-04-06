import java.net.Socket;


// This class will handle the worker connection
public class WorkerHandler implements Runnable
{
    // This is the socket that the worker is connected to
    private Socket workerSocket = null;

    public WorkerHandler(Socket workerSocket)
    {
        this.workerSocket = workerSocket;
    }

    // This is where the worker will be handled
    public void run()
    {

    }


}
