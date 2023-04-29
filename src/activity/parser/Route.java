package activity.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Route implements Serializable
{
    // User is the username of the user who recorded the route
    private final String user;
    // Route ID is the unique ID of the route
    private final int routeID;
    // Client ID is the unique ID of the client-handler who sent the route
    private int clientID;
    // Waypoints is the ArrayList of Waypoints that the route contains
    private final ArrayList<Waypoint> waypoints;
    // segmentsContained: An arraylist, containing a pair of segments and the index of the route where the segment begins
    private ArrayList<Segment> segments;
    // segmentStartingIndices: An arraylist, containing the index of the route where the segment begins
    private ArrayList<Integer> segmentStartingIndices;

    // Route file name: The name of the file. Used to move to "processed_gpx" as soon as the Reduce phase is done.
    private final String fileName;

    // ID generator is a static variable that is used to generate unique IDs for each route
    private static int idGenerator = 0;

    public Route(ArrayList<Waypoint> waypoints, String user, String fileName)
    {
        this.waypoints = waypoints;
        this.user = user;
        this.routeID = idGenerator++;
        this.fileName = fileName;
        this.segments = new ArrayList<>();
        this.segmentStartingIndices = new ArrayList<>();
    }
    public Route(ArrayList<Waypoint> waypoints, int routeID, int clientID, String user, String fileName)
    {
        this.waypoints = waypoints;
        this.routeID = routeID;
        this.clientID = clientID;
        this.user = user;
        this.fileName = fileName;
    }
    public boolean containsSegment(Segment segment)
    {
        int segmentIndex = Collections.indexOfSubList(waypoints, segment.getWaypoints());

        if (segmentIndex == -1)
        {
            return false;
        }

        addSegment(segment, segmentIndex);
        return true;
    }

    // chunkIndex: Returns the starting index of a chunk of a route
    public int getChunkStartingIndex(Chunk chunk)
    {
        return Collections.indexOfSubList(waypoints, chunk.getRoute().getWaypoints());
    }

    private void addSegment(Segment segment, int index)
    {
        if (!segments.contains(segment))
        {
            segments.add(segment);
            segmentStartingIndices.add(index);
        }
    }

    public int getSegmentStartingIndex(Segment segment)
    {
        if (segments.contains(segment))
        {
            int index = segments.indexOf(segment);
            return segmentStartingIndices.get(index);
        }
        return -1;
    }

    public ArrayList<Waypoint> getWaypoints() {
        return this.waypoints;
    }

    public int getRouteID() {
        return routeID;
    }

    public int getClientID()
    {
        return clientID;
    }

    public void setClientID(int clientID)
    {
        this.clientID = clientID;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getUser() {
        return user;
    }

    public String toString()
    {
        return "Route: " + fileName + " Username: " + user + " Waypoints: " + waypoints.size() + " First waypoint: " + waypoints.get(0);
    }
}
