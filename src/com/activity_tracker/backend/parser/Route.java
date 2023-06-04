package com.activity_tracker.backend.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Route implements Serializable
{
    // User is the username of the user who recorded the route
    private final String user;
    // Route ID is the unique ID of the route
    private final int routeID;
    // Client ID is the ID of the client that uploaded the route
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

    /**
     * Constructor for the Route class
     * @param waypoints  The waypoints that the route contains
     * @param user The username of the user who recorded the route
     * @param fileName The name of the file. Used to move to "processed_gpx" as soon as the Reduce phase is done.
     */
    public Route(ArrayList<Waypoint> waypoints, String user, String fileName)
    {
        this.waypoints = waypoints;
        this.user = user;
        this.routeID = idGenerator++;
        this.fileName = fileName;
        this.segments = new ArrayList<>();
        this.segmentStartingIndices = new ArrayList<>();
    }

    /**
     * Checks if the segment is part of our route. if it is, adds it to the route's segment arraylist
     * @param segment The segment to check
     */
    public void checkForSegment(Segment segment)
    {
        int segmentIndex = Collections.indexOfSubList(waypoints, segment.getWaypoints());

        // if the segment is a part of our route (indicated by index != -1), add that segment to the route's segments
        if (segmentIndex != -1)
        {
            addSegment(segment, segmentIndex);
        }
    }

    /**
     * Adds a segment to the route's segments arraylist
     * @param segment The segment to add
     * @param index The index of the route where the segment begins
     * @throws IllegalArgumentException if segment is null or index is negative
     */
    private void addSegment(Segment segment, int index)
    {
        if (segment == null)
        {
            throw new IllegalArgumentException("Segment cannot be null.");
        }
        if (index < 0)
        {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        if (!segments.contains(segment))
        {
            segments.add(segment);
            segmentStartingIndices.add(index);
        }
    }

    /**
     * @param segment The segment to check
     * @return If the segment is already registered in a route's segments, returns its starting value.
     * Otherwise, returns -1.
     * @throws RuntimeException if the segment is null
     */
    private int getSegmentStartingIndex(Segment segment)
    {
        if (segment == null)
        {
            throw new RuntimeException("Segment is null");
        }
        if (segments.contains(segment))
        {
            int index = segments.indexOf(segment);
            return segmentStartingIndices.get(index);
        }
        return -1;
    }

    /**
     *  By using index of sublist, we can check if a chunk is a part of a route.
     *  If it is, we can get the index of the route where the chunk starts.
     *
     * @param chunk The chunk to check
     * @return Returns the starting index of a chunk of a route
     * @throws RuntimeException if the chunk is not a part of the route
     */
    private int getChunkStartingIndex(Chunk chunk)
    {
        assert chunk != null;
        int index = Collections.indexOfSubList(waypoints, chunk.getWaypoints());
        if (index == -1)
        {
            throw new RuntimeException("Chunk is not a part of the route");
        }
        return index;
    }


    /**
     * Registers all the segments contained in a chunk of this route.
     * @param chunk The chunk to check for segments
     * @throws RuntimeException if the chunk does not belong to the route
     * @throws IllegalArgumentException if the segment is null
     */
    protected void registerSegmentsInChunk(Chunk chunk)
    {
        if (chunk == null)
        {
            throw new IllegalArgumentException("Input chunk cannot be null.");
        }

        // Get the index of the route where the chunk starts
        int chunkStartIndex = getChunkStartingIndex(chunk);

        // Get the index of the route where the chunk ends
        int chunkEndIndex = chunkStartIndex + chunk.getWaypoints().size() - 1;

        // Check if the chunk belongs to the route
        if (chunkStartIndex < 0)
        {
            throw new RuntimeException("Found a chunk that does not belong to the route it's registered to.");
        }

        if (segments.isEmpty())
        {
            return;
        }

        for (Segment segment : segments)
        {
            // Get the index of the route where the segment starts
            int segmentStartIndex = getSegmentStartingIndex(segment);

            // Get the index of the route where the segment ends
            int segmentEndIndex = segmentStartIndex + segment.getWaypoints().size() - 1;

            // If the segment is either before or after our chunk, continue.
            // Making the assumption that if there's only 1 segment waypoint in our chunk, we disregard it, since
            // it will be calculated by the following/previous chunk.
            if (chunkStartIndex >= segmentEndIndex || chunkEndIndex <= segmentStartIndex)
            {
                continue;
            }

            // Get the index of the chunk where the segment starts
            int chunkSegmentStartIndex = Math.max(chunkStartIndex, segmentStartIndex) - chunkStartIndex;

            // Get the index of the chunk where the segment ends
            int chunkSegmentEndIndex = Math.min(chunkEndIndex, segmentEndIndex) - chunkStartIndex;

            chunk.addSegment(segment, chunkSegmentStartIndex, chunkSegmentEndIndex);
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
