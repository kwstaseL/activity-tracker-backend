import activity.calculations.Waypoint;
import activity.parser.Route;

import java.util.ArrayList;
import java.util.Queue;

public class WorkDispatcher implements Runnable
{
    private Queue<WorkerHandler> workers;
    private Queue<Route> filesToWorker;

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
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                Route route = filesToWorker.poll();
                ArrayList<Waypoint> waypoints = route.waypoints();
                int routeID = route.getRouteID();
                String clientID = route.getClientID();

                // set n to the desired number of waypoints per chunk
                final int n = 4;

                ArrayList<Waypoint> chunk = new ArrayList<Waypoint>();
                for (int i = 0; i < waypoints.size(); i++)
                {
                    chunk.add(waypoints.get(i));
                    if ((i + 1) % n == 0 || i == waypoints.size() - 1)
                    {
                        WorkerHandler worker = workers.poll();
                        assert worker != null;
                        Route route_ = new Route(chunk, routeID, clientID);
                        System.out.println("WorkerDispatcher: Sending route to workerhandler");
                        worker.processJob(route_);
                        // add the worker to the end of the queue
                        workers.add(worker);
                        // clear the chunk for the next set of waypoints
                        chunk.clear();
                    }
                }
                // Add the worker to the end of the queue
            }
        }
    }

}
