package activity.main;

import activity.parser.Waypoint;
import activity.parser.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class WorkDispatcher implements Runnable
{
    private final Queue<WorkerHandler> workers;
    private final Queue<Route> filesToWorker;
    private HashMap<Integer,Boolean> routeStatus;

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

        // if there's more waypoints than workers provided, make n equal to waypoints.size / workers.size
        if (waypoints.size() >= workers.size()) {
            n = waypoints.size() / workers.size();
        } else {
            // making the assumption that if workers are more than the waypoints provided, n will be equal to workers.size / waypoints.size
            n = workers.size() / waypoints.size();
        }

        // expectedChunks: determines how many chunks of waypoints the route will be split into
        int expectedChunks = (int) Math.ceil(waypoints.size() / (double) n);
        ArrayList<Waypoint> chunk = new ArrayList<>();


        for (int i = 0; i < waypoints.size(); i++)
        {
            chunk.add(waypoints.get(i));

            if (chunk.size() == n || i == waypoints.size() - 1)
            {
                WorkerHandler worker = workers.poll();
                assert worker != null;
                Route chunkedRoute = new Route(chunk, routeID, clientID);
                worker.processJob(chunkedRoute);
                // add the worker to the end of the queue
                workers.add(worker);


                if (i != waypoints.size() - 1) {

                    // clear the chunk for the next set of waypoints
                    chunk = new ArrayList<>();
                    // adding the last waypoint from the previous chunk, so we do not miss the connection between i and i+1
                    chunk.add(waypoints.get(i));
                }
            }
        }
        synchronized (routeStatus)
        {
            routeStatus.put(routeID, true);
            System.err.println("Finished chunking up route:" + routeID + ". Total chunks: " + expectedChunks);
        }
    }

}
