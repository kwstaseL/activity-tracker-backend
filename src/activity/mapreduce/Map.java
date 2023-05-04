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
            ArrayList<Segment> segments = chunk.waypointSegments(w1);
            stats.registerSegments(segments);
        }

        for (int i = 1; i < waypoints.size(); ++i)
        {
            Waypoint w2 = waypoints.get(i);

            if (chunk.isFirstSegmentIndex(w2))
            {
                ArrayList<Segment> segments = chunk.waypointSegments(w2);
                stats.registerSegments(segments);
            }
            else if (chunk.isContainedInSegment(w2))
            {
                ArrayList<Segment> segments = chunk.waypointSegments(w2);
                stats.segmentUpdate(w1, w2, segments);
            }

            stats.update(w1, w2);
            w1 = waypoints.get(i);
        }
        stats.finalise();

        // statsPair: Represents a pair of chunk and the activity stats calculated for it
        Pair<Chunk, ActivityStats> statsPair = new Pair<>(chunk, stats);

        return new Pair<>(clientID, statsPair);
    }

}