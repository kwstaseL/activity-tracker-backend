package activity.main;

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
            shutdown();
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void sendFile()
    {
        try
        {
            out.writeObject(file);
            out.flush();
        }
        catch (Exception e)
        {
            System.out.println("Could not send file");
            shutdown();
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void listenForMessages()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!connection.isClosed())
                {
                    try
                    {
                        Object receivedObject = in.readObject();
                        if (receivedObject instanceof File receivedFile)
                        {
                            System.out.println("Received file: " + receivedFile.getName());
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println("Could not receive object");
                        shutdown();
                        System.out.println("Error: " + e.getMessage());
                    }

                }
            }
        }).start();

    }

    private void shutdown()
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
        File file3 = new File("./gpxs/route3.gpx");
        Client client = new Client(file);
        Client client2 = new Client(file2);
        Client client3 = new Client(file3);

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

        Thread c3 = new Thread(new Runnable()
        {
            @Override
            public void run() {
                client3.sendFile();
            }

        });
        c1.start();
        c2.start();
        c3.start();

        client.listenForMessages();
        client2.listenForMessages();
        client3.listenForMessages();

    }
}
