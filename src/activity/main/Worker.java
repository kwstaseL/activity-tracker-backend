package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.Chunk;
import activity.mapreduce.Map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Worker
{
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final Object writeLock = new Object();

    public Worker()
    {
        try
        {
            connection = new Socket("localhost", Master.WORKER_PORT);
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

    public void start()
    {
        Thread readData = new Thread(this::readForData);

        readData.start();
    }

    private void readForData()
    {
        while (!connection.isClosed())
        {
            try
            {
                //System.out.println("WORKER: Waiting for job from master");
                Object receivedObject = in.readObject();
                //System.out.println("WORKER: Received job from master");
                Chunk chunk = (Chunk) receivedObject;
                
                new Thread(() -> handleMapping(chunk)).start();

            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Could not receive object");
                shutdown();
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleMapping(Chunk chunk)
    {
        System.out.println("WORKER: " + "Route: " + chunk.getRoute().getRouteID() + " Received chunk " + chunk.getChunkIndex() + " of " + chunk.getTotalChunks());

        // TODO: Cleanup?
        // intermediate_result: the mapping process returns a key-value pair, where key is the client id, and the value is another pair of chunk, activityStats
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

    private void shutdown()
    {
        if (connection != null)
        {
            try
            {
                connection.close();

            } catch (IOException e)
            {
                System.out.println("Could not close connection");
                throw new RuntimeException(e);
            }
        }
        if (in != null)
        {
            try
            {
                in.close();

            } catch (IOException e)
            {
                System.out.println("Could not close input stream");
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
    }

}