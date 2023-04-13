package activity.mapreduce;

import java.util.ArrayList;
import java.util.HashMap;

import activity.calculations.ActivityCalculator;
import activity.calculations.ActivityStats;
import activity.parser.Waypoint;

import activity.parser.Route;

public class Map
{
    private ActivityCalculator calculator;
    public Map()
    {
        calculator = new ActivityCalculator();
    }
    public synchronized Pair<Integer, ActivityStats> map(int clientID, Route route)
    {
        ArrayList<Waypoint> waypoints = route.waypoints();

        Waypoint w1 = waypoints.get(0);
        double totalDistance = 0.0;
        double totalElevation = 0.0;
        double totalTime = 0.0;
        double averageSpeed = 0.0;

        ActivityStats stats = new ActivityStats();

        for (int i = 1; i < waypoints.size(); ++i)
        {
            Waypoint w2 = waypoints.get(i);
            stats = calculator.calculateStats(w1, w2);
            totalDistance += stats.getDistance();
            averageSpeed += stats.getSpeed();
            totalTime += stats.getTime();
            totalElevation += stats.getElevation();
            w1 = waypoints.get(i);
        }

        averageSpeed = (totalTime > 0) ? totalDistance / (totalTime / 60.0) : 0.0;
        ActivityStats finalStats = new ActivityStats(totalDistance, averageSpeed, totalElevation, totalTime,route.getRouteID());

        Pair<Integer, ActivityStats> pair = new Pair<>(clientID, finalStats);

        return pair;
    }

}
