package com.activity_tracker.backend.main;

import com.activity_tracker.backend.parser.Chunk;
import com.activity_tracker.backend.parser.Route;
import com.activity_tracker.backend.parser.Waypoint;

import java.util.ArrayList;
import java.util.Queue;

/**
 * The WorkDispatcher class is responsible for taking in a route and splitting it into chunks, then sending the chunks to
 * the workers using round-robin.
 */
public class WorkDispatcher implements Runnable
{
    // This is the queue that contains all the workers
    private final Queue<WorkerHandler> workers;
    // This is the queue that contains all the routes that need to be handled
    private final Queue<Route> routeQueue;
    private final Object writeLock = new Object();
    private final Object readLock = new Object();

    /**
     * Constructs a WorkDispatcher object with a list of workers and routes to process.
     *
     * @param workers list of workers to process routes
     * @param routeQueue list of routes to be processed
     */
    public WorkDispatcher(Queue<WorkerHandler> workers, Queue<Route> routeQueue)
    {
        this.workers = workers;
        this.routeQueue = routeQueue;
    }

    /**
     * The run() method handles the processing of routes.
     */
    public void run()
    {
        synchronized (routeQueue)
        {
            while (true)
            {
                while (routeQueue.isEmpty())
                {
                    try
                    {
                        routeQueue.wait();
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                Route route = routeQueue.poll();
                // Create a new thread to handle the route
                // We create a new thread for each route, so that we can handle multiple routes at the same time
                // and because if one route takes a long time to process, we can still process other routes
                // But also to ensure that if one process fails, it does not affect the other processes
                Thread handleRoute = new Thread(() -> handleRoute(route));
                handleRoute.start();
            }
        }
    }

    /**
     * Splits a route into chunks of waypoints and sends each chunk to a worker.
     *
     * @param route the route to be split into chunks.
     */
    private void handleRoute(Route route)
    {
        ArrayList<Waypoint> waypoints = route.getWaypoints();
        final int waypointsSize = waypoints.size();

        // n will represent the chunk size
        final int n = calculateChunkSize(waypointsSize);

        // expectedChunks: determines how many chunks of waypoints the route will be split into
        final int expectedChunks = calculateExpectedChunks(waypointsSize, n);

        ArrayList<Waypoint> waypointChunk = new ArrayList<>();
        int chunks = 0;

        for (int i = 0; i < waypoints.size(); i++)
        {
            Waypoint currentWaypoint = waypoints.get(i);
            waypointChunk.add(currentWaypoint);

            // First condition: Turns true when the first chunk is full.
            if (waypointChunk.size() == n && chunks == 0)
            {
                chunks++;
                createChunk(route, waypointChunk, expectedChunks);

                if (i != waypoints.size() - 1)
                {
                    // clear the chunk for the next set of waypoints
                    waypointChunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    waypointChunk.add(waypoints.get(i));
                }

            } // Second condition: Turns true when we reach the end of the waypoints to be processed, at which point we
            // assign the chunk as is to a worker to process
            else if (i == waypoints.size() - 1)
            {
                chunks++;
                createChunk(route, waypointChunk, expectedChunks);

            } // Third condition: Turns true when a chunk after the first is full. Size limit is n+1, since it needs
            // to hold the last waypoint of the previous chunk
            else if (waypointChunk.size() == n + 1 && chunks != 0)
            {
                chunks++;
                createChunk(route, waypointChunk, expectedChunks);

                if (i != waypoints.size() - 1)
                {
                    // clear the chunk for the next set of waypoints
                    waypointChunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    waypointChunk.add(currentWaypoint);
                }
            }
        }
    }

    /**
     * Calculates the chunk size based on the number of waypoints and the number of workers.
     *
     * @param waypointsSize the number of waypoints in the route
     * @return the chunk size
     */
    private int calculateChunkSize(int waypointsSize)
    {
        // if there's more waypoints than workers provided, make n equal to waypoints.size / workers.size * 2
        synchronized (readLock)
        {
            if (waypointsSize >= workers.size())
            {
                return (int) Math.ceil(waypointsSize / (workers.size() * 2.0));
            }
            else
            {
                // making the assumption that if workers are more than the waypoints provided, n will be
                // equal to 1, to achieve equal load balance between the first (waypoints.size()) workers
                return 1;
            }
        }
    }

    /**
     * Calculates the expected number of chunks based on the number of waypoints and the chunk size.
     *
     * @param waypointsSize the number of waypoints in the route
     * @param n the size of each chunk
     * @return the expected number of chunks
     */
    private int calculateExpectedChunks(int waypointsSize, int n)
    {
        return (int) Math.ceil(waypointsSize / (double) n);
    }

    /**
     * Creates the chunk and sends it to a worker to process.
     * @param route the route that the chunk belongs to
     * @param chunkWaypoints the waypoints that belong to the chunk
     * @param expectedChunks the expected number of chunks
     *
     */
    private void createChunk(Route route, ArrayList<Waypoint> chunkWaypoints, int expectedChunks)
    {
        Chunk chunk = new Chunk(chunkWaypoints, route, expectedChunks);

        synchronized (writeLock)
        {
            WorkerHandler worker = workers.poll();

            if (worker == null)
            {
                throw new RuntimeException("Tried to poll from an empty worker queue.");
            }

            worker.processJob(chunk);
            // adding the worker to the end of the queue
            workers.add(worker);
        }
    }

}