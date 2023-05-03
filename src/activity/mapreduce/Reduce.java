package activity.mapreduce;

import activity.calculations.ActivityStats;
import activity.misc.Pair;

import java.util.ArrayList;

public class Reduce
{
    // This is the method that will reduce all the intermediate results received from the workers
    // into a single result that will be sent to the client
    // TODO: This method does not need to be synchronized? its not accessing any shared resources
    public static ActivityStats reduce(Pair<Integer, ArrayList<ActivityStats>> intermediateResults)
    {
        assert intermediateResults.getValue() != null && intermediateResults.getValue().size()>0;
        ArrayList<ActivityStats> activityStats = intermediateResults.getValue();

        double elevation = 0;
        double time = 0;
        double distance = 0;
        double totalSpeed = 0;

        for (ActivityStats stats : activityStats)
        {
            elevation += stats.getElevation();
            time += stats.getTime();
            distance += stats.getDistance();
            totalSpeed += stats.getSpeed();
        }

        return new ActivityStats(distance, totalSpeed / activityStats.size(), elevation, time);
    }
}
