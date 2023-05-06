package activity.calculations;

import activity.parser.Segment;
import activity.parser.Waypoint;

import java.io.Serializable;
import java.util.ArrayList;

// Contains the results of the calculations for the activities
// during the map/reduce phase
public class ActivityStats implements Serializable
{
    // Represents the total distance, average speed,
    // total elevation and total time of the activity of a route that the user has uploaded
    private double distance;
    private double speed;
    private double elevation;
    private double time;
    // segmentStatsList: contains the statistics for each segment the route contains
    private final ArrayList<SegmentStats> segmentStatsList;
    // uniqueID: represents the unique ID of the route
    private final int routeID;

    // Constructor used for the map/reduce phase for the final results
    public ActivityStats(int routeID, double distance, double speed, double elevation,
                         double time, ArrayList<SegmentStats> segmentStatsList)
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

    // Used the first time we find a waypoint that is a start of a segment, we create a new segmentStats object
    public void registerSegments(ArrayList<Segment> segments)
    {
        for (Segment segment : segments)
        {
            this.segmentStatsList.add(new SegmentStats(segment.getSegmentID(), segment.getFileName()));
        }
    }
    // Used in the mapping phase to update the stats of the route
    public void updateStats(Waypoint w1, Waypoint w2)
    {
        this.distance += ActivityCalculator.calculateDistanceInKilometers(w1, w2);
        this.time += ActivityCalculator.calculateTime(w1, w2);
        this.elevation += ActivityCalculator.calculateElevation(w1, w2);
    }
    // Used in the mapping phase to update the stats of the segments , segments is the list of
    // segments that contain the waypoint w1 and w2
    public void updateSegmentStats(Waypoint w1, Waypoint w2, ArrayList<Segment> segments)
    {
        for (Segment segment : segments)
        {
            int segmentID = segment.getSegmentID();
            // finds the index of the segmentStats object that has the same segmentID as the segment we are currently looking at
            int segmentStatsIndex = segmentStatsList.indexOf(new SegmentStats(segmentID, segment.getFileName()));
            // if we just found the segmentStats object for the first time we create a new one and continue
            if (segmentStatsIndex == -1)
            {
                continue;
            }
            // if we found the segmentStats object that corresponds to the same segmentID as the segment we are
            // currently looking at we calculate the time and update the segmentStats object
            SegmentStats segmentStats = segmentStatsList.get(segmentStatsIndex);
            segmentStats.timeUpdate(ActivityCalculator.calculateTime(w1, w2));
        }
    }

    public void finaliseStats()
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

    // Returns an arraylist of all the hash codes of the file names of the segments we have calculated the stats for
    public ArrayList<Integer> getSegmentHashes()
    {
        ArrayList<Integer> segmentIDs = new ArrayList<>();
        for (SegmentStats segmentStats : segmentStatsList)
        {
            segmentIDs.add(segmentStats.getFileName().hashCode());
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
