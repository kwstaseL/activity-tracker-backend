package com.activity_tracker.backend.calculations;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/* Statistics: The class that will be in charge of handling all the related statistics.
 * Will maintain a hashmap of users-UserStatistics, a counter of the routes recorded,
 * as well as the total distance, elevation and activity time across all users.
 */
public class Statistics implements Serializable
{
    // Assigns a version number to the serializable class for compatibility during deserialization.
    private static final long serialVersionUID = 2L;
    // The total number of routes recorded,
    private int routesRecorded;
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;

    // userStats: A hashmap matching each user to their respective statistics.
    private HashMap<String, UserStatistics> userStats = new HashMap<>();

    // segmentStatistics: Matches the hashcode (integer) of a segment name, to a leaderboard of user stats for that segment
    private HashMap<Integer, SegmentLeaderboard> segmentStatistics = new HashMap<>();

    /**
     * Initializes a new instance of the Statistics class.
     */
    public Statistics()
    {
        this.routesRecorded = 0;
        this.totalDistance = 0;
        this.totalElevation = 0;
        this.totalActivityTime = 0;

        // if we find a file with pre-existing stats, we load them.
        if (fileExists())
        {
            loadStats();
        }
    }

    /**
     * Copy constructor for the Statistics class.
     * @param other The Statistics object to copy.
     */
    public Statistics(Statistics other)
    {
        this.routesRecorded = other.routesRecorded;
        this.totalDistance = other.totalDistance;
        this.totalElevation = other.totalElevation;
        this.totalActivityTime = other.totalActivityTime;

        // Copying the user stats and segment stats
        this.userStats = new HashMap<>(other.userStats);
        this.segmentStatistics = new HashMap<>(other.segmentStatistics);
    }

    /**
     * Registers a new route for a user, updating their statistics and the total statistics for all users.
     * Also updates the segment statistics for the user.
     *
     * @param user The user who recorded the new route.
     * @param activityStats The statistics for the new route.
     */
    public void registerRoute(String user, ActivityStats activityStats)
    {
        // first, updating the user specific stats
        if (!userStats.containsKey(user))
        {
            userStats.put(user, new UserStatistics(user));
        }
        userStats.get(user).registerRoute(activityStats);

        // then, updating the total stats
        totalDistance += activityStats.getDistance();
        totalElevation += activityStats.getElevation();
        totalActivityTime += activityStats.getTime();
        ++routesRecorded;

        // Extracting all the segment stats this user has done, and updating the segment statistics
        ArrayList<SegmentActivityStats> segmentStatsList = activityStats.getSegmentStatsList();
        for (SegmentActivityStats segment : segmentStatsList)
        {
            int segmentHash = segment.getFileName().hashCode();
            if (!this.segmentStatistics.containsKey(segmentHash))
            {
                // Creating a new SegmentLeaderBoard for that specific segment if it does not exist
                segmentStatistics.put(segmentHash, new SegmentLeaderboard(segment.getFileName()));
            }
            // If the leaderboard for that segment already exists, we register the segment statistics for that user
            SegmentLeaderboard leaderboard = segmentStatistics.get(segmentHash);
            if (leaderboard == null)
            {
                throw new RuntimeException("Leaderboard appears null");
            }
            leaderboard.registerSegmentStatistics(new UserSegmentStatistics(segmentHash, user, segment.getTime()));
        }
    }

    /**
     * Registers existing statistics for a user, updating the total statistics for all users.
     *
     * @param userStatistics The user statistics to register.
     */
    private void registerStatistics(UserStatistics userStatistics)
    {
        this.routesRecorded += userStatistics.getRoutesRecorded();
        this.totalDistance += userStatistics.getTotalDistance();
        this.totalElevation += userStatistics.getTotalElevation();
        this.totalActivityTime += userStatistics.getTotalActivityTime();
    }

    /**
     * Checks if the statistics file exists.
     *
     * @return Returns true if the statistics file exists, false otherwise.
     */
    private boolean fileExists()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            File statisticsPath = new File(config.getProperty("statistics_directory"));
            File[] directoryContents = statisticsPath.listFiles();

            if (directoryContents == null)
            {
                // if directoryContents == null (meaning we could not find the file), we create the directory so that we can create the file later
                if (!new File(config.getProperty("statistics_directory")).mkdirs())
                {
                    throw new RuntimeException("Could not find the directory, and could not make a new directory.");
                }
                return false;
            }

            for (File file : directoryContents)
            {
                if (file.getName().equals(config.getProperty("statistics_file")))
                {
                    return true;
                }
            }
            return false;

        }
        catch (IOException e)
        {
            System.out.println("Could not load config.");
            throw new RuntimeException(e);
        }
    }

    /**
     *  Called upon Statistics initialisation, loads data into both the userStats hashmap and the segmentStatistics hashmap
     *  for the statistics xml file and for each segment xml file for all users.
     */
    private void loadStats()
    {
        try
        {
            // first, loading the appropriate info from the config and initialising a new DocumentBuilder
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            String statisticsPath = config.getProperty("statistics_directory");
            String statisticsFilename = config.getProperty("statistics_file");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Finding the statistics file and parsing it
            Document doc = dBuilder.parse(new File(statisticsPath + File.separator + statisticsFilename));

            // Normalizing the XML structure to prevent errors
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("User");

            // for all the users in our database, write their respective stats to the XML file
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Element currentElement = (Element) nodeList.item(i);
                String user = currentElement.getAttribute("Username");
                int routesForUser = Integer.parseInt(currentElement.getAttribute("Routes_Recorded"));
                double totalDistanceForUser = Double.parseDouble(currentElement.getAttribute("Total_Distance"));
                double totalElevationForUser = Double.parseDouble(currentElement.getAttribute("Total_Elevation"));
                double totalActivityTimeForUser = Double.parseDouble(currentElement.getAttribute("Total_Activity_Time"));

                // Creating a new UserStatistics instance for that user and registering it in the userStats hashmap, also registering the stats
                UserStatistics userStatistics = new UserStatistics(user, routesForUser, totalDistanceForUser, totalElevationForUser, totalActivityTimeForUser);
                userStats.put(user, userStatistics);
                registerStatistics(userStatistics);
            }

            // Finding all the segment files and parsing them
            File statsDir = new File(statisticsPath);
            // Filtering the files to only get the segment files
            File[] segmentStatsFiles = statsDir.listFiles((dir, name) -> name.startsWith("segment") && name.endsWith(".xml"));

            DocumentBuilderFactory dbFactor = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactor.newDocumentBuilder();

            // for all the segment files, we parse them and load the stats into the segmentStatistics hashmap
            assert segmentStatsFiles != null;
            for (File statsFile : segmentStatsFiles)
            {
                Document document = dbBuilder.parse(statsFile);
                document.getDocumentElement().normalize();

                // Load stats for segments
                NodeList segmentNodeList = document.getElementsByTagName("Segment_Statistics");
                for (int i = 0; i < segmentNodeList.getLength(); i++)
                {
                    Element currentElement = (Element) segmentNodeList.item(i);

                    String fileName = currentElement.getAttribute("File_Name");
                    // Hashing the file name to get a unique ID for the segment
                    int segmentHash = fileName.hashCode();
                    NodeList userNodeListForSegment = currentElement.getElementsByTagName("User");

                    // Creating a new SegmentLeaderboard instance for that segment and registering it in
                    // the segmentStatistics hashmap
                    SegmentLeaderboard segmentLeaderboard = new SegmentLeaderboard(fileName);

                    for (int j = 0; j < userNodeListForSegment.getLength(); j++)
                    {
                        Element currentUserElement = (Element) userNodeListForSegment.item(j);
                        String user = currentUserElement.getAttribute("Username");
                        double time = Double.parseDouble(currentUserElement.getAttribute("Time"));

                        // Creating a new UserSegmentStatistics instance for that user and registering it in the segmentLeaderboard
                        UserSegmentStatistics segmentUserStatistics = new UserSegmentStatistics(segmentHash, user, time);
                        segmentLeaderboard.registerSegmentStatistics(segmentUserStatistics);
                    }
                    segmentStatistics.put(segmentHash, segmentLeaderboard);
                }
            }

        }
        catch (ParserConfigurationException e)
        {
            System.out.println("Could not configure parser.");
            throw new RuntimeException("Could not configure parser.");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Could not find the file.");
            throw new RuntimeException("Could not find the file");
        }
        catch (IOException e)
        {
            System.out.println("An error occurred during the I/O process while initialising Statistics.");
            throw new RuntimeException("An error occurred during the I/O process while initialising Statistics.");
        }
        catch (SAXException e)
        {
            System.out.println("An error occurred during the parsing process while initialising Statistics.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when master closes the connection with the user. Creates the statistics file and writes the statistics for
     * all users to it. Also, creates a new xml file for each segment and writes the statistics for that segment to it.
     */
    public void writeToFile()
    {
        try
        {
            // Create the statistics.xml file and write the statistics for all users to it
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();

            // root node
            Element root = doc.createElement("User_Statistics");
            doc.appendChild(root);

            // for all the users currently registered, write their statistics to the xml file
            for (String user : userStats.keySet())
            {
                UserStatistics statisticsForUser = userStats.get(user);

                Element userElement = doc.createElement("User");
                userElement.setAttribute("Username", user);
                userElement.setIdAttribute("Username", true);
                if (statisticsForUser == null)
                {
                    throw new RuntimeException("statisticsForUser appears null");
                }
                userElement.setAttribute("Routes_Recorded", String.valueOf(statisticsForUser.getRoutesRecorded()));
                userElement.setAttribute("Total_Distance", String.valueOf(statisticsForUser.getTotalDistance()));
                userElement.setAttribute("Total_Elevation", String.valueOf(statisticsForUser.getTotalElevation()));
                userElement.setAttribute("Total_Activity_Time", String.valueOf(statisticsForUser.getTotalActivityTime()));

                root.appendChild(userElement);
            }

            // write user statistics to separate files for each segment, same as above
            for (Integer segmentHash : segmentStatistics.keySet())
            {
                SegmentLeaderboard statisticsForSegment = segmentStatistics.get(segmentHash);
                if (statisticsForSegment == null)
                {
                    throw new RuntimeException("statisticsForSegment appears null");
                }
                TreeSet<UserSegmentStatistics> userStatistics = statisticsForSegment.getLeaderboard();
                String segmentName = statisticsForSegment.getTrimmedFileName();

                // create a new document for this segment
                Document segmentDoc = builder.newDocument();
                Element segmentRoot = segmentDoc.createElement("Segment_Statistics");

                segmentRoot.setAttribute("File_Name", segmentName + ".gpx");
                segmentDoc.appendChild(segmentRoot);

                // loop through the TreeSet and add each user to the XML file
                for (UserSegmentStatistics userStat : userStatistics)
                {
                    Element userElement = segmentDoc.createElement("User");
                    userElement.setAttribute("Username", userStat.getUsername());
                    userElement.setIdAttribute("Username", true);
                    userElement.setAttribute("Time", String.valueOf(userStat.getTime()));
                    segmentRoot.appendChild(userElement);
                }

                // write the segment document to a separate file
                DOMSource segmentSource = new DOMSource(segmentDoc);
                Properties config = new Properties();
                config.load(new FileInputStream("config.properties"));
                String path = Paths.get(config.getProperty("statistics_directory"),segmentName + "_statistics.xml").toString();

                File file = new File(path);
                Result result = new StreamResult(file);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(segmentSource, result);
            }

            // write user statistics to a single file
            DOMSource source = new DOMSource(doc);
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            String path = Paths.get(config.getProperty("statistics_directory"), config.getProperty("statistics_file")).toString();

            File file = new File(path);
            Result result = new StreamResult(file);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load config.");
        }
        catch (FactoryConfigurationError e)
        {
            throw new RuntimeException("Could not create a DBF instance.");
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException("Could not create a DB builder.");
        }
        catch (TransformerConfigurationException e)
        {
            throw new RuntimeException("Could not create a TF instance.");
        }
        catch (TransformerException e)
        {
            throw new RuntimeException("Could not transform file.");
        }
    }

    /**
     * Called when sending a user their respective UserStatistics instance
     * @param username The username of the user
     * @return Returns the UserStatistics object for the given user, or a default UserStatistics
     * object if the user has not yet registered a route.
     * @throws RuntimeException If the user has not been registered.
     */
    public UserStatistics getUserStats(String username)
    {
        if (!userStats.containsKey(username))
        {
            return new UserStatistics(username);
        }
        return userStats.get(username);
    }

    /**
     * getGlobalStats: Called by a user when they want to fetch app-wide stats.
     * @return the entire instance of Statistics this method is called on.
     */
    public Statistics getGlobalStats()
    {
        return this;
    }

    /**
     * Calculates and returns the average distance recorded across all users.
     */
    public double getAverageDistance()
    {
        if (routesRecorded == 0)
        {
            return 0.0;
        }
        return totalDistance / userStats.keySet().size();
    }

    /**
     * Calculates and returns the average elevation recorded across all users.
     */
    public double getAverageElevation()
    {
        if (routesRecorded == 0)
        {
            return 0.0;
        }
        return totalElevation / userStats.keySet().size();
    }

    /**
     * Calculates and returns the average Activity Time per user.
     */
    public double getAverageActivityTime()
    {
        if (routesRecorded == 0)
        {
            return 0.0;
        }
        return totalActivityTime / userStats.keySet().size();
    }


    /**
     * Calculates the average distance for a specific user by dividing their total distance by the number of routes
     * they have recorded.
     * @param user the user for whom to calculate the average distance
     * @return the average distance for the user, or 0 if they have not yet registered any routes.
     */
    @Deprecated
    public double getAverageDistanceForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            return 0.0;
        }

        return userStats.get(user).getAverageDistance();
    }

    /**
     * Calculates the average elevation for a specific user by dividing their total distance by the number of routes they have
     * recorded.
     * @param user the user for whom to calculate the average distance
     * @return the average distance for the user
     * @throws RuntimeException if the user has not recorded any routes
     */
    @Deprecated
    public double getAverageElevationForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageElevation();
    }

    /**
     * Calculates the average activity time for a specific user by dividing their total distance by the number of routes they have
     * recorded.
     * @param user the user for whom to calculate the average distance
     * @return the average distance for the user
     * @throws RuntimeException if the user has not recorded any routes
     */
    @Deprecated
    public double getAverageActivityTimeForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageActivityTime();
    }

    /**
     * getSegmentLeaderboardsForUser: Used to get all the segment leaderboards associated with a user.
     * @param user: The user for whom to fetch the leaderboards.
     * @return An arraylist of leaderboards.
     */
    public ArrayList<SegmentLeaderboard> getSegmentLeaderboardsForUser(String user)
    {
        ArrayList<SegmentLeaderboard> leaderboards = new ArrayList<>();
        for (SegmentLeaderboard leaderboard : segmentStatistics.values())
        {
            if (leaderboard.containsUser(user))
            {
                leaderboards.add(leaderboard);
            }
        }
        return leaderboards;
    }

    /**
     * @return Returns a string representation of the global statistics.
     */
    @Override
    public String toString()
    {
        return "+---------------------------------------------+\n" +
                "|       Statistics across all users           |\n" +
                "+-------------------+-------------------------+\n" +
                String.format("| %-17s | %20.2f km |\n", "Average Distance", getAverageDistance()) +
                String.format("| %-17s | %20.2f m  |\n", "Average Elevation", getAverageElevation()) +
                String.format("| %-17s | %19.2f min |\n", "Avg Workout Time", getAverageActivityTime()) +
                "+-------------------+-------------------------+\n";
    }



}
