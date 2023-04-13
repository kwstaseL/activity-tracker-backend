package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.Route;
import activity.mapreduce.Map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Queue;

public class Worker
{
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Queue<HashMap<String, ActivityStats>> results;
    private Map mapper;
    private int workerID;
    private static int idGenerator = 0;

    private final Object writeLock = new Object();

    public Worker()
    {
        try
        {
            connection = new Socket("localhost", Master.WORKER_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            workerID = idGenerator++;
            mapper = new Map();
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
        Thread readData = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                readForData();
            }
        });

        readData.start();
    }

    private void readForData()
    {
        while (!connection.isClosed())
        {
            try
            {
                System.out.println("WORKER: Waiting for job from master");
                Object receivedObject = in.readObject();
                System.out.println("WORKER: Received job from master");
                Route route = (Route) receivedObject;
                
                new Thread(() ->
                {
                        handleMapping(route);

                }).start();

            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Could not receive object");
                shutdown();
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleMapping(Route route)
    {
        System.out.println("WORKER: Received route from master " + route);
        Pair<Integer, ActivityStats> intermediate_result = mapper.map(route.getClientID(), route);
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