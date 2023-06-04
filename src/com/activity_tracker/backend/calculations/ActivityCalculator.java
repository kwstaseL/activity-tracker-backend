package com.activity_tracker.backend.calculations;



import com.activity_tracker.backend.parser.Waypoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class used for the mapping phase.
 * It will calculate the distance, time and elevation differences between two waypoints.
 */
public class ActivityCalculator
{
    /**
     * Calculates the time between two waypoints.
     *
     * @param w1 the first waypoint.
     * @param w2 the second waypoint.
     * @return the time in hours between the two waypoints.
     * @throws NullPointerException if either w1 or w2 is null.
     */
    protected static double calculateTime(Waypoint w1, Waypoint w2)
    {
        assert w1 != null && w2 != null;
        final String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime t1 = LocalDateTime.parse(w1.getTimestamp(), formatter);
        LocalDateTime t2 = LocalDateTime.parse(w2.getTimestamp(), formatter);
        Duration duration = Duration.between(t1, t2);
        double timeInSeconds = duration.getSeconds();
        return timeInSeconds / 60.0f;   // we divide by 60 to get the time in minutes
    }

    /**
     * Calculates the distance between two waypoints in meters.
     *
     * @param w1 the first waypoint.
     * @param w2 the second waypoint.
     * @return the distance in meters between the two waypoints.
     * @throws NullPointerException if either w1 or w2 is null.
     */
    public static double calculateDistanceInMeters(Waypoint w1, Waypoint w2)
    {
        assert w1 != null && w2 != null;
        final double RADIUS_OF_EARTH_METERS = 6378100;
        // Convert degrees to radians
        double lat1 = w1.getLatitude();
        double lon1 = w1.getLongitude();
        double lat2 = w2.getLatitude();
        double lon2 = w2.getLongitude();
        lat1 = lat1 * Math.PI / 180.0;
        lon1 = lon1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        lon2 = lon2 * Math.PI / 180.0;
        // radius of earth in metres
        // P
        double rho1 = RADIUS_OF_EARTH_METERS * Math.cos(lat1);
        double z1 = RADIUS_OF_EARTH_METERS * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lon1);
        double y1 = rho1 * Math.sin(lon1);
        // Q
        double rho2 = RADIUS_OF_EARTH_METERS * Math.cos(lat2);
        double z2 = RADIUS_OF_EARTH_METERS * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lon2);
        double y2 = rho2 * Math.sin(lon2);
        // Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (RADIUS_OF_EARTH_METERS * RADIUS_OF_EARTH_METERS);
        final double theta = Math.acos(cos_theta);
        // Distance in Metres
        return RADIUS_OF_EARTH_METERS * theta;
    }

    /**
     * Calculates the distance in kilometers between two waypoints.
     *
     * @param w1 the first waypoint.
     * @param w2 the second waypoint.
     * @return the distance in kilometers between the two waypoints.
     * @throws NullPointerException if either w1 or w2 is null.
     */
    protected static double calculateDistanceInKilometers(Waypoint w1, Waypoint w2)
    {
        assert w1 != null && w2 != null;
        // Converting to Kilometers
        return calculateDistanceInMeters(w1, w2) / 1000;
    }

    /**
     * Calculates the elevation difference between two waypoints.
     *
     * @param w1 the first waypoint.
     * @param w2 the second waypoint.
     * @return the elevation difference in meters between the two waypoints.
     * @throws NullPointerException if either w1 or w2 is null.
     */
    // Calculating the elevation between two waypoints
    protected static double calculateElevation(Waypoint w1, Waypoint w2)
    {
        assert w1 != null && w2 != null;
        // if the elevation of w2 is greater than w1, return the difference between the two elevations
        double elevation1 = w1.getElevation();
        double elevation2 = w2.getElevation();
        return elevation2 > elevation1 ? elevation2 - elevation1 : 0;
    }

}