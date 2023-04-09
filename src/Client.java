import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.File;

public class Client
{

    private Socket socket;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private File file;

    boolean fileSent = false;

    public Client()
    {
        try
        {
            socket = new Socket("localhost", Master.CLIENT_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void sendFile()
    {
        try
        {
            while (!socket.isClosed())
            {
                if (!fileSent)
                {
                    File filetoSend = new File("/Users/kwstasel/IdeaProjects/Activity Tracker/data/route1.gpx");
                    out.writeObject(filetoSend);
                    out.flush();
                    fileSent = true;
                }
                else // here this if statement is just for testing purposes to see if the file is sent to the master, it will be removed later
                {
                    Object receivedObject = in.readObject();
                    if (receivedObject instanceof File receivedFile)
                    {
                        System.out.println("Received file: " + receivedFile.getName());
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Could not send file");
            System.out.println("Error: " + e.getMessage());
        }
    }
    public static void main(String[] args)
    {
        Client client = new Client();
        client.sendFile();
    }

}