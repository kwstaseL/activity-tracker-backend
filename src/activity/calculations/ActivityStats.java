package activity.calculations;

import java.io.Serializable;

public class ActivityStats implements Serializable
{
    private double distance;
    private double speed;
    private double elevation;
    private double time;
    private boolean flag = false;

    private int routeID;

    // TODO: Cleanup "Flag"

    public ActivityStats(double distance, double speed, double elevation, double time,int routeID)
    {
        this.distance = distance;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time;
        this.routeID = routeID;
    }

    public ActivityStats()
    {
        this(0, 0, 0, 0,-1);
    }

    public ActivityStats(boolean flag, int routeID) {
        this(0, 0, 0, 0, routeID);
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
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

    public void setRouteID(int routeID)
    {
        this.routeID = routeID;
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

    public int getRouteID()
    {
        return routeID;
    }

    public String toString()
    {
        return String.format("Total Distance: %.2f, Average Speed: %.2f, Total Elevation: %.2f and Total Time: %.2f", distance, speed, elevation, time);
    }
}
