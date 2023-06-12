package com.activity_tracker.backend.calculations;



import com.activity_tracker.backend.parser.Segment;
import com.activity_tracker.backend.parser.Waypoint;

import java.io.Serializable;
import java.util.ArrayList;

// Contains the results of the calculations for the activities
// during the map/reduce phase
public class ActivityStats implements Serializable
{
    // segmentStatsList: contains the statistics for each segment the route contains
    private final ArrayList<SegmentActivityStats> segmentStatsList;
    // uniqueID: represents the unique ID of the route
    private final int routeID;
    // Represents the total distance, average speed,
    // total elevation and total time of the activity of a route that the user has uploaded
    private double distance;
    private double speed;
    private double elevation;
    private double time;

    /**
     * Constructor used for the map/reduce phase for the final results.
     *
     * @param routeID          the unique ID of the route
     * @param distance         the total distance of the route
     * @param speed            the average speed of the route
     * @param elevation        the total elevation of the route
     * @param time             the total time of the route
     * @param segmentStatsList the statistics for each segment of the route
     */
    public ActivityStats(int routeID, double distance, double speed, double elevation,
                         double time, ArrayList<SegmentActivityStats> segmentStatsList) {
        this.routeID = routeID;
        this.distance = distance;
        this.speed = speed;
        this.elevation = elevation;
        this.time = time;
        this.segmentStatsList = new ArrayList<>(segmentStatsList);
    }

    /**
     * Constructor used for calculating and saving the results of the calculations.
     *
     * @param routeID the unique ID of the route
     */
    public ActivityStats(int routeID)
    {
        this(routeID, 0, 0, 0, 0, new ArrayList<>());
    }

    /**
     * Registers the segments statistics for the route
     *
     * @param segments the segments to register
     */
    public void registerSegments(ArrayList<Segment> segments) {
        for (Segment segment : segments)
        {
            this.segmentStatsList.add(new SegmentActivityStats(segment.getSegmentID(), segment.getFileName()));
        }
    }

    /**
     * Updates the stats of the activity with the given waypoints.
     *
     * @param w1 the first waypoint
     * @param w2 the second waypoint
     */
    public void updateStats(Waypoint w1, Waypoint w2)
    {
        this.distance += ActivityCalculator.calculateDistanceInKilometers(w1, w2);
        this.time += ActivityCalculator.calculateTime(w1, w2);
        this.elevation += ActivityCalculator.calculateElevation(w1, w2);
    }

    /**
     * Updates the stats of the segments that contain the given waypoints.
     *
     * @param w1       the first waypoint
     * @param w2       the second waypoint
     * @param segments the segments to update
     */
    public void updateSegmentStats(Waypoint w1, Waypoint w2, ArrayList<Segment> segments)
    {
        for (Segment segment : segments)
        {
            int segmentID = segment.getSegmentID();
            // finds the index of the segmentStats object that has the same segmentID as the segment we
            // are currently looking at
            int segmentStatsIndex = segmentStatsList.indexOf(new SegmentActivityStats(segmentID, segment.getFileName()));
            // if we just found the segmentStats object for the first time we create a new one and continue
            if (segmentStatsIndex == -1)
            {
                continue;
            }
            // if we found the segmentStats object that corresponds to the same segmentID as the segment we are
            // currently looking at we calculate the time and update the segmentStats object
            SegmentActivityStats segmentStats = segmentStatsList.get(segmentStatsIndex);
            segmentStats.updateTime(ActivityCalculator.calculateTime(w1, w2));
        }
    }

    /**
     * Called when registering the last waypoint associated with a chunk; calculates the speed for the respective chunk.
     */
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

    @Deprecated
    public int getRouteID()
    {
        return routeID;
    }

    public ArrayList<SegmentActivityStats> getSegmentStatsList()
    {
        return this.segmentStatsList;
    }

    /**
     * Returns the segmentIDs of the segments that the route contains.
     *
     * @return the segmentIDs of the segments that the route contains
     */
    public ArrayList<Integer> getSegmentHashes()
    {
        ArrayList<Integer> segmentIDs = new ArrayList<>();
        for (SegmentActivityStats segmentStats : segmentStatsList)
        {
            segmentIDs.add(segmentStats.getFileName().hashCode());
        }
        return segmentIDs;
    }

    @Override
    public String toString()
    {
        return "+--------------------------+-----------+\n" +
                "| Metric                   |   Value   |\n" +
                "+--------------------------+-----------+\n" +
                String.format("| Total Distance (km)      | %9.2f |\n", distance) +
                String.format("| Average Speed (km/h)     | %9.2f |\n", speed) +
                String.format("| Total Elevation (m)      | %9.2f |\n", elevation) +
                String.format("| Total Time (min)         | %9.2f |\n", time) +
                "+--------------------------+-----------+\n";
    }




}
