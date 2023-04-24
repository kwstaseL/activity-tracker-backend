package activity.main;

import activity.calculations.ActivityStats;
import activity.misc.Pair;
import activity.parser.Chunk;

import java.util.HashMap;
import java.util.Queue;

public class ResultDispatcher
{
    // This is the lookup table that will map the client id to the appropriate client handler
    private final HashMap<Integer,ClientHandler> clients;
    // This is the queue that will contain the intermediate results calculated by the workers and will be sent to the appropriate client
    private final Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults;

    public ResultDispatcher(HashMap<Integer, ClientHandler> clients,
                            Queue<Pair<Integer, Pair<Chunk, ActivityStats>>> intermediateResults)
    {
        this.clients = clients;
        this.intermediateResults = intermediateResults;
    }

    // This method will handle the intermediate results by sending them to the appropriate client-handler.
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
                        // waiting for the workers to send the intermediate results
                        intermediateResults.wait();
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error while waiting for intermediate results");
                        System.out.println(e.getMessage());
                    }
                }

                // fetching the results from the WorkerHandler
                Pair<Integer, Pair<Chunk, ActivityStats>> activityStatsPair = intermediateResults.poll();


                // extracting the pair associated with the client
                Pair<Chunk, ActivityStats> stats = activityStatsPair.getValue();

                ClientHandler appropriateHandler = clients.get(activityStatsPair.getKey());
                // sending the results to the appropriate client by writing them to shared memory
                appropriateHandler.addStats(stats);
            }
        }
    }
}
