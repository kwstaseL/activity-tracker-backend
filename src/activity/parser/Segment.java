package activity.parser;

import java.util.ArrayList;

public class Segment
{
    private ArrayList<Waypoint> waypoints;
    private int segmentID;
    private static int segmentIDGenerator = 0;

    public Segment()
    {
        waypoints = new ArrayList<>();
        segmentID = segmentIDGenerator++;
    }

    public Segment(ArrayList<Waypoint> waypoints)
    {
        waypoints = waypoints;
        segmentID = segmentIDGenerator++;
    }

    public ArrayList<Waypoint> getWaypoints()
    {
        return waypoints;
    }
}
