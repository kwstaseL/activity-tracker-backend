package activity.main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.File;

// DONE

public class Client
{
    // Connection socket to the master
    private Socket connection;
    // Output stream to the master
    private ObjectOutputStream out;
    // Input stream from the master
    private ObjectInputStream in;
    // The file that will be sent to the master
    private File file;

    public Client(File file)
    {
        try
        {
            this.file = file;
            // Creating the connection to the master
            connection = new Socket("localhost", Master.CLIENT_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            System.out.println("Error: " + e.getMessage());
        }
        finally
        {
            shutdown();
        }
    }

    // This method will send the file to the master
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

    // This method will listen for messages from the master
    public void listenForMessages()
    {
        new Thread(() -> {
            while (!connection.isClosed())
            {
                try
                {
                    Object receivedObject = in.readObject();
                    // TODO: This will need to be casted to an ActivityStats object
                }
                catch (Exception e)
                {
                    System.out.println("Could not receive object");
                    System.out.println("Error: " + e.getMessage());
                }
                finally
                {
                    shutdown();
                }
            }
        }).start();

    }

    // This method will close the connection to the master
    // and the input and output streams when an error occurs or the client is done
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
                System.out.println("Could not close connection");
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
                System.out.println("Could not close output stream");
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
                System.out.println("Could not close input stream");
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args)
    {
        File file = new File("./gpxs/route1.gpx");
        File file2 = new File("./gpxs/route2.gpx");
        File file3 = new File("./gpxs/route3.gpx");
        File file4 = new File("./gpxs/route4.gpx");
        File file5 = new File("./gpxs/route5.gpx");
        File file6 = new File("./gpxs/route6.gpx");
        File segment1 = new File("./gpxs/segment1.gpx");
        File segment2 = new File("./gpxs/segment2.gpx");

        Client client = new Client(file);
        Client client2 = new Client(file2);
        Client client3 = new Client(file3);
        Client client4 = new Client(file4);
        Client client5 = new Client(file5);
        Client client6 = new Client(file6);
        Client client7 = new Client(segment1);
        Client client8 = new Client(segment2);

        Thread c1 = new Thread(client::sendFile);

        Thread c2 = new Thread(client2::sendFile);

        Thread c3 = new Thread(client3::sendFile);

        Thread c4 = new Thread(client4::sendFile);

        Thread c5 = new Thread(client5::sendFile);

        Thread c6 = new Thread(client6::sendFile);

        Thread c7 = new Thread(client7::sendFile);
        Thread c8 = new Thread(client8::sendFile);

        c1.start();
        c2.start();
        c3.start();
        c4.start();
        c5.start();
        c6.start();
        c7.start();
        c8.start();


        client.listenForMessages();
        client2.listenForMessages();
        client3.listenForMessages();
        client4.listenForMessages();
        client5.listenForMessages();
        client6.listenForMessages();
        client7.listenForMessages();
        client8.listenForMessages();


    }
}
