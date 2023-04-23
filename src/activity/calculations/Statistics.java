package activity.calculations;

import activity.mapreduce.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/* Statistics: The class that will be in charge of handling all the related statistics.
 * Will maintain a hashmap of users-UserStatistics, a counter of the routes recorded, an archive of
 * the stats recorded, as well as the total distance, elevation and activity time across all users.
 */
public class Statistics implements Serializable
{

    // userStats: A hashmap matching each user to their respective statistics.
    private HashMap<String, UserStatistics> userStats = new HashMap<>();

    // activityArchive: An ArrayList consisting of the detailed stats of every route in the system
    private ArrayList<Pair<String, ActivityStats>> activityArchive = new ArrayList<>();     // TODO: Possibly unnecessary?
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

        System.out.println("Statistics across all users so far: ");
    }

    // getUserStats: Returns the UserStatistics object associated with a specific user.
    public UserStatistics getUserStats(String user)
    {
        if (!userStats.containsKey(user)) {
            throw new RuntimeException("User has not been registered.");
        }
        return userStats.get(user);
    }

    public Statistics getGlobalStats()
    {
        return this;
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

    @Override
    public String toString()
    {
        return String.format("Statistics across all users:\nAverage Distance: %.2f km\nAverage Elevation: %.2f m\nAverage Work Out Time: %.2f minutes",
                getAverageDistance(), getAverageElevation(), getAverageActivityTime());
    }

}
