package activity.calculations;

import java.io.Serializable;

// Contains the results of the calculations for the activities
// during the map/reduce phase
public class ActivityStats implements Serializable
{
    private double distance;
    private double speed;
    private double elevation;
    private double time;
    private final int routeID;

    // This flag is used as a communication protocol between the master and the worker
    // to alert him that this is the last chunk of data for the current routeID
    // TODO: Remove flag completely
    private boolean flag = false;


    public ActivityStats(double distance, double speed, double elevation, double time,int routeID)
    {
        this.distance = distance;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time;
        this.routeID = routeID;
    }

    // This constructor is used for calculating and saving the results of the calculations
    public ActivityStats()
    {
        this(0, 0, 0, 0, -1);
    }


    // This constructor is used to create the last chunk of data for the current routeID
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
        return String.format("Total Distance: %.2f, Average Speed: %.2f, Total Elevation: %.2f and Total Time: %.2f",
                distance, speed, elevation, time);
    }
}
