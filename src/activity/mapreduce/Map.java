package activity.mapreduce;

import java.util.ArrayList;
import java.util.HashMap;

import activity.calculations.ActivityCalculator;
import activity.calculations.ActivityStats;
import activity.calculations.Waypoint;

import activity.parser.Route;

public class Map
{
    private ActivityCalculator calculator;
    public Map()
    {
        calculator = new ActivityCalculator();
    }

    public HashMap<String, ActivityStats> map(String clientID, Route route)
    {
        System.out.println("Started mapping phase..");
        HashMap<String, ActivityStats> intermediate_results = new HashMap<>();
        // TODO: For each waypoint in the route calculate the stats
        // TODO: Add the stats to the intermediate_results hashmap

        ArrayList<Waypoint> waypoints = route.waypoints();

        Waypoint w1 = waypoints.get(0);
        double currentHighestElevation = w1.getElevation();   // initialising currentHighestElevation with the first waypoint's elevation
        double totalDistance = 0.0;
        double totalElevation = 0.0;
        double totalTime = 0.0;
        double averageSpeed = 0.0;

        ActivityStats stats = null;

        for (int i = 1; i < waypoints.size(); ++i)
        {
            Waypoint w2 = waypoints.get(i);
            stats = calculator.calculateStats(w1, w2, currentHighestElevation);
            totalDistance += stats.getDistance();
            averageSpeed += stats.getSpeed();
            totalTime += stats.getTime();
            double elevation = stats.getElevation();

            // if (currentElevation > 0): indicates the second waypoint (w2) has a higher elevation than the one we have currently registered as highest.
            // therefore, updating currentHighestElevation
            if (elevation > 0)
            {
                currentHighestElevation = w2.getElevation();
            }
            totalElevation += elevation;
            w1 = waypoints.get(i);
        }

        averageSpeed = (totalTime > 0) ? totalDistance / (totalTime / 60.0) : 0.0;

        System.out.println("Total Distance: " + String.format("%.2f", totalDistance) + " km");
        System.out.println("Average Speed: " + String.format("%.2f", averageSpeed) + " km/h");
        System.out.println("Total Elevation: " + String.format("%.2f", totalElevation) + " m");
        System.out.println("Total Time: " + String.format("%.2f", totalTime) + " minutes");

        intermediate_results.put(clientID, stats);

        return intermediate_results;
    }

}
