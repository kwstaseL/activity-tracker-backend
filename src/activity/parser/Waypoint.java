package activity.parser;

import java.io.Serializable;
import java.util.Objects;

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


    public double distanceTo(Waypoint other)
    {
        // Convert degrees to radians
        double lat1 = this.latitude;
        double lon1 = this.longitude;
        double lat2 = other.latitude;
        double lon2 = other.longitude;
        lat1 = lat1 * Math.PI / 180.0;
        lon1 = lon1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        lon2 = lon2 * Math.PI / 180.0;
        // radius of earth in metres
        double r = 6378100;
        // P
        double rho1 = r * Math.cos(lat1);
        double z1 = r * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lon1);
        double y1 = rho1 * Math.sin(lon1);
        // Q
        double rho2 = r * Math.cos(lat2);
        double z2 = r * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lon2);
        double y2 = rho2 * Math.sin(lon2);
        // Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (r * r);
        double theta = Math.acos(cos_theta);

        return r * theta;
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
        final double gpsError = 5.0;

        if (this == o) return true;
        if (!(o instanceof Waypoint other)) return false;

        // Calculates the distance between two waypoints. If the distance is less than 5 metres, the 2 waypoints are considered equal.
        return this.distanceTo(other) <= gpsError;
    }
}