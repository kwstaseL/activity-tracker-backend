package activity.main;

import activity.calculations.ActivityStats;
import activity.misc.Pair;
import activity.parser.Chunk;
import activity.mapreduce.Map;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

// Worker class responsible for handling the mapping of the data
public class Worker
{
    // This is the socket that the worker is connected to
    private Socket connection;
    // Input stream for the worker to receive objects from the master
    private ObjectInputStream in;
    // Output stream for the worker to send objects to the master
    private ObjectOutputStream out;
    // This is the lock that will be used to ensure that only one thread can write to the output stream at a time
    private final Object writeLock = new Object();

    public Worker()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            final String masterIP = config.getProperty("master_ip");
            final int WORKER_PORT = Integer.parseInt(config.getProperty("worker_port"));
            // Creating a socket that will connect to the master and creating the input and output streams
            connection = new Socket(masterIP, WORKER_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            System.out.println("Worker: Connection to master established");
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            shutdown();
            System.out.println("Error: " + e.getMessage());
        }

    }

    // This method will start the worker and start listening for jobs from the master
    private void start()
    {
        Thread readData = new Thread(this::readForData);
        readData.start();
    }

    // This method will read data from master and handle the mapping of the data.
    // For each chunk that is received, a new thread will be created to handle the mapping of the data
    private void readForData()
    {
        while (!connection.isClosed())
        {
            try
            {
                Object receivedObject = in.readObject();
                Chunk chunk;

                // If the received object is a chunk, a new thread will be created to handle the mapping of the data
                if (receivedObject instanceof Chunk)
                {
                    chunk = (Chunk) receivedObject;
                    new Thread(() -> handleMapping(chunk)).start();
                }
                else
                {
                    throw new RuntimeException("Received unknown object.");
                }

            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Could not receive object");
                shutdown();
                System.out.println("Error: " + e.getMessage());
            }

        }
    }

    // This method will handle the mapping of the data
    // It will call the map method from the Map class and send the result back to the master
    private void handleMapping(Chunk chunk)
    {
        assert chunk != null;
        // intermediate_result: the mapping process returns a key-value pair,
        // where key is the client id, and the value is another pair of chunk, activityStats
        Pair<Integer, Pair<Chunk, ActivityStats>> intermediate_result = Map.map(chunk.getRoute().getClientID(), chunk);
        try
        {
            // Send the result back to the worker-handler
            synchronized (writeLock)
            {
                out.writeObject(intermediate_result);
                out.flush();
            }

        } catch (IOException e)
        {
            System.out.println("Could not send object to the worker handler");
            throw new RuntimeException(e);
        }
    }

    // This method will close the connection to the master and close the input and output streams
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
            System.out.println("Worker: Could not close connection to master");
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        Worker worker = new Worker();
        worker.start();
    }

}