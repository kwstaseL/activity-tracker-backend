package com.activity_tracker.backend.main;

import com.activity_tracker.backend.calculations.SegmentLeaderboard;
import com.activity_tracker.backend.misc.GPXData;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

/**
 * Represents the user of the application.
 * He will be able to send files to the master and receive the results of his statistics back.
 */
@Deprecated
public class User
{
    // This is the socket that the user is connected to with the master
    private final Socket connection;

    // This is the output stream that will be used to send objects to the master
    private final ObjectOutputStream out;

    // This is the input stream that will be used to receive objects from the master
    private final ObjectInputStream in;

    // unprocessedDirectory: The directory with all the gpx available for processing
    private String unprocessedDirectory;
    private String completedDirectory;
    private static String masterIP;
    private static int clientPort;

    // TODO: Adjust to match the frontend approach

    /**
     * Constructs a new User by initialising the attributes and connecting to the master.
     */
    public User()
    {
        userInitialisation();
        // Create a socket that will connect to the master
        try
        {
            connection = new Socket(masterIP, clientPort);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            shutdown();
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialises the user by reading the config file and setting the attributes.
     */
    private void userInitialisation()
    {
        try (FileInputStream configInput = new FileInputStream("config.properties"))
        {
            Properties config = new Properties();
            config.load(configInput);

            unprocessedDirectory = new File(config.getProperty("unprocessed_directory")).getAbsolutePath();
            completedDirectory = new File(config.getProperty("completed_directory")).getAbsolutePath();

            File directory = new File(completedDirectory);
            if (!directory.exists() && !directory.mkdirs())
            {
                throw new RuntimeException("Could not create the directory for the completed gpx.");
            }

            masterIP = config.getProperty("master_ip");
            clientPort = Integer.parseInt(config.getProperty("client_port"));
        }
        catch (IOException e)
        {
            System.out.println("Initialization of clients failed.");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * The UI used for the user.
     * It will allow the user to select a route to be sent for processing and will display the results of the processing.
     */
    private void startMessageLoop()
    {
        try (Scanner scanner = new Scanner(System.in))
        {
            System.out.println("Enter your username:");
            String username = scanner.nextLine().trim().toLowerCase();

            // send the username to the master to check if it is already connected
            try
            {
                out.writeObject(username);
                // Read the response from the master
                String response = (String) in.readObject();
                // If the response is not "OK", then the username is already connected
                if (!response.equals("OK"))
                {
                    System.out.println("This user is already connected.");
                    shutdown();
                    return;
                }
            }
            catch (Exception e)
            {
                System.out.println("Could not send the username to the master.");
                shutdown();
                throw new RuntimeException(e);
            }

            System.out.println("Welcome " + username + "!");

            while (true)
            {
                System.out.println("Available files:");
                File directory = new File(unprocessedDirectory);

                // Get all the files in the directory
                File[] directoryContents = directory.listFiles(File::isFile);
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
                Integer choice = null;

                while (choice == null || choice < 0 || choice >= filteredFiles.length)
                {
                    String message = filteredFiles.length > 1 ? "Enter a file index (0-" + (filteredFiles.length - 1) + ") to send a single route:"
                            : "Enter 0 to send the only available route:";
                    System.out.println("\n" + message);

                    String input = scanner.nextLine();
                    try
                    {
                        choice = Integer.parseInt(input);
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("Invalid input. Please enter a valid file index.");
                        continue;
                    }

                    if (choice < 0 || choice >= filteredFiles.length)
                    {
                        System.out.println("Invalid input. Please enter a valid file index.");
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
        catch (Exception e)
        {
            System.out.println("Could not read the input from the user.");
            System.out.println(e.getMessage());
            shutdown();
        }
    }


    /**
     * Listens for messages from the master and prints them to the console
     * This messages will contain the statistics for the route, the statistics for the user and the statistics for all the users
     * @param selectedGPX the file that was sent to the master
     */
    @SuppressWarnings("unchecked")
    private void listenForMessages(File selectedGPX)
    {
        try {

            // Read the objects from the input stream
            Object routeStats = in.readObject();
            Object userStats = in.readObject();
            Object allUsersStats = in.readObject();
            Object leaderboardObject = in.readObject();

            // Print the received statistics
            System.out.println();
            System.out.println("Statistics for your route (" + selectedGPX.getName() + "): " );
            System.out.println(routeStats + "\n");
            System.out.println("Your overall performance:");
            System.out.println(userStats + "\n");
            System.out.println("Average overall performance for all users:");
            System.out.println(allUsersStats + "\n");

            ArrayList<SegmentLeaderboard> leaderboards = (ArrayList<SegmentLeaderboard>) leaderboardObject;
            for (SegmentLeaderboard leaderboard : leaderboards)
            {
                System.out.println(leaderboard + "\n");
            }
        }
        catch (Exception e)
        {
            System.out.println("Could not read the statistics.");
            System.out.println(e.getMessage());
            shutdown();
        }

        // Move the file to the completed directory
        Path sourcePath = Paths.get(unprocessedDirectory + File.separator + selectedGPX.getName());
        Path destPath = Paths.get(completedDirectory + File.separator + selectedGPX.getName());
        try
        {
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            System.out.println("Could not move the file to the completed directory.");
            shutdown();
        }
    }

    /**
     * This method will be used to send the file to the master
     * @param selectedGPX The file that the user selected to be sent to the master
     */
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

            // Creating a GPXData object with the name of the file and the contents of the file and sending it to master
            GPXData gpx = new GPXData(selectedGPX.getName(), buffer);
            out.writeObject(gpx);
            out.flush();

        }
        catch (Exception e)
        {
            System.out.println("Could not send file");
            shutdown();
        }
    }


    /**
     * This method will be used to filter the files based on the username
     * @param directoryContents The files in the directory
     * @param username The username of the user
     * @return The files that match the username
     *
     * @Description: The Arrays.stream() method will convert the array into a stream
     * The filter() method will filter the stream based on the username in the creator attribute for each file in the directory
     * This will return a stream of files that match the username
     * The filtering will be done first and then the stream will be converted back into an array
     */
    private File[] filterFilesByUsername(File[] directoryContents, String username)
    {
        return Arrays.stream(directoryContents)
                .filter(file -> containsUsername(file, username)).toArray(File[]::new);
    }

    /**
     * This method will be used to check if the file contains the username
     * @param file The file to be checked
     * @param username The username of the user
     * @return True if the file contains the username, false otherwise
     */
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
        }
        catch (IOException e)
        {
            System.out.println("Could not read file");
            shutdown();
        }
        return false;
    }


    /**
     * Method used to shut down the connection with the master and close the input and output streams
     */
    private void shutdown()
    {
        try
        {
            if (in != null)
            {
                in.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not close the input stream.");
        }
        try
        {
            if (out != null)
            {
                out.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not close the output stream.");
        }
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not close the connection.");
        }
    }
    public static void main(String[] args)
    {
        User user = new User();
        user.startMessageLoop();
    }
}