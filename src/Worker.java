import java.net.Socket;


public class Worker
{
    public static void main(String[] args)
    {
        try
        {

            Socket socket = new Socket("localhost", Master.WORKER_PORT);

        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            System.out.println("Error: " + e.getMessage());
        }
    }

}
