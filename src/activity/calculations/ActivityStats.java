package activity.calculations;

import java.io.Serializable;

public class ActivityStats implements Serializable
{
    private double distance;
    private double speed;
    private double elevation;
    private double time;

    public ActivityStats(double distance, double speed, double elevation, double time) {
        this.distance = distance;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time;
    }

    public ActivityStats() {
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

    public String toString()
    {
        return "Total Distance: " + distance + " Average Speed: "
                + speed + " Total Elevation: " + elevation + " Total Time: " + time;
    }
}
