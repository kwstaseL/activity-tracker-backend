import java.net.Socket;


public class Client
{

    public static void main(String[] args)
    {
        try
        {
            Socket socket = new Socket("localhost", Master.CLIENT_PORT);

        } catch (Exception e)
        {
            System.out.println("Could not connect to master");
            System.out.println("Error: " + e.getMessage());
        }
    }

}