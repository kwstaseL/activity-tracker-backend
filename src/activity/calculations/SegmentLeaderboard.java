package activity.calculations;

import java.io.Serializable;
import java.util.TreeSet;

public class SegmentLeaderboard implements Serializable
{
    private final TreeSet<UserSegmentStatistics> statistics;
    private final String fileName;
    private final int segmentID;

    public SegmentLeaderboard(int segmentID, String fileName)
    {
        this.statistics = new TreeSet<>();
        this.segmentID = segmentID;
        this.fileName = fileName;
    }

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
        System.out.println("The file : " + fileName);
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