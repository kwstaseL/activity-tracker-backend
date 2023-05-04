package activity.parser;

import activity.calculations.ActivityCalculator;

import java.io.Serializable;

public class Waypoint implements Serializable
{
    final private double latitude;
    final private double longitude;
    final private double elevation;
    final private String timestamp;

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


    public String toString()
    {
        return "Latitude: " + latitude + " Longitude: "
                + longitude + " Elevation: " + elevation + " Timestamp: " + timestamp;
    }

    /* equals: Used when calculating segments, and is used to work around a possible GPS drift between two waypoints
     * Making the assumption that two waypoints will be equal if they are at most 5 metres apart.
     */
    @Override
    public boolean equals(Object o)
    {
        final double gpsError = 10;
        if (this == o) return true;
        if (!(o instanceof Waypoint)) return false;

        // Calculates the distance between two waypoints. If the distance is less than 5 metres, the 2 waypoints are considered equal.
        return ActivityCalculator.calculateDistanceInMeters(this,(Waypoint) o) <= gpsError;
    }
}