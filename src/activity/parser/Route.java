package activity.parser;

import activity.misc.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Route implements Serializable
{
    // User is the username of the user who recorded the route
    private final String user;
    // Route ID is the unique ID of the route
    private final int routeID;

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

    // checkForSegment: Checks if the segment is part of our route. if it is, adds it to the route's segment arraylist
    public void checkForSegment(Segment segment)
    {
        int segmentIndex = Collections.indexOfSubList(waypoints, segment.getWaypoints());

        // if the segment is a part of our route (indicated by index != -1), add that segment to the route's segments
        if (segmentIndex != -1)
        {
            addSegment(segment, segmentIndex);
        }
    }

    private void addSegment(Segment segment, int index)
    {
        if (!segments.contains(segment))
        {
            segments.add(segment);
            segmentStartingIndices.add(index);
        }
    }

    // getSegmentStartingIndex: If the segment is already registered in a route's segments, returns its starting value. Otherwise, returns -1.
    public int getSegmentStartingIndex(Segment segment)
    {
        if (segments.contains(segment))
        {
            int index = segments.indexOf(segment);
            return segmentStartingIndices.get(index);
        }
        return -1;
    }

    // getChunkStartingIndex: Returns the starting index of a chunk of a route
    public int getChunkStartingIndex(Chunk chunk)
    {
        return Collections.indexOfSubList(waypoints, chunk.getWaypoints());
    }

    // segmentsInChunk: Returns an arraylist of all the segments contained in a chunk, paired with the respective segment's beginning and ending CHUNK index.
    public void segmentsInChunk(Chunk chunk)
    {
        int startingChunkIndex = getChunkStartingIndex(chunk);
        int lastChunkIndex = startingChunkIndex + chunk.getWaypoints().size() - 1;

        // precautionary check to make sure the chunk is in the route
        if (startingChunkIndex < 0)
        {
            throw new RuntimeException("Found a chunk that does not belong to the route it's registered to.");
        }
        ArrayList<Pair<Segment, Pair<Integer, Integer>>> chunkSegments = new ArrayList<>();

        for (Segment segment : segments)
        {
            int startingSegmentIndex = getSegmentStartingIndex(segment);
            int lastSegmentIndex = startingSegmentIndex + segment.getWaypoints().size() - 1;

            // if the segment is either before or after our chunk, continue
            if (startingChunkIndex >= lastSegmentIndex || lastChunkIndex <= startingSegmentIndex)
            {
                continue;
            }



        }
    }

    public ArrayList<Waypoint> getWaypoints()
    {
        return this.waypoints;
    }

    public int getRouteID()
    {
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
