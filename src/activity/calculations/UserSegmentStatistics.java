package activity.calculations;

import java.io.Serializable;
import java.util.Objects;

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

        if (comparisonResult < 0)
        {
            return (int) Math.floor(comparisonResult);
        }
        else if (comparisonResult > 0)
        {
            return (int) Math.ceil(comparisonResult);
        }
        else
        {
            return this.username.compareTo(o.username);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UserSegmentStatistics)) return false;

        UserSegmentStatistics that = (UserSegmentStatistics) o;
        return segmentID == that.segmentID && Double.compare(that.time, time) == 0 && Objects.equals(username, that.username);
    }

    public String getUsername()
    {
        return username;
    }

    public double getTime()
    {
        return time;
    }

    @Override
    public String toString()
    {
        return String.format("%s: %.2f min", username, time);
    }
}
