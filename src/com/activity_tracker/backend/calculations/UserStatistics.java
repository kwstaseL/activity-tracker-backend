package com.activity_tracker.backend.calculations;

import java.io.Serializable;

// UserStatistics: A class that represents the statistics for a specific user
public class UserStatistics implements Serializable
{
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;

    // routesRecorded: A counter for the amount of routes a user has registered.
    // Eager approach to avoid repetitive activityArchive.length calls
    private int routesRecorded;

    // user: Represents the username as entered on the original gpx file
    private final String user;

    /**
     * Constructs a UserStatistics object with the given user, routesRecorded, totalDistance, totalElevation, and totalActivityTime.
     *
     * @param user the username of the user this UserStatistics is for
     * @param routesRecorded the amount of routes a user has registered
     * @param totalDistance the total distance of all the routes a user has registered
     * @param totalElevation the total elevation of all the routes a user has registered
     * @param totalActivityTime the total activity time of all the routes a user has registered
     */
    public UserStatistics(String user, int routesRecorded, double totalDistance, double totalElevation, double totalActivityTime)
    {
        this.totalDistance = totalDistance;
        this.totalElevation = totalElevation;
        this.totalActivityTime = totalActivityTime;
        this.user = user;
        this.routesRecorded = routesRecorded;
    }


    public UserStatistics(String user)
    {
        this(user,0, 0, 0, 0);
    }

    /**
     * Registers the results of an ActivityStats object for the user.
     *
     * @param stats the ActivityStats object to register
     */
    public void registerRoute(ActivityStats stats)
    {
        totalDistance += stats.getDistance();
        totalElevation += stats.getElevation();
        totalActivityTime += stats.getTime();
        ++routesRecorded;
    }


    /**
     * @return Returns the total distance of all the routes a user has registered.
     */
    public double getTotalDistance()
    {
        return totalDistance;
    }

    /**
     * @return Returns the total elevation of all the routes a user has registered.
     */
    public double getTotalElevation()
    {
        return totalElevation;
    }

    /**
     * @return Returns the total activity time of all the routes a user has registered.
     */
    public double getTotalActivityTime()
    {
        return totalActivityTime;
    }

    /**
     * @return Returns a counter for the amount of routes a user has registered.
     */
    public int getRoutesRecorded()
    {
        return routesRecorded;
    }

    /**
     * @return Returns the average distance of all the routes a user has registered.
     */
    public double getAverageDistance()
    {
        if (routesRecorded == 0)
        {
            return 0.0;
        }
        return totalDistance / routesRecorded;
    }

    /**
     * @return Returns the average elevation of all the routes a user has registered.
     */
    public double getAverageElevation()
    {
        if (routesRecorded == 0)
        {
            return 0.0;
        }
        return totalElevation / routesRecorded;
    }

    /**
     * @return Returns the average activity time of all the routes a user has registered.
     */
    public double getAverageActivityTime()
    {
        if (routesRecorded == 0)
        {
            return 0.0;
        }
        return totalActivityTime / routesRecorded;
    }


    /**
     * @return Returns a representation of the UserStatistics object in a String format.
     */
    @Override
    public String toString()
    {
        return "+--------------------------------------------+\n" +
                String.format("| General Statistics for: %-19s|\n", user) +
                String.format("| Routes Recorded: %-21d     |\n",routesRecorded) +
                "+---------------------+----------+-----------+\n" +
                "| Metric              | Total    | Average   |\n" +
                "+---------------------+----------+-----------+\n" +
                String.format("| Distance (km)       | %8.2f | %8.2f  |\n", totalDistance, getAverageDistance()) +
                String.format("| Elevation (m)       | %8.2f | %8.2f  |\n", totalElevation, getAverageElevation()) +
                String.format("| Workout Time (min)  | %8.2f | %8.2f  |\n", totalActivityTime, getAverageActivityTime()) +
                "+---------------------+----------+-----------+\n";
    }

}
