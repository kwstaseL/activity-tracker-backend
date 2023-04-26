package activity.parser;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;

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

    public boolean containsRoute(Route route)
    {
        ArrayList<Waypoint> segmentWaypoints = getWaypoints();
        ArrayList<Waypoint> routeWaypoints = route.waypoints();
        System.out.println(Collections.indexOfSubList(segmentWaypoints, routeWaypoints));
        return true;
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
