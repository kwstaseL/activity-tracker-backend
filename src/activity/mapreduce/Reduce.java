package activity.mapreduce;

import activity.calculations.ActivityCalculator;
import activity.calculations.ActivityStats;

import java.util.ArrayList;
import java.util.HashMap;

public class Reduce
{
    public synchronized static ActivityStats reduce(Pair<Integer, ArrayList<ActivityStats>> intermediate_results)
    {
        assert intermediate_results.getValue() != null && intermediate_results.getValue().size()>0;
        ArrayList<ActivityStats> activityStats = intermediate_results.getValue();

        int routeID = activityStats.get(0).getRouteID();
        double elevation = 0;
        double time = 0;
        double distance = 0;
        double totalSpeed = 0;

        for (ActivityStats stats : activityStats) {
            elevation += stats.getElevation();
            time += stats.getTime();
            distance += stats.getDistance();
            totalSpeed += stats.getSpeed();
        }

        return new ActivityStats(distance, totalSpeed / activityStats.size(), elevation, time, routeID);


    }
}
