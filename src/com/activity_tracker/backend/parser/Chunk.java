package com.activity_tracker.backend.parser;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Chunk: Wrapper class, which contains the ArrayList of Waypoints the chunk is supposed to contain,
 * a variable indicating how many chunks in total the route was split into, and a variable indicating
 * the index of the chunk being currently processed amongst the total chunks
 * */
public class Chunk implements Serializable
{

    // route represents the original route this chunk is a part of
    private final Route route;

    // Total chunks is the total number of chunks the route was split into
    private final int totalChunks;

    private final ArrayList<Waypoint> waypoints;

    /* segments, segmentStartingIndices, segmentEndingIndices:
     * Three symmetrical arraylists, the index of a starting/ending segment index corresponds
     * to the segment in the according segments index
     */
    private final ArrayList<Segment> segments;

    private final ArrayList<Integer> segmentStartingIndices;

    private final ArrayList<Integer> segmentEndingIndices;

    /**
     * Constructor for the Chunk class
     * @param waypoints the waypoints the chunk is supposed to contain
     * @param route the route this chunk is a part of
     * @param totalChunks the total number of chunks the route was split into
     */
    public Chunk(ArrayList<Waypoint> waypoints, Route route, int totalChunks)
    {
        this.route = route;
        this.waypoints = new ArrayList<>(waypoints);
        this.totalChunks = totalChunks;
        this.segments = new ArrayList<>();
        this.segmentStartingIndices = new ArrayList<>();
        this.segmentEndingIndices = new ArrayList<>();
        registerSegments();
    }

    public ArrayList<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    public Route getRoute()
    {
        return route;
    }

    public int getTotalChunks()
    {
        return totalChunks;
    }

    /**
     *  Called on the route this chunk belongs to. The route calls
     * addSegment for all the segments that are contained in this chunk.
     * @throws IllegalArgumentException if the method on the route class fails to register the segments,
     * */
    private void registerSegments()
    {
        try
        {
            route.registerSegmentsInChunk(this);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Error while registering segments");
        }
    }

    /**
     * @param waypoint the waypoint to check
     * @return Returns true if the waypoint parameter is the first index of the part of a segment that this chunk contains
     * @throws IllegalArgumentException if the waypoint parameter is null
     */
    public boolean isFirstSegmentIndex(Waypoint waypoint)
    {
        if (waypoint == null)
        {
            throw new IllegalArgumentException("Received a null waypoint");
        }
        return segmentStartingIndices.contains(waypoints.indexOf(waypoint));
    }

    /**
     * @param waypoint the waypoint to check
     * @return Returns true if the given waypoint is contained in a segment in this chunk
     * @throws IllegalArgumentException if the waypoint parameter is null or not contained in this chunk
     */
    public boolean isInsideSegment(Waypoint waypoint)
    {
        if (!waypoints.contains(waypoint) || waypoint == null)
        {
            throw new IllegalArgumentException("Waypoint is not contained in this chunk");
        }

        int indexInChunk = waypoints.indexOf(waypoint);
        for (int i = 0; i < segmentStartingIndices.size(); i++)
        {
            if (indexInChunk > segmentStartingIndices.get(i) && indexInChunk <= segmentEndingIndices.get(i))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the segments starting from the given waypoint.
     * @param waypoint the waypoint to check
     * @return Returns an arraylist of segments starting from the given waypoint
     * @throws IllegalArgumentException if the waypoint parameter is null or not contained in this chunk
     */
    public ArrayList<Segment> getSegmentsStartingFrom(Waypoint waypoint)
    {
        if (!waypoints.contains(waypoint) || waypoint == null)
        {
            throw new IllegalArgumentException("Waypoint is not contained in this chunk");
        }
        ArrayList<Segment> waypointSegments = new ArrayList<>();

        int indexInChunk = waypoints.indexOf(waypoint);
        for (int i = 0; i < segments.size(); i++)
        {
            if (indexInChunk == segmentStartingIndices.get(i))
            {
                waypointSegments.add(segments.get(i));
            }
        }
        return waypointSegments;
    }

    /**
     * @param waypoint the waypoint to check
     * @return Returns the segments containing the given waypoint.
     * @throws IllegalArgumentException if the waypoint parameter is null or not contained in this chunk
     */
    public ArrayList<Segment> getSegmentsContainingWaypoint(Waypoint waypoint)
    {
        if (!waypoints.contains(waypoint) || waypoint == null)
        {
            throw new IllegalArgumentException("Waypoint is not contained in this chunk");
        }
        ArrayList<Segment> waypointSegments = new ArrayList<>();

        int indexInChunk = waypoints.indexOf(waypoint);
        for (int i = 0; i < segments.size(); i++)
        {
            if (indexInChunk > segmentStartingIndices.get(i) && indexInChunk <= segmentEndingIndices.get(i))
            {
                waypointSegments.add(segments.get(i));
            }
        }
        return waypointSegments;
    }

    /**
     * Called by this chunk's route class, adds all the segments
     * and their respective starting/ending indices to the chunk
     * @param segment the segment to add
     * @param startingIndex the starting index of the segment in the route
     * @param endingIndex the ending index of the segment in the route
     */
    protected void addSegment(Segment segment, int startingIndex, int endingIndex)
    {
        segments.add(segment);
        segmentStartingIndices.add(startingIndex);
        segmentEndingIndices.add(endingIndex);
    }

}

