package activity.calculations;

// UserStatistics: Contains the total distance, elevation and activity time recorded for a specific user. Also keeps track of how many routesRecorded they have registered.

import java.io.Serializable;
import java.util.ArrayList;

public class UserStatistics implements Serializable
{
    // TODO: Make files out of user statistics
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;

    // activityArchive: An ArrayList consisting of the detailed stats of every route for the specific user
    private ArrayList<ActivityStats> activityArchive;      // TODO: Possibly unnecessary

    // routesRecorded: A counter for the amount of routes a user has registered. Eager approach to avoid repetitive activityArchive.length calls
    private int routesRecorded;

    // user: Represents the username as entered on the original gpx file
    private String user;

    public UserStatistics(double totalDistance, double totalElevation, double totalActivityTime, String user)
    {
        this.totalDistance = totalDistance;
        this.totalElevation = totalElevation;
        this.totalActivityTime = totalActivityTime;
        this.activityArchive = new ArrayList<>();
        this.user = user;
        routesRecorded = 0;
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
        ++routesRecorded;
    }

    public double getAverageDistance()
    {
        assert routesRecorded >= 1;
        return totalDistance / routesRecorded;
    }

    public double getAverageElevation()
    {
        assert routesRecorded >= 1;
        return totalElevation / routesRecorded;
    }

    public double getAverageActivityTime()
    {
        assert routesRecorded >= 1;
        return totalActivityTime / routesRecorded;
    }

    @Override
    public String toString()
    {
        return String.format("All stats recorded for %s:\nAverage Distance: %.2f km\nAverage Elevation: %.2f m\nAverage Work Out Time: %.2f minutes",
                user,getAverageDistance(), getAverageElevation(), getAverageActivityTime());
    }

}
