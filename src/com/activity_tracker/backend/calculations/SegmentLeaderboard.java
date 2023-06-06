package com.activity_tracker.backend.calculations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * SegmentLeaderboard: A class that represents a leaderboard for a specific segment
 * It contains a TreeSet of UserSegmentStatistics, which is sorted by the UserSegmentStatistics' time
 */
public class SegmentLeaderboard implements Serializable
{
    // statistics: A TreeSet of UserSegmentStatistics, sorted by the UserSegmentStatistics' time
    private final TreeSet<UserSegmentStatistics> statistics;

    // users: HashSet that contains the usernames that have registered statistics for this leaderboard's segment
    private final HashSet<String> users;

    // fileName: The name of the file this segment was extracted from
    private final String fileName;

    /**
     * Constructs a new SegmentLeaderboard with the given file name.
     *
     * @param fileName the name of the file
     */
    public SegmentLeaderboard(String fileName)
    {
        this.statistics = new TreeSet<>();
        this.users = new HashSet<>();
        this.fileName = fileName;
    }

    /**
     * Registers a UserSegmentStatistics to the leaderboard.
     *
     * @param userSegmentStatistics  the UserSegmentStatistics to register
     */
    public void registerSegmentStatistics(UserSegmentStatistics userSegmentStatistics)
    {
        this.statistics.add(userSegmentStatistics);
        this.users.add(userSegmentStatistics.getUsername());
    }

    /**
     * Returns the leaderboard as a TreeSet of UserSegmentStatistics objects.
     * @return the leaderboard as a TreeSet of UserSegmentStatistics objects
     */
    public TreeSet<UserSegmentStatistics> getLeaderboard()
    {
        return this.statistics;
    }

    public String getTrimmedFileName()
    {
        int fileTypeIndex = fileName.trim().indexOf(".gpx");
        return fileName.substring(0, fileTypeIndex);
    }

    /**
     * containsUser: Used to check whether a user has registered statistics associated with a segment.
     *
     * @param user: The username
     * @return true if the users HashSet contains the username.
     */
    public boolean containsUser(String user)
    {
        return this.users.contains(user);
    }

    /**
     * Returns a representation of the leaderboard in a String format.
     *
     * @return the leaderboard in a String format
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Found a segment inside the route you registered!\n");
        sb.append("Here is the leaderboard for this segment:\n");
        sb.append("+---------------------------------+\n");
        sb.append("|             Leaderboard         |\n");
        sb.append("+------+---------+----------------+\n");
        sb.append("| Rank |   User  | Time (minutes) |\n");
        sb.append("+------+---------+----------------+\n");
        int rank = 1;
        for (UserSegmentStatistics stats : statistics)
        {
            sb.append(String.format("| %4d | %-8s|%15.2f |\n", rank++, stats.getUsername(), stats.getTime()));
        }
        sb.append("+------+---------+----------------+\n");
        return sb.toString();
    }







}