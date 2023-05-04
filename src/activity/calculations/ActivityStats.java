package activity.calculations;

import activity.parser.Segment;
import activity.parser.Waypoint;

import java.io.Serializable;
import java.util.ArrayList;

// Contains the results of the calculations for the activities
// during the map/reduce phase
public class ActivityStats implements Serializable
{
    // Represents the total distance, average speed, total elevation and total time of the activity
    private double distance;
    private double speed;
    private double elevation;
    private double time;
    private final ArrayList<SegmentStats> segmentStatsList;
    private final int routeID;

    // Constructor used for the map/reduce phase for the final results
    public ActivityStats(int routeID, double distance, double speed, double elevation, double time, ArrayList<SegmentStats> segmentStatsList)
    {
        this.routeID = routeID;
        this.distance = distance;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time;
        this.segmentStatsList = new ArrayList<>(segmentStatsList);
    }

    // This constructor is used for calculating and saving the results of the calculations
    public ActivityStats(int routeID)
    {
        this(routeID, 0, 0, 0, 0, new ArrayList<>());
    }

    public void registerSegments(ArrayList<Segment> segments)
    {
        for (Segment segment : segments)
        {
            this.segmentStatsList.add(new SegmentStats(segment.getSegmentID()));
        }
    }

    public void update(Waypoint w1, Waypoint w2)
    {
        this.distance += ActivityCalculator.calculateDistanceInKilometers(w1, w2);
        this.time += ActivityCalculator.calculateTime(w1, w2);
        this.elevation += ActivityCalculator.calculateElevation(w1, w2);
    }

    public void segmentUpdate(Waypoint w1, Waypoint w2, ArrayList<Segment> segments)
    {
        for (Segment segment : segments)
        {
            int segmentID = segment.getSegmentID();

            int segmentStatsIndex = segmentStatsList.indexOf(new SegmentStats(segmentID));

            if (segmentStatsIndex == -1)
            {
                continue;
            }

            SegmentStats segmentStats = segmentStatsList.get(segmentStatsIndex);
            segmentStats.timeUpdate(ActivityCalculator.calculateTime(w1, w2));
        }
    }

    public void finalise()
    {
        this.speed = (time > 0) ? distance / (time / 60.0) : 0.0;
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

    public ArrayList<SegmentStats> getSegmentStatsList()
    {
        return this.segmentStatsList;
    }

    public ArrayList<Integer> getSegmentIDs()
    {
        ArrayList<Integer> segmentIDs = new ArrayList<>();
        for (SegmentStats segmentStats : segmentStatsList)
        {
            segmentIDs.add(segmentStats.getSegmentID());
        }
        return segmentIDs;
    }

    @Override
    public String toString()
    {
        return String.format("Total Distance: %.2f km, Average Speed: %.2f km/h, Total Elevation: %.2f m and Total Time: %.2f minutes",
                distance, speed, elevation, time);
    }
}
