package activity.main;

public class UserStats
{
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;
    private int routes;

    public UserStats(double totalDistance, double totalElevation, double totalActivityTime)
    {
        this.totalDistance = totalDistance;
        this.totalElevation = totalElevation;
        this.totalActivityTime = totalActivityTime;
    }

    public double getTotalDistance()
    {
        return totalDistance;
    }

    public double getTotalElevation()
    {
        return totalElevation;
    }

    public double getTotalActivityTime()
    {
        return totalActivityTime;
    }


    public String toString()
    {
        return "Total Distance: " + totalDistance + " Total Elevation: " + totalElevation + " Total Activity Time: " + totalActivityTime;
    }

}
