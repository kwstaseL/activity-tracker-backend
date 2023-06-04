package com.activity_tracker.backend.mapreduce;

import com.activity_tracker.backend.calculations.ActivityStats;
import com.activity_tracker.backend.calculations.SegmentActivityStats;
import com.activity_tracker.backend.misc.Pair;

import java.util.ArrayList;

public class Reduce
{

    /**
     * Reduces all the intermediate results received from the workers into a single result that will be sent to the client.
     * @param intermediateResults the intermediate results to reduce
     * @return the reduced activity statistics
     */
    public static ActivityStats reduce(Pair<Integer, ArrayList<ActivityStats>> intermediateResults)
    {
        if (intermediateResults == null || intermediateResults.getValue() == null)
        {
            throw new IllegalArgumentException("The pair passed as argument to reduce has an invalid value.");
        }

        if (intermediateResults.getValue().isEmpty())
        {
            throw new RuntimeException("The list of stats passed to reduce appears empty.");
        }

        // activityStatsList: The value of the pair. Represents the results for each chunk of the route.
        ArrayList<ActivityStats> activityStatsList = intermediateResults.getValue();

        double elevation = 0;
        double time = 0;
        double distance = 0;
        double totalSpeed = 0;

        int routeID = intermediateResults.getKey();
        // finalSegmentStats: contains the statistics for each segment the route contains
        ArrayList<SegmentActivityStats> finalSegmentStats = new ArrayList<>();

        // iterating over the stats returned by each chunk of our route
        for (ActivityStats stats : activityStatsList)
        {
            // reduce the stats of each chunk into a single result
            elevation += stats.getElevation();
            time += stats.getTime();
            distance += stats.getDistance();
            totalSpeed += stats.getSpeed();

            // get the list of segment stats that this chunk contained
            ArrayList<SegmentActivityStats> chunkSegmentStats = stats.getSegmentStatsList();

            // for each segment stats in the list above:
            for (SegmentActivityStats segmentStats : chunkSegmentStats)
            {
                // check if these segment stats are already included in our final stats (by comparing their segmentID)
                int segmentStatsIndex = finalSegmentStats.indexOf(segmentStats);

                // if these segment stats are not included in our final stats yet, add them
                if (segmentStatsIndex == -1)
                {
                    finalSegmentStats.add(segmentStats);
                    continue;
                }
                // else (meaning we have already found other stats about this segment), update the total time
                SegmentActivityStats currentSegmentStats = finalSegmentStats.get(segmentStatsIndex);
                currentSegmentStats.updateTime(segmentStats.getTime());
            }
        }
        return new ActivityStats(routeID, distance, totalSpeed / activityStatsList.size(), elevation, time, finalSegmentStats);
    }
}
