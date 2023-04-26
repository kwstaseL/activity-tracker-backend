package activity.main;

import activity.parser.Chunk;
import activity.parser.Waypoint;
import activity.parser.Route;

import java.util.ArrayList;
import java.util.Queue;

// This class is responsible for taking in a route and splitting it into chunks, then sending the chunks to the workers
// using round-robin
public class WorkDispatcher implements Runnable
{
    // This is the queue that contains all the workers
    private final Queue<WorkerHandler> workers;
    // This is the queue that contains all the routes that need to be handled
    private final Queue<Route> filesToWorker;

    public WorkDispatcher(Queue<WorkerHandler> workers, Queue<Route> filesToWorker)
    {
        this.workers = workers;
        this.filesToWorker = filesToWorker;
    }

    public void run()
    {
        synchronized (filesToWorker)
        {
            while (true)
            {
                while (filesToWorker.isEmpty())
                {
                    try
                    {

                        filesToWorker.wait();

                    } catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                Route route = filesToWorker.poll();
                handleRoute(route);
            }
        }
    }

    // Takes in a route and splits it into chunks, then sends the chunks to the workers using round-robin
    private void handleRoute(Route route)
    {
        ArrayList<Waypoint> waypoints = route.waypoints();
        final int waypointsSize = waypoints.size();

        // n will represent the chunk size
        final int n = calculateChunkSize(waypointsSize);

        // expectedChunks: determines how many chunks of waypoints the route will be split into
        final int expectedChunks = calculateExpectedChunks(waypointsSize, n);

        ArrayList<Waypoint> waypointChunk = new ArrayList<>();
        int chunkIndex = 0;
        int chunks = 0;

        final int routeID = route.getRouteID();
        final int clientID = route.getClientID();
        final String user = route.getUser();
        final String fileName = route.getFileName();

        for (int i = 0; i < waypoints.size(); i++)
        {
            Waypoint currentWaypoint = waypoints.get(i);
            waypointChunk.add(currentWaypoint);

            // First condition: Turns true when the first chunk is full.
            if (waypointChunk.size() == n && chunks == 0) {
                chunks++;
                chunkIndex++;
                createChunk(waypointChunk, routeID, clientID, user, expectedChunks, chunkIndex, fileName);

                if (i != waypoints.size() - 1) {
                    // clear the chunk for the next set of waypoints
                    waypointChunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    waypointChunk.add(waypoints.get(i));
                }

            } // Second condition: Turns true when we reach the end of the waypoints to be processed, at which point we assign the chunk as is to a worker to process
            else if (i == waypoints.size() - 1)
            {
                chunks++;
                chunkIndex++;
                createChunk(waypointChunk, routeID, clientID, user, expectedChunks, chunkIndex, fileName);

            } // Third condition: Turns true when a chunk after the first is full. Size limit is n+1, since it needs to hold the last waypoint of the previous chunk
            else if (waypointChunk.size() == n + 1 && chunks != 0)
            {
                chunks++;
                chunkIndex++;
                createChunk(waypointChunk, routeID, clientID, user, expectedChunks, chunkIndex, fileName);

                if (i != waypoints.size() - 1)
                {
                    // clear the chunk for the next set of waypoints
                    waypointChunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    waypointChunk.add(currentWaypoint);
                }
            }
        }
        System.err.println("Finished chunking up route: " + routeID + ". Total chunks: " + chunks);
        System.err.println("Expected chunks were: " + expectedChunks);
    }

    // Creates the chunk and sends it to a worker
    private void createChunk(ArrayList<Waypoint> chunkWaypoints, int routeID, int clientID, String user,
                             int expectedChunks, int chunkIndex, String fileName)
    {
        WorkerHandler worker = workers.poll();
        assert worker != null;

        Route chunkedRoute = new Route(chunkWaypoints, routeID, clientID, user, fileName);
        Chunk chunk = new Chunk(chunkedRoute, expectedChunks, chunkIndex);

        worker.processJob(chunk);
        // adding the worker to the end of the queue
        workers.add(worker);
    }

    private int calculateExpectedChunks(int waypointsSize,int n)
    {
        return (int) Math.ceil(waypointsSize / (double) n);
    }

    private int calculateChunkSize(int numWaypoints)
    {
        // if there's more waypoints than workers provided, make n equal to waypoints.size / workers.size * 2
        if (numWaypoints >= workers.size())
        {
            return (int) Math.ceil(numWaypoints / (workers.size() * 2.0));
        }
        else
        {
            // making the assumption that if workers are more than the waypoints provided, n will be
            // equal to 1, to achieve equal load balance between the first (waypoints.size()) workers
            return 1;
        }
    }


}