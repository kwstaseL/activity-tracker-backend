package activity.calculations;

import java.io.Serializable;

// Contains the results of the calculations for the activities
// during the map/reduce phase
public class ActivityStats implements Serializable
{
    // Represents the total distance, average speed, total elevation and total time of the activity
    private double distance;
    private double speed;
    private double elevation;
    private double time;

    // Constructor used for the map/reduce phase for the final results
    public ActivityStats(double distance, double speed, double elevation, double time)
    {
        this.distance = distance;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time;
    }

    // This constructor is used for calculating and saving the results of the calculations
    public ActivityStats()
    {
        this(0, 0, 0, 0);
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    public void setElevation(double elevation)
    {
        this.elevation = elevation;
    }

    public void setTime(double time)
    {
        this.time = time;
    }

    public double getDistance()
    {
        return distance;
    }

    public double getSpeed()
    {
        return speed;
    }

    public double getElevation()
    {
        return elevation;
    }

    public double getTime()
    {
        return time;
    }

    @Override
    public String toString()
    {
        return String.format("Total Distance: %.2fkm, Average Speed: %.2fkm/h, Total Elevation: %.2fm and Total Time: %.2f minutes",
                distance, speed, elevation, time);
    }
}
