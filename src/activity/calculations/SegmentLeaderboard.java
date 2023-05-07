package activity.calculations;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * SegmentLeaderboard: A class that represents a leaderboard for a specific segment
 * It contains a TreeSet of UserSegmentStatistics, which is sorted by the UserSegmentStatistics' time
 */

public class SegmentLeaderboard implements Serializable
{
    // statistics: A TreeSet of UserSegmentStatistics, sorted by the UserSegmentStatistics' time
    private final TreeSet<UserSegmentStatistics> statistics;
    // fileName: The name of the file this segment was extracted from
    private final String fileName;

    public SegmentLeaderboard(String fileName)
    {
        this.statistics = new TreeSet<>();
        this.fileName = fileName;
    }
    // Used in the Statistics class to add a UserSegmentStatistics to the leaderboard
    public void registerSegmentStatistics(UserSegmentStatistics userSegmentStatistics)
    {
        this.statistics.add(userSegmentStatistics);
    }

    public TreeSet<UserSegmentStatistics> getLeaderboard()
    {
        return this.statistics;
    }

    public String getTrimmedFileName()
    {
        int fileTypeIndex = fileName.trim().indexOf(".gpx");
        return fileName.substring(0, fileTypeIndex);
    }
    @Override
    public String toString()
    {
        StringBuilder toReturn = new StringBuilder(String.format("Found a segment!%nLeaderboard:%n"));
        for (UserSegmentStatistics userSegmentStatistics : statistics)
        {
            toReturn.append(userSegmentStatistics).append("\n");
        }
        return toReturn.toString();
    }
}