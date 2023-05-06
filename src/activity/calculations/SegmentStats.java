package activity.calculations;

import java.io.Serializable;

public class SegmentStats implements Serializable
{
    private final int segmentID;    // represents the id of the segment this SegmentStats instance represents
    private final String fileName;  // represents the name of the segment file
    private double time;

    SegmentStats(int segmentID, String fileName)
    {
        this.segmentID = segmentID;
        this.fileName = fileName;
        this.time = 0;
    }

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SegmentStats)) return false;
        SegmentStats that = (SegmentStats) o;
        return segmentID == that.segmentID;
    }
}
