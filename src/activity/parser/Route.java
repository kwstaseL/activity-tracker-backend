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

    private int clientID;

    // Waypoints is the ArrayList of Waypoints that the route contains
    private final ArrayList<Waypoint> waypoints;
    // segmentsContained: An arraylist, containing a pair of segments and the index of the route where the segment begins
    private final ArrayList<Segment> segments;
    // segmentStartingIndices: An arraylist, containing the index of the route where the segment begins
    private final ArrayList<Integer> segmentStartingIndices;

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
    private int getSegmentStartingIndex(Segment segment)
    {
        if (segments.contains(segment))
        {
            int index = segments.indexOf(segment);
            return segmentStartingIndices.get(index);
        }
        return -1;
    }

    // getChunkStartingIndex: Returns the starting index of a chunk of a route
    private int getChunkStartingIndex(Chunk chunk)
    {
        return Collections.indexOfSubList(waypoints, chunk.getWaypoints());
    }

    // segmentsInChunk: Registers all the segments contained in a chunk of this route.
    protected void segmentsInChunk(Chunk chunk)
    {
        assert chunk != null;
        // firstChunkIndex: The index of the route where the chunk starts
        int firstChunkIndex = getChunkStartingIndex(chunk);

        // lastChunkIndex: The index of the route where the chunk ends
        int lastChunkIndex = firstChunkIndex + chunk.getWaypoints().size() - 1;

        // precautionary check to make sure the chunk is in the route (returns -1 if the chunk is not in the route)
        if (firstChunkIndex < 0)
        {
            throw new RuntimeException("Found a chunk that does not belong to the route it's registered to.");
        }

        if (segments.size() == 0)
        {
            return;
        }

        for (Segment segment : segments)
        {
            // firstSegmentIndex: The index of the route where the segment starts
            int firstSegmentIndex = getSegmentStartingIndex(segment);

            // lastSegmentIndex: The index of the route where the segment ends
            int lastSegmentIndex = firstSegmentIndex + segment.getWaypoints().size() - 1;

            /* if the segment is either before or after our chunk, continue.
             *
             * Making the assumption that if there's only 1 segment waypoint in our chunk, we disregard it, since
             * it will be calculated by the following/previous chunk. */
            if (firstChunkIndex >= lastSegmentIndex || lastChunkIndex <= firstSegmentIndex)
            {
                continue;
            }

            // chunkSegmentStartingIndex: The index of the chunk where the segment starts
            int chunkSegmentStartingIndex = Math.max(firstChunkIndex, firstSegmentIndex) - firstChunkIndex;

            // chunkSegmentEndingIndex: The index of the chunk where the segment ends
            int chunkSegmentEndingIndex = Math.min(lastChunkIndex, lastSegmentIndex) - firstChunkIndex;

            chunk.addSegment(segment, chunkSegmentStartingIndex, chunkSegmentEndingIndex);
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

    public String getUser()
    {
        return user;
    }

    public String toString()
    {
        return "Route: " + fileName + " Username: " + user + " Waypoints: " + waypoints.size() + " First waypoint: " + waypoints.get(0);
    }
}
