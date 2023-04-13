package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;

import java.util.HashMap;
import java.util.Queue;

public class ResultDispatcher
{
    private final HashMap<String,ClientHandler> clients;
    private final HashMap<Integer,Boolean> routeStatus;
    private static HashMap<Integer,Integer> chunksPerRoute;
    private final Queue<Pair<String, ActivityStats>> intermediateResults;

    public ResultDispatcher(HashMap<String, ClientHandler> clients, HashMap<Integer, Boolean> routeStatus,
                                     Queue<Pair<String, ActivityStats>> intermediateResults , HashMap<Integer,Integer> chunksPerRoute)
    {
        this.clients = clients;
        this.routeStatus = routeStatus;
        this.intermediateResults = intermediateResults;
        this.chunksPerRoute = chunksPerRoute;
    }

    public void handleResults()
    {
        synchronized (intermediateResults)
        {
            while (true)
            {
                while (intermediateResults.isEmpty())
                {
                    try
                    {
                        intermediateResults.wait();
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                Pair<String, ActivityStats> activityStatsPair = intermediateResults.poll();
                ActivityStats stats = activityStatsPair.getValue();
                int routeID = stats.getRouteID();

                synchronized (chunksPerRoute)
                {
                    if (chunksPerRoute.containsKey(routeID)) {

                        int count = chunksPerRoute.get(routeID);
                        count--;
                        chunksPerRoute.put(routeID, count);

                        String clientID = activityStatsPair.getKey();
                        ClientHandler appropriateHandler = clients.get(clientID);

                        if (chunksPerRoute.get(routeID) > 0)
                        {
                            appropriateHandler.addStats(stats);
                        }
                        else if (chunksPerRoute.get(routeID) == 0)
                        {
                            appropriateHandler.addStats(stats);

                            // If the Work Dispatcher hasn't finished processing all chunks for this route
                            // Then continue getting chunks from the Work Dispatcher
                            // Else send the final result to the client
                            synchronized (routeStatus)
                            {
                                System.err.println("Route Status: " + routeStatus + " for route " + routeID);
                                if (!routeStatus.containsKey(routeID))
                                {
                                    int chunks = chunksPerRoute.get(routeID);
                                    chunks++;
                                    chunksPerRoute.put(routeID, chunks);
                                }
                                else
                                {
                                    appropriateHandler.addStats(new ActivityStats(true,routeID));
                                    chunksPerRoute.remove(routeID);
                                    System.err.println("WorkerHandler: All chunks for route " + routeID + " have been processed");
                                }
                            }


                        }
                    } else {
                        System.err.println("Tried to access route that its chunks have already been processed.");
                    }

                }
            }
        }

    }


}
