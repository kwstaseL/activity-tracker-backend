package com.activity_tracker.backend.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Resets the XML file with the statistics, along with the segment statistics if any exist.
 * Simply run the main method of the class to reset the statistics.
 * <p>
 * In order for the reset to be successful, it is advised to do this when the master is not currently running. If it
 * is, the reset will be meaningless, as the xml file will be regenerated as soon as a user sends a new gpx file (with
 * all the statistics as of the time of the reset still present).
 */
public class ResetStats
{
    private static void resetXMLStats()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            File statisticsPath = new File(config.getProperty("statistics_directory"));
            File[] directoryContents = statisticsPath.listFiles();
            if (directoryContents == null)
            {
                throw new RuntimeException("Could not find the directory.");
            }

            for (File file : directoryContents)
            {
                // delete all the xml that are related to statistics
                if (file.getName().endsWith("xml") && file.getName().contains("statistics"))
                {
                    if (!file.delete())
                    {
                        throw new RuntimeException("Could not delete file.");
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load config.");
        }
    }

    public static void main(String[] args)
    {
        resetXMLStats();
    }

}