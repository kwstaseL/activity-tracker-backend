package activity.calculations;

import java.io.Serializable;
import java.util.Objects;

// UserSegmentStatistics: A class that represents the statistics for a specific segment for a specific user
public class UserSegmentStatistics implements Comparable<UserSegmentStatistics>, Serializable
{
    // segmentID: The ID of the segment this UserSegmentStatistics is for
    private int segmentID;
    // username: The username of the user this UserSegmentStatistics is for
    private String username;
    // time: The time it took for the user to complete the segment
    private final double time;

    public UserSegmentStatistics(int segmentID, String username, double time)
    {
        this.segmentID = segmentID;
        this.username = username;
        this.time = time;
    }

    // compareTo: Used to compare two UserSegmentStatistics when inserting them into a TreeSet for the leaderboard
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

    // Equals function that only compares the segmentID, username and time between two UserSegmentStatistics
    // Used for the TreeSet, to handle the case where two users have the same time for a segment
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
