package activity.main;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

// Represents the client which is basically the user of the application
// He will be able to send files to the master and receive the results of his statistics back
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

    public Client(File file)
    {
        if (!initialised)
        {
            clientInitialisation();
        }
        this.file = file;
        // Create a socket that will connect to the master
        try
        {
            connection = new Socket(masterIP, clientPort);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (Exception e)
        {
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

        // otherwise, initialise the Client class
        Properties config = new Properties();
        try
        {
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

    // This is the UI for the client
    // It will allow the user to select a route to be sent for processing and will display the results
    // It will also allow him to select a segment that he will decide, and find the statistics for that segment for all users
    private static void startMessageLoop()
    {
        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.println("Available files:");
            File directory = new File(Client.directory);

            // directoryContents: Lists all the files (not folders) included in our directory.
            // (essentially just excluding the segment folder)
            File[] directoryContents = directory.listFiles(new FileFilter()
            {
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
            for (int i = 0; i < directoryContents.length; i++)
            {
                System.out.println(i + ": " + directoryContents[i].getName());
            }

            // Prompt user to select a route or segment
            String input = null;
            Integer choice = null;

            // Acceptable input: all or "all" to send all routes/segments, or anything in the range of 0 to the total number
            // of routes/segments to send a single route/segment.
            while (choice == null || choice < 0 || choice >= directoryContents.length)
            {
                System.out.println("\nEnter \"all\" to send all routes, or enter a file index (0-"
                        + (directoryContents.length - 1) +") to send a single route/segment:");

                input = scanner.nextLine();

                if (input.equalsIgnoreCase("all"))
                {
                    sendAllRoutes(directoryContents);
                    return;
                }

                try
                {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e)
                {
                    System.out.println("Invalid input. Please enter a valid file index or \"all\".");
                    continue;
                }

                if (choice < 0 || choice >= directoryContents.length) {
                    System.out.println("Invalid input. Please enter a valid file index or \"all\".");
                    choice = null;
                    continue;
                }

                File file = directoryContents[choice];

                // send the selected route/segment
                Client client = new Client(file);
                client.sendFile();
                client.listenForMessages();
            }
        }
    }

    // This method will be used to receive the statistics from the master.
    public void listenForMessages()
    {
        try
        {
            // routeStats: contains the statistics for the route that was sent to the master
            Object routeStats = in.readObject();
            System.out.println("Output for file | " + file.getName() + " | " + routeStats + "\n");
            // userStats: contains the overall statistics for the user that sent the route to the master
            Object userStats = in.readObject();
            System.out.println(userStats + "\n");
            // allUsersStats: contains the overall statistics for all users that have sent routes to the master
            Object allUsersStats = in.readObject();
            System.out.println(allUsersStats + "\n");
        }
        catch (Exception e)
        {
            System.out.println("Could not receive object");
            e.printStackTrace();
            shutdown();
        }

    }

    // This method will be used to send the file to the master
    public void sendFile()
    {
        try
        {
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

    // This method will be used to send all routes to the master
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

    // This method will be used to close the connection with the master and the streams
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

    public static void main(String[] args)
    {
        clientInitialisation();
        startMessageLoop();
    }

}