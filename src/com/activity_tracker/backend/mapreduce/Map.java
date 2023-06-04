package com.activity_tracker.backend.mapreduce;

import com.activity_tracker.backend.calculations.ActivityStats;
import com.activity_tracker.backend.misc.Pair;
import com.activity_tracker.backend.parser.Chunk;
import com.activity_tracker.backend.parser.Waypoint;

import java.util.ArrayList;

public class Map
{

    /**
     * Maps a chunk to a pair of client ID and activity stats.
     *
     * @param clientID the ID of the client that requested the activity stats.
     * @param chunk the chunk to be mapped.
     * @return a pair of client ID and the chunk's activity stats.
     */
    public static Pair<Integer, Pair<Chunk, ActivityStats>> map(int clientID, Chunk chunk)
    {
        // Get the waypoints from the chunk
        ArrayList<Waypoint> waypoints = chunk.getWaypoints();

        // create a new ActivityStats instance with the route ID of the chunk
        ActivityStats stats = new ActivityStats(chunk.getRoute().getRouteID());

        // register the segments starting from the first waypoint of the chunk, if it is also the first waypoint of a segment
        Waypoint previousWaypoint = waypoints.get(0);
        if (chunk.isFirstSegmentIndex(previousWaypoint))
        {
            stats.registerSegments(chunk.getSegmentsStartingFrom(previousWaypoint));
        }

        // iterate over the waypoints and update the activity stats accordingly
        for (Waypoint currentWaypoint : waypoints.subList(1, waypoints.size()))
        {
            // Register the segments starting from the current waypoint, if it is also the first waypoint of a segment
            if (chunk.isFirstSegmentIndex(currentWaypoint))
            {
                stats.registerSegments(chunk.getSegmentsStartingFrom(currentWaypoint));
            }

            // If the current waypoint is inside any of the chunk segments, update the segment stats
            if (chunk.isInsideSegment(currentWaypoint))
            {
                stats.updateSegmentStats(previousWaypoint, currentWaypoint, chunk.getSegmentsContainingWaypoint(currentWaypoint));
            }

            // Update the activity stats
            stats.updateStats(previousWaypoint, currentWaypoint);
            previousWaypoint = currentWaypoint;
        }

        // Finalize the activity stats
        stats.finaliseStats();

        // Return a pair of client ID and the chunk's activity stats
        return new Pair<>(clientID, new Pair<>(chunk, stats));
    }
}