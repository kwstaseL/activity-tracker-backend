package activity.main;

import activity.calculations.Statistics;
import activity.misc.GPXData;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

// Represents the client which is basically the user of the application
// He will be able to send files to the master and receive the results of his statistics back
// TODO: Cleanup the code
public class Client
{
    // This is the socket that the client is connected to with the master
    private Socket connection;

    // This is the output stream that will be used to send objects to the master
    private ObjectOutputStream out;

    // This is the input stream that will be used to receive objects from the master
    private ObjectInputStream in;
    // initialised: represents the state of Client objects. If false, clients cannot be initialised.
    private static boolean initialised = false;
    // unprocessedDirectory: The directory with all the gpx available for processing
    private static String directory;
    private static String masterIP;
    private static int clientPort;

    private Object messageLock = new Object();


    public Client()
    {
        if (!initialised)
        {
            clientInitialisation();
        }
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
    private void clientInitialisation()
    {
        // if the Client class has already been initialised, return
        if (Client.initialised)
        {
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
        }
        catch (Exception e)
        {
            System.out.println("Initialisation of clients failed.");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // This is the UI for the client
    // It will allow the user to select a route to be sent for processing and will display the results
    private void startMessageLoop()
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username:");
        String username = scanner.nextLine().trim().toLowerCase();

        while (true)
        {
            System.out.println("Available files:");
            File directory = new File(Client.directory);

            // Get all the files in the directory
            File[] directoryContents = directory.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile();
                }
            });
            // Filter the files based on the username
            File[] filteredFiles = filterFilesByUsername(directoryContents, username);

            // if no files match the username, inform the user and exit
            if (filteredFiles == null || filteredFiles.length == 0) {
                System.out.println("No routes are available for processing!");
                return;
            }
            // else print the files and prompt the user to select a route
            for (int i = 0; i < filteredFiles.length; i++) {
                System.out.println(i + ": " + filteredFiles[i].getName());
            }

            // Prompt user to select a route
            String input = null;
            Integer choice = null;

            while (choice == null || choice < 0 || choice >= filteredFiles.length)
            {
                System.out.println("\nEnter a file index (0-"
                        + (filteredFiles.length - 1) +") to send a single route/segment:");

                input = scanner.nextLine();
                // TODO: Implement sending all routes
                /*
                if (input.equalsIgnoreCase("all"))
                {
                    sendAllRoutes(filteredFiles);
                    return;
                }
                */
                try
                {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e)
                {
                    System.out.println("Invalid input. Please enter a valid file index.");
                    continue;
                }

                if (choice < 0 || choice >= filteredFiles.length) {
                    System.out.println("Invalid input. Please enter a valid file index.");
                    choice = null;
                    continue;
                }

                File selectedGPX = filteredFiles[choice];
                sendFile(selectedGPX);
                listenForMessages(selectedGPX);
            }
        }
    }
    // This method will be used to receive the statistics from the master.
    private void listenForMessages(File selectedGPX)
    {
        try
        {
            System.out.println("Waiting for message from master...");
            // routeStats: contains the statistics for the route that was sent to the master
            Object routeStats = in.readObject();
            System.out.println("Output for file | " + selectedGPX.getName() + " | " + routeStats + "\n");
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
    private void sendFile(File selectedGPX)
    {
        // Creating the file input stream to read the contents of the file
        try (FileInputStream fileInputStream = new FileInputStream(selectedGPX))
        {
            // Creating a byte buffer array with the same size as the file
            // This will be used to store the contents of the file
            byte[] buffer = new byte[(int) selectedGPX.length()];
            // The read() method will read the contents of the file into the buffer
            int bytesRead = fileInputStream.read(buffer);
            // Creating a GPXData object with the name of the file and the contents of the file
            // and sending it to the master
            GPXData gpx = new GPXData(selectedGPX.getName(), buffer);
            out.writeObject(gpx);
            out.flush();
            System.out.println("File " + selectedGPX.getName() + " sent to master");
        } catch (Exception e)
        {
            System.out.println("Could not send file");
            shutdown();
        }
    }

    /*
    // This method will be used to send all routes to the master
    private void sendAllRoutes(File[] filteredFiles)
    {
        for (File file : filteredFiles)
        {
            Thread clientThread = new Thread(() ->
            {
                setFile(file);
                sendFile();
                listenForMessages();
            });
            clientThread.start();
        }
    }
    */

    // The Arrays.stream() method will convert the array into a stream
    // The filter() method will filter the stream based on the username in the creator attribute for each file in the directory
    // This will return a stream of files that match the username
    // The filtering will be done first and then the stream will be converted back into an array
    private static File[] filterFilesByUsername(File[] directoryContents, String username)
    {
        return Arrays.stream(directoryContents)
                .filter(file -> containsUsername(file, username))
                .toArray(File[]::new);
    }

    // Checks if the username is in the creator attribute for the that file
    private static boolean containsUsername(File file, String username)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("creator=\"" + username + "\""))
                {
                    return true;
                }
            }
        } catch (IOException e)
        {
            System.out.println("Could not read file");
            e.printStackTrace();
        }
        return false;
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
        Client client = new Client();
        client.startMessageLoop();
    }
}