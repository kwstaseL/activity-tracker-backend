package activity.main;

import activity.calculations.Statistics;
import activity.misc.GPXData;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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
    // unprocessedDirectory: The directory with all the gpx available for processing
    private static String directory;
    private static String masterIP;
    private static int clientPort;
    private String unprocessedDirectory;
    private String completedDirectory;
    private Object messageLock = new Object();

    public Client()
    {
        clientInitialisation();
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
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            unprocessedDirectory = (new File(config.getProperty("unprocessed_directory"))).getAbsolutePath();
            completedDirectory = (new File(config.getProperty("completed_directory"))).getAbsolutePath();

            try
            {
                File directory = new File(completedDirectory);
                if (!directory.exists())
                {
                    directory.mkdirs();
                }
            }
            catch (Exception e)
            {
                System.out.println("Could not create the directory for the completed gpx.");
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

            config.load(new FileInputStream("config.properties"));
            masterIP = config.getProperty("master_ip");
            clientPort = Integer.parseInt(config.getProperty("client_port"));
            directory = config.getProperty("unprocessed_directory");
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

        // TODO: Check if username already exists the isDuplicateUsername can be implemented on the server side
        // if (isDuplicateUsername(username))
        // {
        //    System.out.println("Username is already connected with tha username. Please try again.");
        // }

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
            if (filteredFiles.length == 0)
            {
                System.out.println("No routes are available for processing!");
                return;
            }

            // else print the files and prompt the user to select a route
            for (int i = 0; i < filteredFiles.length; i++)
            {
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

                if (choice < 0 || choice >= filteredFiles.length)
                {
                    System.out.println("Invalid input. Please enter a valid file index.");
                    choice = null;
                    continue;
                }
                File selectedGPX = filteredFiles[choice];
                // Create a new thread that will send the file to the master and listen for the results
                sendFile(selectedGPX);
                // Create a new ObjectInputStream for each iteration
                listenForMessages(selectedGPX);
            }
        }
    }
    private void listenForMessages(File selectedGPX)
    {
        try {

            // Read the objects from the input stream
            Object routeStats = in.readObject();
            Object userStats = in.readObject();
            Object allUsersStats = in.readObject();

            // Print the received statistics
            System.out.println("Output for file | " + selectedGPX.getName() + "| " + routeStats + "\n");
            System.out.println(userStats + "\n");
            System.out.println(allUsersStats + "\n");

        } catch (Exception e)
        {
            System.out.println("Could not receive objects");
            e.printStackTrace();
            shutdown();
        }

        Path sourcePath = Paths.get(unprocessedDirectory + File.separator + selectedGPX.getName());
        Path destPath = Paths.get(completedDirectory + File.separator + selectedGPX.getName());
        try
        {
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
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
    private File[] filterFilesByUsername(File[] directoryContents, String username)
    {
        return Arrays.stream(directoryContents)
                .filter(file -> containsUsername(file, username)).toArray(File[]::new);
    }

    // Checks if the username is in the creator attribute for the that file
    private boolean containsUsername(File file, String username)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            final String usernameTag = "creator=\"" + username + "\"";

            while ((line = reader.readLine()) != null)
            {
                if (line.contains(usernameTag))
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
        try
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not close connection");
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        Client client = new Client();
        client.startMessageLoop();
    }
}