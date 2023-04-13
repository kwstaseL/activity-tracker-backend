package activity.main;

import activity.parser.Chunk;
import activity.parser.Waypoint;
import activity.parser.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class WorkDispatcher implements Runnable
{
    private final Queue<WorkerHandler> workers;
    private final Queue<Route> filesToWorker;
    private final HashMap<Integer,Boolean> routeStatus;

    public WorkDispatcher(Queue<WorkerHandler> workers, Queue<Route> filesToWorker,HashMap<Integer,Boolean> routeStatus)
    {
        this.workers = workers;
        this.filesToWorker = filesToWorker;
        this.routeStatus = routeStatus;
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
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                Route route = filesToWorker.poll();
                handleRoute(route);

            }
        }
    }
    private void handleRoute(Route route)
    {
        ArrayList<Waypoint> waypoints = route.waypoints();

        int routeID = route.getRouteID();
        int clientID = route.getClientID();

        // n will represent the chunk size
        int n;

        // if there's more waypoints than workers provided, make n equal to waypoints.size / workers.size * 2
        if (waypoints.size() >= workers.size()) {
            n = (int) Math.ceil(waypoints.size() / (workers.size() * 2.0));
        } else {
            // making the assumption that if workers are more than the waypoints provided, n will be
            // equal to 1, to achieve equal load balance between the first (waypoints.size()) workers
            n = 1;
        }

        // expectedChunks: determines how many chunks of waypoints the route will be split into
        int expectedChunks = (int) Math.ceil(waypoints.size() / (double) n);

        ArrayList<Waypoint> waypointChunk = new ArrayList<>();
        int chunkIndex = 0;
        int chunks = 0;

        for (int i = 0; i < waypoints.size(); i++)
        {
            waypointChunk.add(waypoints.get(i));

            if (waypointChunk.size() == n && chunks == 0)
            {
                WorkerHandler worker = workers.poll();
                assert worker != null;

                chunks++;
                chunkIndex++;

                Route chunkedRoute = new Route(waypointChunk, routeID, clientID);
                Chunk chunk = new Chunk(chunkedRoute, expectedChunks, chunkIndex);

                // TODO: Make WorkerHandlers process Chunks instead of Routes
                worker.processJob(chunk);
                // add the worker to the end of the queue
                workers.add(worker);

                if (i != waypoints.size() - 1) {

                    // clear the chunk for the next set of waypoints
                    waypointChunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    waypointChunk.add(waypoints.get(i));
                }
            } else if (i == waypoints.size() - 1) {
                WorkerHandler worker = workers.poll();
                assert worker != null;

                chunks++;
                chunkIndex++;

                Route chunkedRoute = new Route(waypointChunk, routeID, clientID);
                Chunk chunk = new Chunk(chunkedRoute, expectedChunks, chunkIndex);

                // TODO: Make WorkerHandlers process Chunks instead of Routes
                worker.processJob(chunk);
                // add the worker to the end of the queue
                workers.add(worker);
            } else if (waypointChunk.size() == n+1 && chunks != 0) {
                WorkerHandler worker = workers.poll();
                assert worker != null;

                chunks++;
                chunkIndex++;

                Route chunkedRoute = new Route(waypointChunk, routeID, clientID);
                Chunk chunk = new Chunk(chunkedRoute, expectedChunks, chunkIndex);

                // TODO: Make WorkerHandlers process Chunks instead of Routes
                worker.processJob(chunk);
                // add the worker to the end of the queue
                workers.add(worker);

                if (i != waypoints.size() - 1) {

                    // clear the chunk for the next set of waypoints
                    waypointChunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    waypointChunk.add(waypoints.get(i));
                }
            }

        }

        synchronized (routeStatus)
        {
            System.err.println("Finished chunking up route: " + routeID + ". Total chunks: " + chunks);
            System.err.println("Expected chunks were: " + expectedChunks);
        }
    }
}
