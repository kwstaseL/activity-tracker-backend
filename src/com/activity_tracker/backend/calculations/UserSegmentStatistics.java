package com.activity_tracker.backend.calculations;

import java.io.Serializable;
import java.util.Objects;

// UserSegmentStatistics: A class that represents the statistics of a specific user, for a specific segment
public class UserSegmentStatistics implements Comparable<UserSegmentStatistics>, Serializable
{
    // segmentID: The ID of the segment this UserSegmentStatistics is for
    private final int segmentHashID;
    // username: The username of the user this UserSegmentStatistics is for
    private final String username;
    // time: The time it took for the user to complete the segment
    private final double time;

    /**
     * Constructs a UserSegmentStatistics object with the given segment ID, username, and time.
     *
     * @param segmentHashID the ID of the segment this UserSegmentStatistics is for
     * @param username the username of the user this UserSegmentStatistics is for
     * @param time the time it took for the user to complete the segment
     */
    public UserSegmentStatistics(int segmentHashID, String username, double time)
    {
        this.segmentHashID = segmentHashID;
        this.username = username;
        this.time = time;
    }

    /**
     * Compares two UserSegmentStatistics objects based on their completion times.
     * Used to sort UserSegmentStatistics objects in a TreeSet.
     *
     * @param o the UserSegmentStatistics object to compare to
     * @return -1 if this UserSegmentStatistics has a shorter completion time than o,
     *         0 if their completion times are equal and their usernames are the same,
     *         1 if this UserSegmentStatistics has a longer completion time than o
     *         or their completion times are equal and their usernames are different.
     */
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

    /**
     * Checks if this UserSegmentStatistics object is equal to another object based on their segment IDs,
     * usernames, and completion times.
     *
     * @param o the object to compare to
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UserSegmentStatistics)) return false;

        UserSegmentStatistics that = (UserSegmentStatistics) o;
        return segmentHashID == that.segmentHashID && Double.compare(that.time, time) == 0 && Objects.equals(username, that.username);
    }

    /**
     * Gets the username of the user this UserSegmentStatistics is for.
     *
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Gets the completion time of this UserSegmentStatistics.
     *
     * @return the completion time in minutes
     */
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
