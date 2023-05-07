package activity.calculations;

import java.io.Serializable;

// SegmentStats: A class that represents the statistics for a specific segment
public class SegmentStats implements Serializable
{
    // represents the id of the segment this SegmentStats was created for
    private final int segmentID;
    // represents the name of the segment file this statistics was created for
    private final String fileName;
    // represents the time it took for the user to complete the segment
    private double time;

    SegmentStats(int segmentID, String fileName)
    {
        this.segmentID = segmentID;
        this.fileName = fileName;
        this.time = 0;
    }

    // timeUpdate: Used in the mapping phase to update the time it took for the user to complete the segment
    public void timeUpdate(double time)
    {
        this.time += time;
    }

    public double getTime()
    {
        return time;
    }

    public int getSegmentID()
    {
        return this.segmentID;
    }

    public String getFileName()
    {
        return fileName;
    }

    // Equals function that only compares the segmentID between two SegmentStats
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SegmentStats)) return false;
        SegmentStats that = (SegmentStats) o;
        return segmentID == that.segmentID;
    }
}
