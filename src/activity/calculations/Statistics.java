package activity.calculations;

import activity.misc.Pair;
import org.w3c.dom.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/* Statistics: The class that will be in charge of handling all the related statistics.
 * Will maintain a hashmap of users-UserStatistics, a counter of the routes recorded, an archive of
 * the stats recorded, as well as the total distance, elevation and activity time across all users.
 */
public class Statistics implements Serializable {

    // userStats: A hashmap matching each user to their respective statistics.
    private HashMap<String, UserStatistics> userStats = new HashMap<>();

    // activityArchive: An ArrayList consisting of the detailed stats of every route in the system
    private ArrayList<Pair<String, ActivityStats>> activityArchive = new ArrayList<>();     // TODO: Possibly unnecessary?
    private int routesRecorded = 0;
    private double totalDistance = 0;
    private double totalElevation = 0;
    private double totalActivityTime = 0;

    public void registerRoute(String user, ActivityStats activityStats) {
        // first, updating the user specific stats
        if (!userStats.containsKey(user)) {
            userStats.put(user, new UserStatistics(user));
        }
        userStats.get(user).registerRoute(activityStats);

        // then, updating the total stats
        totalDistance += activityStats.getDistance();
        totalElevation += activityStats.getElevation();
        totalActivityTime += activityStats.getTime();
        activityArchive.add(new Pair<>(user, activityStats));
        ++routesRecorded;

        updateStats();
    }

    // updateStats: Updates the xml file accordingly.
    private void updateStats()
    {
        // TODO: Optimise this method, to avoid updating the entire file every time
        createFile();

    }

    private void updateFile()
    {

    }

    // fileExists: Returns true if the statistics file already exists, false otherwise.
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
                if (!new File(config.getProperty("statistics_directory")).mkdirs())
                {
                    throw new RuntimeException("Could not find the directory, and could not make a new directory.");
                }
                directoryContents = new File[0];
                statisticsPath = new File(config.getProperty("statistics_directory"));
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
            throw new RuntimeException(e);
        }
    }

    // createFile: Called when first creating the file. Writes down the statistics for all users currently registered
    public void createFile()
    {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));
            File statisticsPath = new File(config.getProperty("statistics_directory"));
            File[] directoryContents = statisticsPath.listFiles();
            if (directoryContents == null)
            {
                if (!new File(config.getProperty("statistics_directory")).mkdirs())
                {
                    throw new RuntimeException("Could not find the directory, and could not make a new directory.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try
        {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();

            // root node
            Element root = doc.createElement("User_Statistics");
            doc.appendChild(root);

            // for all the users currently registered, write their statistics to the xml file
            for (String user : userStats.keySet())
            {
                Element userElement = doc.createElement("User");

                Element usernameElement = doc.createElement("Username");
                Text nameOfUser = doc.createTextNode(user);
                usernameElement.appendChild(nameOfUser);
                userElement.appendChild(usernameElement);

                UserStatistics statisticsForUser = userStats.get(user);

                Element routeElement = doc.createElement("Routes_Recorded");
                Text routesForUser = doc.createTextNode(String.valueOf(statisticsForUser.getRoutesRecorded()));
                routeElement.appendChild(routesForUser);
                userElement.appendChild(routeElement);

                Element totalDistanceElement = doc.createElement("Total_Distance");
                Text totalDistanceOfUser = doc.createTextNode(String.valueOf(statisticsForUser.getTotalDistance()));
                totalDistanceElement.appendChild(totalDistanceOfUser);
                userElement.appendChild(totalDistanceElement);

                Element totalElevationElement = doc.createElement("Total_Elevation");
                Text totalElevationOfUser = doc.createTextNode(String.valueOf(statisticsForUser.getTotalElevation()));
                totalElevationElement.appendChild(totalElevationOfUser);
                userElement.appendChild(totalElevationElement);

                Element totalActivityTimeElement = doc.createElement("Total_Activity_Time");
                Text totalActivityTimeOfUser = doc.createTextNode(String.valueOf(statisticsForUser.getTotalActivityTime()));
                totalActivityTimeElement.appendChild(totalActivityTimeOfUser);
                userElement.appendChild(totalActivityTimeElement);

                root.appendChild(userElement);
            }

            // now, writing the statistics for all users
            Element total = doc.createElement("Total");

            Element totalRoutes = doc.createElement("Total_Routes_Recorded");
            Text totalRoutesText = doc.createTextNode(String.valueOf(routesRecorded));
            totalRoutes.appendChild(totalRoutesText);
            total.appendChild(totalRoutes);

            Element totalDistanceElement = doc.createElement("Total_Distance");
            Text totalDistanceText = doc.createTextNode(String.valueOf(totalDistance));
            totalDistanceElement.appendChild(totalDistanceText);
            total.appendChild(totalDistanceElement);

            Element totalElevationElement = doc.createElement("Total_Elevation");
            Text totalElevationText = doc.createTextNode(String.valueOf(totalElevation));
            totalElevationElement.appendChild(totalElevationText);
            total.appendChild(totalElevationElement);

            Element totalActivityTimeElement = doc.createElement("Total_Activity_Time");
            Text totalActivityTimeText = doc.createTextNode(String.valueOf(totalActivityTime));
            totalActivityTimeElement.appendChild(totalActivityTimeText);
            total.appendChild(totalActivityTimeElement);

            root.appendChild(total);

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

    // getUserStats: Returns the UserStatistics object associated with a specific user.
    public UserStatistics getUserStats(String user)
    {
        if (!userStats.containsKey(user)) {
            throw new RuntimeException("User has not been registered.");
        }
        return userStats.get(user);
    }

    public Statistics getGlobalStats()
    {
        return this;
    }

    // getAverageDistance: Calculates the average distance recorded across all routes.
    public double getAverageDistance()
    {
        if (routesRecorded == 0)
        {
            throw new RuntimeException("No routes have been recorded yet.");
        }
        return totalDistance / routesRecorded;
    }

    // getAverageElevation: Similarly to getAverageDistance
    public double getAverageElevation()
    {
        if (routesRecorded == 0)
        {
            throw new RuntimeException("No routes have been recorded yet.");
        }
        return totalElevation / routesRecorded;
    }

    // getAverageDistance: Similarly to getAverageDistance
    public double getAverageActivityTime()
    {
        if (routesRecorded == 0)
        {
            throw new RuntimeException("No routes have been recorded yet.");
        }
        return totalActivityTime / routesRecorded;
    }


    // getAverageDistanceForUser: Calculates the average distance for a user by dividing their total distance with the # of routes they have recorded.
    public double getAverageDistanceForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageDistance();
    }

    // getAverageElevationForUser: Similarly to getAverageDistanceForUser
    public double getAverageElevationForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageElevation();
    }

    // getAverageActivityTimeForUser: Similarly to getAverageDistanceForUser
    public double getAverageActivityTimeForUser(String user)
    {
        if (!userStats.containsKey(user))
        {
            throw new RuntimeException("User does not have any routes registered.");
        }

        return userStats.get(user).getAverageActivityTime();
    }

    @Override
    public String toString()
    {
        return String.format("Statistics across all users:\nAverage Distance: %.2f km\nAverage Elevation: %.2f m\nAverage Work Out Time: %.2f minutes",
                getAverageDistance(), getAverageElevation(), getAverageActivityTime());
    }

}
