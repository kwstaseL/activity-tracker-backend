package activity.calculations;

import java.io.Serializable;

/**
 * This class is used to store the statistics of a segment for a user
 */
public class SegmentStats implements Serializable
{
    // represents the id of the segment this SegmentStats was created for
    private final int segmentID;
    // represents the name of the segment file this statistics was created for
    private final String fileName;
    // represents the time it took for the user to complete the segment
    private double time;

    /**
     * Creates a new instance of SegmentStats.
     *
     * @param segmentID the id of the segment
     * @param fileName the name of the segment file
     * @throws IllegalArgumentException if segmentID is negative or fileName is null or empty
     */
    public SegmentStats(int segmentID, String fileName)
    {
        if (segmentID < 0)
        {
            throw new IllegalArgumentException("segmentID cannot be negative");
        }
        if (fileName == null || fileName.isEmpty())
        {
            throw new IllegalArgumentException("fileName cannot be null or empty");
        }
        this.segmentID = segmentID;
        this.fileName = fileName;
        this.time = 0;
    }

    /**
     * Used in the mapping phase to update the time it took for the user to complete the segment.
     *
     * @param time the time to update
     */
    public void timeUpdate(double time)
    {
        this.time += time;
    }

    /**
     * Returns the time it took for the user to complete the segment.
     *
     * @return the time
     */
    public double getTime()
    {
        return time;
    }

    /**
     * Returns the name of the segment file.
     *
     * @return the file name
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     *  Custom equals function that compares two SegmentStats objects based on their segmentID.
     * @param o the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SegmentStats)) return false;
        SegmentStats that = (SegmentStats) o;
        return segmentID == that.segmentID;
    }
}
