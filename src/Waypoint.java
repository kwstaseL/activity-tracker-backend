import java.io.Serializable;
import java.time.LocalDateTime;

// A class that
public class Waypoint implements Serializable
{
    final private double latitude;
    final private double longitude;
    final private double elevation;
    final private String timestamp;
    final private String username;

    public Waypoint(double latitude, double longitude,
                    double elevation, String timestamp,String username)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.timestamp = timestamp;
        this.username = username;
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
                + longitude + " Elevation: " + elevation + " Timestamp: " + timestamp + " Username: " + username;
    }


}