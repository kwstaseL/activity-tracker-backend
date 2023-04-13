package activity.mapreduce;

import java.util.ArrayList;

import activity.calculations.ActivityCalculator;
import activity.calculations.ActivityStats;

import activity.parser.Waypoint;
import activity.parser.Route;
import activity.parser.Chunk;

public class Map
{
    private ActivityCalculator calculator;
    public Map()
    {
        calculator = new ActivityCalculator();
    }
    public synchronized Pair<Integer, Pair<Chunk, ActivityStats>> map(int clientID, Chunk chunk)
    {
        Route route = chunk.getRoute();
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

        Pair<Chunk, ActivityStats> statsPair = new Pair<>(chunk, finalStats);
        return new Pair<Integer, Pair<Chunk, ActivityStats>>(clientID, statsPair);
    }

}
