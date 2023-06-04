package com.activity_tracker.backend.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a segment of a route, containing a list of waypoints and file name.
 */
public class Segment implements Serializable
{
    private final ArrayList<Waypoint> waypoints;
    private final String fileName;
    private final int segmentID;
    private static int segmentIDGenerator = 0;

    /**
     * Constructor for Segment object.
     * @param waypoints List of waypoints in this segment.
     * @param fileName The file name associated with this segment.
     */
    public Segment(ArrayList<Waypoint> waypoints, String fileName)
    {
        this.waypoints = waypoints;
        this.segmentID = segmentIDGenerator++;
        this.fileName = fileName;
    }

    public ArrayList<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    public int getSegmentID()
    {
        return segmentID;
    }

    public String getFileName()
    {
        return fileName;
    }

    /**
     * Overrides the equals method for object comparison.
     * @param o The object to compare to.
     * @return true if objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;

        Segment segment = (Segment) o;
        return segmentID == segment.segmentID && Objects.equals(waypoints, segment.waypoints);
    }
}
