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

    public boolean containsRoute(Route route)
    {
        ArrayList<Waypoint> segmentWaypoints = getWaypoints();
        ArrayList<Waypoint> routeWaypoints = route.waypoints();

        // Use a sliding window of length equal to the number of segment waypoints
        for (int i = 0; i <= routeWaypoints.size() - segmentWaypoints.size(); i++) {
            // Check if the segment waypoints match the route waypoints starting at index i
            boolean match = true;
            for (int j = 0; j < segmentWaypoints.size(); j++)
            {
                if (!routeWaypoints.get(i + j).equals(segmentWaypoints.get(j)))
                {
                    match = false;
                    break;
                }
            }
            if (match)
            {
                // The segment matches the route
                return true;
            }
        }

        // The segment does not match the route
        return false;
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
