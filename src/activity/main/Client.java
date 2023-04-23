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

    // initialised: represents the state of Client objects. If false, clients cannot be initialised.
    private static boolean initialised = false;

    // unprocessedDirectory: The directory with all the gpx available for processing
    private static String directory;
    private static String masterIP;
    private static int clientPort;

    // lock: dummy object used for synchronization
    private final Object lock = new Object();

    public Client(File file)
    {
        if (!initialised) {
            clientInitialisation();
        }

        this.file = file;

        // Create a socket that will connect to the master
        try {
            connection = new Socket(masterIP, clientPort);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (Exception e) {
            System.out.println("Could not connect to master");
            shutdown();
            throw new RuntimeException(e);
        }
    }

    /* clientInitialisation: To be called before the first Client object is instantiated, or during the first Client instantiation.
     * Initiates all the necessary attributes a Client object should be aware of.
     */
    public static void clientInitialisation()
    {
        // if the Client class has already been initialised, return
        if (Client.initialised) {
            return;
        }

        Properties config = new Properties();
        try {
            config.load(new FileInputStream("config.properties"));
            masterIP = config.getProperty("master_ip");
            clientPort = Integer.parseInt(config.getProperty("client_port"));
            directory = config.getProperty("unprocessed_directory");
            initialised = true;
        } catch (Exception e) {
            System.out.println("Initialisation of clients failed.");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void sendFile()
    {
        try
        {
            // TODO: Cleanup
            /*
            if (isSegment)
            {
                out.writeObject("SEGMENT");
            }
            else
            {
                out.writeObject("ROUTE");
            }
             */
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
                    System.out.println("Statistics for user: ");
                    System.out.println("Statistics across all users: ");
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

        // TODO: Make the following a while(true) loop?

        System.out.println("Available files:");
        File directory = new File(Client.directory);

        // directoryContents: Lists all the files (not folders) included in our directory.
        // (essentially just excluding the segment folder)
        File[] directoryContents = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();   // file.isFile(): returns false if the file is a directory (like segments)
            }
        });

        // if our directory is empty, there is nothing left to process
        if (directoryContents == null || directoryContents.length == 0)
        {
            System.out.println("No routes are available for processing!");
            return;
        }

        // list all routes
        for (int i = 0; i < directoryContents.length; i++) {
            System.out.println(i + ": " + directoryContents[i].getName());
        }


        // Prompt user to select a route
        String input = null;
        Integer choice = null;

        // Acceptable input: all or "all" to send all routes, or anything in the range of 0 to directoryContents.length to send a single route.
        while (choice == null || choice < 0 || choice >= directoryContents.length)
        {
            System.out.println("\nEnter \"all\" to send all routes, or enter a route index (0-" + (directoryContents.length - 1) +") to send a single route.");
            try
            {
                input = scanner.nextLine();
                choice = Integer.valueOf(input);
            }
            catch (NumberFormatException e)
            {
                // if the exception was caused by the user typing "all", send all the routes
                if (input != null && (input.equalsIgnoreCase("all") || input.equalsIgnoreCase("\"all\"")))
                {
                    sendAllRoutes(directoryContents);
                    return;
                }

                // else, ignore the invalid input and prompt the user to select a route again
            }
        }

        // Select file based on user choice
        File file = directoryContents[choice];

        Client client = new Client(file);
        Thread clientThread = new Thread(client::sendFile);
        clientThread.start();
        client.listenForMessages();
    }

    private static void sendAllRoutes(File[] directoryContents)
    {
        for (File file : directoryContents)
        {
            Client client = new Client(file);
            Thread clientThread = new Thread(client::sendFile);
            clientThread.start();
            client.listenForMessages();
        }
    }

    public static void main(String[] args)
    {
        clientInitialisation();
        sendRoute();
    }

}