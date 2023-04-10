import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.File;

public class Client
{

    private Socket connection;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private File file;

    boolean fileSent = false;

    public Client(File file)
    {
        try
        {
            this.file = file;
            connection = new Socket("localhost", Master.CLIENT_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            close();
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void sendFile()
    {
        try
        {
            while (!connection.isClosed())
            {
                if (!fileSent)
                {
                    out.writeObject(file);
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
            close();
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void close()
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    public static void main(String[] args)
    {
        File file = new File("./gpxs/route1.gpx");
        File file2 = new File("./gpxs/route2.gpx");
        Client client = new Client(file);
        Client client2 = new Client(file2);
        Thread c1 = new Thread(new Runnable()
        {
            @Override
            public void run() {
                client.sendFile();
            }

        });

        Thread c2 = new Thread(new Runnable()
        {
            @Override
            public void run() {
                client2.sendFile();
            }

        });

        c1.start();
        c2.start();

    }
}
