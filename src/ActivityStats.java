public class ActivityStats
{
    private double distance;
    private double speed;
    private double elevation;
    private double time;

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
