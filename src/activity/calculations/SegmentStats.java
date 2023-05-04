package activity.calculations;

import java.io.Serializable;

public class SegmentStats implements Serializable
{
    private final int segmentID;
    private double time;

    SegmentStats(int segmentID)
    {
        this.segmentID = segmentID;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SegmentStats that)) return false;
        return segmentID == that.segmentID;
    }
}
