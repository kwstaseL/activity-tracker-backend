import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Worker
{
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Worker()
    {
        try
        {
            connection = new Socket("localhost", Master.WORKER_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            System.out.println("Error: " + e.getMessage());
        }

    }

    private void start()
    {
        while (!connection.isClosed())
        {
            try
            {
                Object receivedObject = in.readObject();

            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Could not receive object");
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args)
    {
        Worker worker = new Worker();
        worker.start();
    }

}
