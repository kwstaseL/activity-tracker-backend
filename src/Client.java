import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.File;

public class Client
{
    public static void main(String[] args)
    {
        try
        {
            Socket socket = new Socket("localhost", Master.CLIENT_PORT);
            File filetoSend = new File("/Users/kwstasel/IdeaProjects/Activity Tracker/data/route1.gpx");
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(filetoSend);
            out.flush();
            out.close();
            socket.close();

        } catch (Exception e)
        {
            System.out.println("Could not connect to master");
            System.out.println("Error: " + e.getMessage());
        }
    }

}