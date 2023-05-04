package activity.mapreduce;

import activity.calculations.ActivityStats;
import activity.calculations.SegmentStats;
import activity.misc.Pair;

import java.util.ArrayList;

public class Reduce
{
    // This is the method that will reduce all the intermediate results received from the workers
    // into a single result that will be sent to the client
    public static ActivityStats reduce(Pair<Integer, ArrayList<ActivityStats>> intermediateResults)
    {
        assert intermediateResults.getValue() != null && intermediateResults.getValue().size()>0;
        ArrayList<ActivityStats> activityStatsList = intermediateResults.getValue();

        double elevation = 0;
        double time = 0;
        double distance = 0;
        double totalSpeed = 0;
        ArrayList<SegmentStats> finalSegmentStats = new ArrayList<>();

        // iterating over the stats returned by each chunk of our route
        for (ActivityStats stats : activityStatsList)
        {
            elevation += stats.getElevation();
            time += stats.getTime();
            distance += stats.getDistance();
            totalSpeed += stats.getSpeed();

            // get the list of segment stats that this chunk contained
            ArrayList<SegmentStats> chunkSegmentStats = stats.getSegmentStatsList();

            // for each segment stats in the list above:
            for (SegmentStats segmentStats : chunkSegmentStats)
            {
                int segmentStatsIndex = finalSegmentStats.indexOf(segmentStats);

                // if these segment stats are not included in our final stats
                if (segmentStatsIndex == -1)
                {
                    finalSegmentStats.add(segmentStats);
                    continue;
                }

                // else (meaning we have already found other stats about this segment), update the total time
                SegmentStats currentSegmentStats = finalSegmentStats.get(segmentStatsIndex);
                currentSegmentStats.timeUpdate(segmentStats.getTime());
            }
        }

        return new ActivityStats(distance, totalSpeed / activityStatsList.size(), elevation, time, finalSegmentStats);
    }
}
