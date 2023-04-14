package activity.calculations;

import activity.parser.Waypoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// DONE

// Class used for the mapping phase
// It will calculate for a chunk of data the distance,time ,speed and elevation
public class ActivityCalculator
{
    // Calculating the distance between two waypoints
    private static double calculateDistance(Waypoint w1, Waypoint w2)
    {
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
        final double r = 6378100;
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
        final double theta = Math.acos(cos_theta);
        // Distance in Metres
        double d_meters = r * theta;
        // Converting to Kilometers
        return d_meters/1000;
    }


    // Calculating the time between two waypoints
    private static double calculateTime(Waypoint w1, Waypoint w2)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime t1 = LocalDateTime.parse(w1.getTimestamp(), formatter);
        LocalDateTime t2 = LocalDateTime.parse(w2.getTimestamp(), formatter);
        Duration duration = Duration.between(t1, t2);
        double timeInSeconds = duration.getSeconds();
        return timeInSeconds / 60.0f;   // /60.0f to get the current time in hours
    }

    // Calculating the speed between two waypoints
    private static double calculateSpeed(double distance, double time)
    {
        return distance / time;
    }

    // Calculating the elevation between two waypoints
    private static double calculateElevation(Waypoint w1, Waypoint w2)
    {
        // if w2 is above the current highest elevation recorded, we return w2.getElevation - currentHighestElevation
        if (w2.getElevation() > w1.getElevation())
        {
            return w2.getElevation() - w1.getElevation();
        }

        // else (which means w2 is below the current highest elevation recorded), we return 0.
        return 0;
    }
    // This is the method that will be called during the mapping phase
    // to calculate the stats for a chunk of data
    public static ActivityStats calculateStats(Waypoint w1, Waypoint w2)
    {
        ActivityStats stats = new ActivityStats();
        double distance = calculateDistance(w1, w2);
        double time = calculateTime(w1, w2);
        double speed = calculateSpeed(distance, time);
        double elevation = calculateElevation(w1, w2);

        stats.setDistance(distance);
        stats.setSpeed(speed);
        stats.setElevation(elevation);
        stats.setTime(time);

        return stats;
    }
}
