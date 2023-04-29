package activity.parser;

import java.util.ArrayList;

public class Segment
{
    private final ArrayList<Waypoint> waypoints;
    private final int segmentID;
    private static int segmentIDGenerator = 0;

    public Segment(ArrayList<Waypoint> waypoints)
    {
        this.waypoints = waypoints;
        segmentID = segmentIDGenerator++;
    }

    public ArrayList<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    public int getSegmentID()
    {
        return segmentID;
    }

}
