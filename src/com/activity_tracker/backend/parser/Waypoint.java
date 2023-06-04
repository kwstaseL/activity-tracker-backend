package com.activity_tracker.backend.parser;



import com.activity_tracker.backend.calculations.ActivityCalculator;

import java.io.Serializable;

/**
 * A class representing a geographic location, including latitude, longitude, elevation, and timestamp.
 */
public class Waypoint implements Serializable
{
    final private double latitude;
    final private double longitude;
    final private double elevation;
    final private String timestamp;

    /**
     * Constructs a new Waypoint with the given latitude, longitude, elevation, and timestamp.
     */
    public Waypoint(double latitude, double longitude,
                    double elevation, String timestamp)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.timestamp = timestamp;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public double getElevation()
    {
        return elevation;
    }

    public String getTimestamp()
    {
        return timestamp;
    }


    /**
     * Returns a string representation of this Waypoint, including latitude, longitude, elevation, and timestamp.
     */
    public String toString()
    {
        return "Latitude: " + latitude + " Longitude: "
                + longitude + " Elevation: " + elevation + " Timestamp: " + timestamp;
    }

    /* equals: Used when calculating segments, and is used to work around a possible GPS drift between two waypoints
     * Making the assumption that two waypoints will be equal if they are at most 10 metres apart.           */
    @Override
    public boolean equals(Object o)
    {
        final double GPS_ERROR = 10;
        if (this == o) return true;
        if (!(o instanceof Waypoint)) return false;

        // Calculates the distance between two waypoints. If the distance is less than 10 metres, the 2 waypoints are considered equal.
        return ActivityCalculator.calculateDistanceInMeters(this,(Waypoint) o) <= GPS_ERROR;
    }

}