package activity.main;

import activity.calculations.ActivityStats;
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
    private Map mapFunction;
    private int workerID;
    private static int idGenerator = 0;

    private final Object writeLock = new Object();

    public Worker()
    {
        try
        {
            workerID = idGenerator++;
            connection = new Socket("localhost", Master.WORKER_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            mapFunction = new Map();
        }
        catch (Exception e)
        {
            System.out.println("Could not connect to master");
            close();
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
                Route route = (Route) receivedObject;


                new Thread(() -> {
                        handleMapping(route);
                }).start();

            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("Could not receive object");
                close();
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleMapping(Route route)
    {
        System.out.println("WORKER: Received route from master " + route);
        HashMap<String, ActivityStats> result = mapFunction.map(route.getClientID(), route);
        try
        {
            // Send the result back to the master
            synchronized (writeLock)
            {
                out.writeObject(result);
                out.flush();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void close()
    {
        if (connection != null)
        {
            try
            {
                connection.close();

            } catch (IOException e)
            {
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
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args)
    {
        Worker worker = new Worker();
        worker.start();
    }


}