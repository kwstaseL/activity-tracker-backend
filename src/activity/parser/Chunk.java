package activity.parser;

import java.io.Serializable;
import java.util.ArrayList;

/* Chunk: Wrapper class, which contains the ArrayList of Waypoints the chunk is supposed to contain,
 * a variable indicating how many chunks in total the route was split into, and a variable indicating
 * the index of the chunk being currently processed amongst the total chunks                        */

public class Chunk implements Serializable {

    // route represents the original route this chunk is a part of
    private final Route route;

    // Total chunks is the total number of chunks the route was split into
    private final int totalChunks;

    // Chunk index is the index of the chunk being currently processed amongst the total chunks
    private final int chunkIndex;

    private final ArrayList<Waypoint> waypoints;

    /* segments, segmentStartingIndices, segmentEndingIndices:
     * Three symmetrical arraylists, the index of a starting/ending segment index corresponds
     * to the segment in the according segments index
     */
    private final ArrayList<Segment> segments;

    private final ArrayList<Integer> segmentStartingIndices;

    private final ArrayList<Integer> segmentEndingIndices;

    public Chunk(ArrayList<Waypoint> waypoints, Route route, int totalChunks, int chunkIndex)
    {
        this.route = route;
        this.waypoints = new ArrayList<>(waypoints);
        this.totalChunks = totalChunks;
        this.chunkIndex = chunkIndex;
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

    public int getChunkIndex()
    {
        return chunkIndex;
    }

    /* registerSegments: Called on the route this chunk belongs to. The route calls
     * addSegment for all the segments that are contained in this chunk. */
    private void registerSegments()
    {
        route.segmentsInChunk(this);
    }

    // isFirstIndex: Returns true if the waypoint parameter is the first index of a chunk of a segment this chunk contains
    public boolean isFirstIndex(Waypoint waypoint)
    {
        int indexInChunk = waypoints.indexOf(waypoint);
        return segmentStartingIndices.contains(indexInChunk);
    }

    // isLastIndex: Returns true if the waypoint parameter is the last index of a chunk of a segment this chunk contains
    public boolean isLastIndex(Waypoint waypoint)
    {
        int indexInChunk = waypoints.indexOf(waypoint);
        return segmentEndingIndices.contains(indexInChunk);
    }

    // isContainedInSegment: Returns true if the waypoint parameter is contained in a segment this chunk holds.
    public boolean isContainedInSegment(Waypoint waypoint)
    {
        int indexInChunk = waypoints.indexOf(waypoint);
        for (int i = 0; i < segmentStartingIndices.size(); i++)
        {
            if (segmentStartingIndices.get(i) < indexInChunk && indexInChunk < segmentEndingIndices.get(i))
            {
                return true;
            }
        }
        return false;
    }


    /* addSegment: Called by this chunk's route class, adds all the segments
     * and their respective starting/ending indices to the chunk    */
    protected void addSegment(Segment segment, int startingIndex, int endingIndex)
    {
        segments.add(segment);
        segmentStartingIndices.add(startingIndex);
        segmentEndingIndices.add(endingIndex);
    }

}

