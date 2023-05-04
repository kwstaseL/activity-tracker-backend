package activity.calculations;

import java.io.Serializable;

public class UserSegmentStatistics implements Comparable<UserSegmentStatistics>, Serializable
{
    private int segmentID;
    private String username;

    private final double time;

    public UserSegmentStatistics(int segmentID, String username, double time)
    {
        this.segmentID = segmentID;
        this.username = username;
        this.time = time;
    }

    @Override
    public int compareTo(UserSegmentStatistics o)
    {
        double comparisonResult = this.time - o.time;

        if (comparisonResult > -1 && comparisonResult < 0) {
            return -1;
        }
        return (int) Math.ceil(comparisonResult);
    }

    @Override
    public String toString()
    {
        return String.format("%s: %.2f min", username, time);
    }
}
