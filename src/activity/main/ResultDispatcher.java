package activity.main;

import activity.calculations.ActivityStats;
import activity.mapreduce.Pair;
import activity.parser.Chunk;

import java.util.HashMap;
import java.util.Queue;

//TODO: handle what happens if a worker is disconnected so send it to the next worker
public class ResultDispatcher
{
    private final HashMap<Integer,ClientHandler> clients;
    // RouteStatus: A hashmap of RouteID and whether it's had all its chunks received or not
    private final Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults;

    public ResultDispatcher(HashMap<Integer, ClientHandler> clients,
                            Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults)
    {
        this.clients = clients;
        this.intermediateResults = intermediateResults;
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
