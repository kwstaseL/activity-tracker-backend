package com.activity_tracker.backend.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class ResetDirectories
{
    private static void resetFileDirectory()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            File unprocessedDirectory = new File(config.getProperty("unprocessed_directory"));
            File processedDirectory = new File(config.getProperty("completed_directory"));
            // file.isFile(): returns false if the file is a directory (like segments)
            File[] processedDirectoryContents = processedDirectory.listFiles(File::isFile);

            if (processedDirectoryContents == null)
            {
                throw new RuntimeException("Could not find the directory specified.");
            }

            for (File file : processedDirectoryContents)
            {
                Path sourcePath = Paths.get(file.getAbsolutePath());
                Path destPath = Paths.get(unprocessedDirectory.getAbsolutePath() + File.separator + file.getName());
                try
                {
                    Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Could not move file.");
                }
            }
            System.out.println("Successfully moved all files from processed directory to unprocessed directory.");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

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
                if (file.getName().endsWith("xml") && file.getName().contains("statistic"))
                {
                    if (!file.delete())
                    {
                        throw new RuntimeException("Could not delete file.");
                    }
                }
            }

        } catch (IOException e)
        {

            throw new RuntimeException("Could not load config.");
        }
    }

    public static void main(String[] args)
    {
        resetFileDirectory();
        resetXMLStats();
    }

}