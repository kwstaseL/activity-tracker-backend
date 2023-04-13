package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.Chunk;

import java.util.HashMap;
import java.util.Queue;

public class ResultDispatcher
{
    private final HashMap<Integer,ClientHandler> clients;
    // RouteStatus: A hashmap of RouteID and whether it's had all its chunks received or not
    private final HashMap<Integer,Boolean> routeStatus;
    private static HashMap<Integer,Integer> chunksPerRoute;
    private final Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults;

    public ResultDispatcher(HashMap<Integer, ClientHandler> clients, HashMap<Integer, Boolean> routeStatus,
                            Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults , HashMap<Integer,Integer> chunksPerRoute)
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

                // fetching the results from the WorkerHandler
                Pair<Integer, Pair<Chunk, ActivityStats>> activityStatsPair = intermediateResults.poll();

                // extracting the pair associated with the client
                Pair<Chunk, ActivityStats> stats = activityStatsPair.getValue();

                ClientHandler appropriateHandler = clients.get(activityStatsPair.getKey());
                appropriateHandler.addStats(stats);
            }
        }

    }


}
