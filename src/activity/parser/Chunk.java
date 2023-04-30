package activity.parser;

import java.io.Serializable;
import java.util.ArrayList;

/* Chunk: Wrapper class, which contains the ArrayList of Waypoints the chunk is supposed to contain,
 * a variable indicating how many chunks in total the route was split into, and a variable indicating
 * the index of the chunk being currently processed amongst the total chunks                        */

public class Chunk implements Serializable {

    // Route contains the ArrayList of Waypoints, the route id, client id, and the username of the user who recorded the route
    private final Route route;
    // Total chunks is the total number of chunks the route was split into
    private final int totalChunks;
    // Chunk index is the index of the chunk being currently processed amongst the total chunks
    private final int chunkIndex;

    private final ArrayList<Waypoint> waypoints;

    /* segments, segmentStartingIndices, segmentEndingIndices:
     * Three symmetrical arraylists, the index of a starting/ending segment index corresponds to the segment in the according segments index
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

    public void registerSegments()
    {
        route.segmentsInChunk(this);
    }

    public void addSegment(Segment segment, int startingIndex, int endingIndex)
    {

    }

}

