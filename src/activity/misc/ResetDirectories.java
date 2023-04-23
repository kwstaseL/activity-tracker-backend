package activity.misc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class ResetDirectories {
    public static void main(String[] args) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            File unprocessedDirectory = new File(config.getProperty("unprocessed_directory"));
            File processedDirectory = new File(config.getProperty("processed_directory"));
            File[] processedDirectoryContents = processedDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile();   // file.isFile(): returns false if the file is a directory (like segments)
                }
            });

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
                } catch (IOException e)
                {
                    throw new RuntimeException("Could not move file.");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}