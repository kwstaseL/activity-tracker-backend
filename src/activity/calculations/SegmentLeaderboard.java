package activity.calculations;

import java.io.Serializable;
import java.util.TreeSet;

public class SegmentLeaderboard implements Serializable
{
    private final TreeSet<UserSegmentStatistics> statistics;
    private final int segmentID;

    public SegmentLeaderboard(int segmentID)
    {
        this.statistics = new TreeSet<>();
        this.segmentID = segmentID;
    }

    public void registerSegmentStatistics(UserSegmentStatistics userSegmentStatistics)
    {
        this.statistics.add(userSegmentStatistics);
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder(String.format("Found a segment!%n Leaderboard:%n"));
        for (UserSegmentStatistics userSegmentStatistics : statistics)
        {
            toReturn.append(userSegmentStatistics);
        }
        return toReturn.toString();
    }
}