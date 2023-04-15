package activity.calculations;

// UserStatistics: Contains the total distance, elevation and activity time recorded for a specific user. Also keeps track of how many routes they have registered

import java.util.ArrayList;

public class UserStatistics
{
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;
    private ArrayList<ActivityStats> activityArchive;      // TODO: Possibly unnecessary
    private int routes;
    private String user;

    public UserStatistics(double totalDistance, double totalElevation, double totalActivityTime, String user)
    {
        this.totalDistance = totalDistance;
        this.totalElevation = totalElevation;
        this.totalActivityTime = totalActivityTime;
        this.activityArchive = new ArrayList<>();
        this.user = user;
        routes = 0;
    }

    public UserStatistics(String user)
    {
        this(0, 0, 0, user);
    }

    public void registerRoute(ActivityStats stats)
    {
        totalDistance += stats.getDistance();
        totalElevation += stats.getElevation();
        totalActivityTime += stats.getTime();
        activityArchive.add(stats);
        ++routes;
    }

    public double getAverageDistance()
    {
        assert routes >= 1;
        return totalDistance / routes;
    }

    public double getAverageElevation()
    {
        assert routes >= 1;
        return totalElevation / routes;
    }

    public double getAverageActivityTime()
    {
        assert routes >= 1;
        return totalActivityTime / routes;
    }


    public String toString()
    {
        return "Routes recorded for " + user + ": " + routes + ".\n Total Distance: " + totalDistance + " Total Elevation: " + totalElevation + " Total Activity Time: " + totalActivityTime;
    }

}
