package activity.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Segment implements Serializable
{
    private final ArrayList<Waypoint> waypoints;
    private final String fileName;
    private final int segmentID;
    private static int segmentIDGenerator = 0;

    public Segment(ArrayList<Waypoint> waypoints, String fileName)
    {
        this.waypoints = waypoints;
        segmentID = segmentIDGenerator++;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Segment segment)) return false;
        return segmentID == segment.segmentID && Objects.equals(waypoints, segment.waypoints);
    }
}
