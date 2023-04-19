package activity.main;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class Client
{
    // This is the socket that the client is connected to with the master
    private Socket connection;
    // This is the output stream that will be used to send objects to the master
    private ObjectOutputStream out;
    // This is the input stream that will be used to receive objects from the master
    private ObjectInputStream in;
    // This is the file that will be sent to the master
    private File file;

    // TODO: Print the contents of the appropriate folder when asking what route to process

    private final Object lock = new Object();

    public Client(File file)
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            final String masterIP = config.getProperty("master_ip");
            final int client_port = Integer.parseInt(config.getProperty("client_port"));

            this.file = file;
            // Create a socket that will connect to the master
            connection = new Socket(masterIP, client_port);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            shutdown();
        }
    }

    public void sendFile(boolean isSegment)
    {
        try
        {
            if (isSegment)
            {
                out.writeObject("SEGMENT");
            }
            else
            {
                out.writeObject("ROUTE");
            }
            System.out.println("Sending file " + file.getName() + " to master\n");
            out.writeObject(file);
            out.flush();
        }
        catch (Exception e)
        {
            System.out.println("Could not send file");
            shutdown();
        }
    }

    public void listenForMessages()
    {
        new Thread(() -> {
            while (!connection.isClosed())
            {
                try
                {
                    Object receivedObject = in.readObject();
                    System.out.println("Output for file | " + file.getName() + " | " + receivedObject + "\n");
                }
                catch (Exception e)
                {
                    System.out.println("Could not receive object");
                    shutdown();
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
                System.out.println("Could not close connection");
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
            }
        }
    }

    private static void sendRoute()
    {
        Scanner scanner = new Scanner(System.in);

        // Prompt user to select a route or segment
        System.out.println("Select a route: ");
        System.out.println("1. Route 1");
        System.out.println("2. Route 2");
        System.out.println("3. Route 3");
        System.out.println("4. Route 4");
        System.out.println("5. Route 5");
        System.out.println("6. Route 6");
        System.out.println("7. Send all routes");
        System.out.println("Enter your choice:");

        // Get user input
        final int choice = scanner.nextInt();

        // Select file based on user choice
        File file = null;
        if (choice >= 1 && choice <= 6)
        {
            file = new File("./gpxs/route" + choice + ".gpx");

            Client client = new Client(file);
            Thread c1 = new Thread(() -> client.sendFile(false));
            c1.start();
            client.listenForMessages();
        }
        else if (choice == 7)
        {
            File file1 = new File("./gpxs/route1.gpx");
            File file2 = new File("./gpxs/route2.gpx");
            File file3 = new File("./gpxs/route3.gpx");
            File file4 = new File("./gpxs/route4.gpx");
            File file5 = new File("./gpxs/route5.gpx");
            File file6 = new File("./gpxs/route6.gpx");

            Client client = new Client(file1);
            Client client2 = new Client(file2);
            Client client3 = new Client(file3);
            Client client4 = new Client(file4);
            Client client5 = new Client(file5);
            Client client6 = new Client(file6);

            Thread c1 = new Thread(() -> client.sendFile(false));

            Thread c2 = new Thread(() -> client2.sendFile(false));

            Thread c3 = new Thread(() -> client3.sendFile(false));

            Thread c4 = new Thread(() -> client4.sendFile(false));

            Thread c5 = new Thread(() -> client5.sendFile(false));

            Thread c6 = new Thread(() -> client6.sendFile(false));

            c1.start();
            c2.start();
            c3.start();
            c4.start();
            c5.start();
            c6.start();

            client.listenForMessages();
            client2.listenForMessages();
            client3.listenForMessages();
            client4.listenForMessages();
            client5.listenForMessages();
            client6.listenForMessages();
        }
        else if (choice > 7)
        {
            System.out.println("Invalid choice");
            System.exit(0);
        }
    }

    private static void sendSegment()
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select a segment: ");
        System.out.println("1. Segment 1");
        System.out.println("2. Segment 2");

        int choice = scanner.nextInt();

        File file = null;
        if (choice >= 1 && choice <= 2)
        {
            file = new File("./gpxs/segment" + choice + ".gpx");

            Client client = new Client(file);
            Thread c1 = new Thread(() -> client.sendFile(true));
            c1.start();
            client.listenForMessages();
        }
        else
        {
            System.out.println("Invalid choice");
            System.exit(0);
        }


    }
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select an option: ");
        System.out.println("1. Send a route");
        System.out.println("2. Send a segment");
        System.out.println("Enter your choice:");
        final int choice = scanner.nextInt();

        if (choice == 1)
        {
            sendRoute();
        }
        else if (choice == 2)
        {
            sendSegment();
        }
        else
        {
            System.out.println("Invalid choice");
            System.exit(0);
        }

    }



}
