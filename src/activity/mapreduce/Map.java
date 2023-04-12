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

    public synchronized HashMap<String, ActivityStats> map(String clientID, Route route)
    {
        System.out.println("Started mapping phase..");
        HashMap<String, ActivityStats> intermediate_results = new HashMap<>();
        // TODO: For each waypoint in the route calculate the stats
        // TODO: Add the stats to the intermediate_results hashmap

        ArrayList<Waypoint> waypoints = route.waypoints();
        System.out.println("Waypoints size: " + waypoints.size());

        Waypoint w1 = waypoints.get(0);
        double currentHighestElevation = w1.getElevation();   // initialising currentHighestElevation with the first waypoint's elevation
        double totalDistance = 0.0;
        double totalElevation = 0.0;
        double totalTime = 0.0;
        double averageSpeed = 0.0;

        ActivityStats stats = new ActivityStats();
        ActivityStats finalStats = new ActivityStats();

        for (int i = 1; i < waypoints.size(); ++i)
        {
            Waypoint w2 = waypoints.get(i);
            stats = calculator.calculateStats(w1, w2, currentHighestElevation);
            totalDistance += stats.getDistance();
            averageSpeed += stats.getSpeed();
            totalTime += stats.getTime();
            double elevation = stats.getElevation();

            // if (currentElevation > 0): indicates the second waypoint (w2) has a higher elevation
            // than the one we have currently registered as highest.
            // therefore, updating currentHighestElevation
            if (elevation > 0)
            {
                currentHighestElevation = w2.getElevation();
            }
            totalElevation += elevation;
            w1 = waypoints.get(i);
        }

        averageSpeed = (totalTime > 0) ? totalDistance / (totalTime / 60.0) : 0.0;
        finalStats.setDistance(totalDistance);
        finalStats.setSpeed(averageSpeed);
        finalStats.setElevation(totalElevation);
        finalStats.setTime(totalTime);

        System.out.println("Total Distance: " + String.format("%.2f", finalStats.getDistance()) + " km");
        System.out.println("Average Speed: " + String.format("%.2f", finalStats.getSpeed()) + " km/h");
        System.out.println("Total Elevation: " + String.format("%.2f", finalStats.getElevation()) + " m");
        System.out.println("Total Time: " + String.format("%.2f", finalStats.getTime()) + " minutes");
        System.out.println("Finished mapping phase.. for client: " + clientID);

        intermediate_results.put(clientID, finalStats);

        return intermediate_results;
    }

}
