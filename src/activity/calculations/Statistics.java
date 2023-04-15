package activity.calculations;

import activity.mapreduce.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/* Statistics: The class that will be in charge of handling all the related statistics.
 * Will maintain a hashmap of users-UserStatistics, a counter of the routes recorded, an archive of
 * the stats recorded, as well as the total distance, elevation and activity time across all users.
 */

public class Statistics {
    private HashMap<String, UserStatistics> userStats = new HashMap<>();
    private ArrayList<Pair<String, ActivityStats>> activityArchive = new ArrayList<>();
    private int routesRecorded = 0;
    private double totalDistance = 0;
    private double totalElevation = 0;
    private double totalActivityTime = 0;
    public void registerRoute(String user, ActivityStats activityStats)
    {
        // first, updating the user specific stats
        if (!userStats.containsKey(user)) {
            userStats.put(user, new UserStatistics(user));
        }
        userStats.get(user).registerRoute(activityStats);

        // then, updating the total stats
        totalDistance += activityStats.getDistance();
        totalElevation += activityStats.getElevation();
        totalActivityTime += activityStats.getTime();
        activityArchive.add(new Pair<>(user, activityStats));
        ++routesRecorded;
    }

    // getAverageDistanceForUser: Calculates the average distance for a user by dividing their total distance with the # of routes they have recorded.
    public double getAverageDistanceForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageDistance();
    }

    // getAverageElevationForUser: Similarly to getAverageDistanceForUser
    public double getAverageElevationForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageElevation();
    }

    // getAverageActivityTimeForUser: Similarly to getAverageDistanceForUser
    public double getAverageActivityTimeForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageActivityTime();
    }

    // getAverageDistance: Calculates the average distance recorded across all routes.
    public double getAverageDistance()
    {
        if (routesRecorded == 0)
        {
            throw new RuntimeException("No routes have been recorded yet.");
        }
        return totalDistance / routesRecorded;
    }

    // getAverageElevation: Similarly to getAverageDistance
    public double getAverageElevation()
    {
        if (routesRecorded == 0)
        {
            throw new RuntimeException("No routes have been recorded yet.");
        }
        return totalElevation / routesRecorded;
    }

    // getAverageDistance: Similarly to getAverageDistance
    public double getAverageActivityTime()
    {
        if (routesRecorded == 0)
        {
            throw new RuntimeException("No routes have been recorded yet.");
        }
        return totalActivityTime / routesRecorded;
    }

}
