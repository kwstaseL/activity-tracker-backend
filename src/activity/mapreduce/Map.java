package activity.mapreduce;

import java.util.ArrayList;

import activity.calculations.ActivityStats;

import activity.misc.Pair;
import activity.parser.Segment;
import activity.parser.Waypoint;
import activity.parser.Chunk;

public class Map
{
    // This is the method that will get called by the worker to map a chunk to a pair of clientID and activity stats
    // The clientID is used to identify the client that requested the activity stats
    // The activity stats are the result of the map operation for that chunk.
    public static Pair<Integer, Pair<Chunk, ActivityStats>> map(int clientID, Chunk chunk)
    {
        ArrayList<Waypoint> waypoints = chunk.getWaypoints();
        ActivityStats stats = new ActivityStats(chunk.getRoute().getRouteID());

        Waypoint w1 = waypoints.get(0);

        // checking if the first chunk waypoint is also the first waypoint of a part of a segment this chunk contains
        if (chunk.isFirstSegmentIndex(w1))
        {
            ArrayList<Segment> segments = chunk.getSegmentsStartingFrom(w1);
            stats.registerSegments(segments);
        }
        
        for (int i = 1; i < waypoints.size(); ++i)
        {
            Waypoint w2 = waypoints.get(i);

            // first, get the segments starting from this waypoint, and register them to the ActivityStats instance
            if (chunk.isFirstSegmentIndex(w2))
            {
                ArrayList<Segment> segments = chunk.getSegmentsStartingFrom(w2);
                stats.registerSegments(segments);
            }

            // then, if this waypoint is inside any of the chunk segments, calculate accordingly
            if (chunk.isInsideSegment(w2))
            {
                ArrayList<Segment> segments = chunk.getSegmentsContainingWaypoint(w2);
                stats.updateSegmentStats(w1, w2, segments);
            }

            stats.updateStats(w1, w2);
            w1 = waypoints.get(i);
        }
        stats.finaliseStats();

        // statsPair: Represents a pair of chunk and the activity stats calculated for it
        Pair<Chunk, ActivityStats> statsPair = new Pair<>(chunk, stats);

        return new Pair<>(clientID, statsPair);
    }

}