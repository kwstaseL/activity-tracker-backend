package activity.calculations;

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
import java.util.HashMap;
import java.util.Properties;

/* Statistics: The class that will be in charge of handling all the related statistics.
 * Will maintain a hashmap of users-UserStatistics, a counter of the routes recorded,
 * as well as the total distance, elevation and activity time across all users.
 */
public class Statistics implements Serializable
{
    // userStats: A hashmap matching each user to their respective statistics.
    private final HashMap<String, UserStatistics> userStats;
    private int routesRecorded;
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;

    public Statistics()
    {
        this.userStats = new HashMap<>();
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
            throw new RuntimeException(e);
        }
    }

    // initialise: Called upon Statistics initialisation, loads data from our "statistics.xml" file
    public void loadStats()
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
                double totalDistance = Double.parseDouble(currentElement.getAttribute("Total_Distance"));
                double totalElevation = Double.parseDouble(currentElement.getAttribute("Total_Elevation"));
                double totalActivityTime = Double.parseDouble(currentElement.getAttribute("Total_Activity_Time"));

                UserStatistics userStatistics = new UserStatistics(user, routesForUser, totalDistance, totalElevation, totalActivityTime);
                userStats.put(user, userStatistics);
                registerStatistics(userStatistics);
            }
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException("Could not configure parser.");
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("Could not find the file");
        }
        catch (IOException e)
        {
            throw new RuntimeException("An error occurred during the I/O process while initialising Statistics.");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

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

        updateStats();
    }

    // registerStatistics: Called when adding a new UserStatistics instance to our database, when loading stats from the XML file.
    public void registerStatistics(UserStatistics userStatistics)
    {
        this.routesRecorded += userStatistics.getRoutesRecorded();
        this.totalDistance += userStatistics.getTotalDistance();
        this.totalElevation += userStatistics.getTotalElevation();
        this.totalActivityTime += userStatistics.getTotalActivityTime();
    }

    // createFile: Called when first creating the file. Writes down the statistics for all users currently registered
    public void createFile()
    {
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
                UserStatistics statisticsForUser = userStats.get(user);

                Element userElement = doc.createElement("User");
                userElement.setAttribute("Username", user);
                userElement.setIdAttribute("Username", true);
                userElement.setAttribute("Routes_Recorded", String.valueOf(statisticsForUser.getRoutesRecorded()));
                userElement.setAttribute("Total_Distance", String.valueOf(statisticsForUser.getTotalDistance()));
                userElement.setAttribute("Total_Elevation", String.valueOf(statisticsForUser.getTotalElevation()));
                userElement.setAttribute("Total_Activity_Time", String.valueOf(statisticsForUser.getTotalActivityTime()));

                root.appendChild(userElement);
            }

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

    // updateStats: Updates the xml file accordingly.
    private void updateStats()
    {
        // TODO: Optimise this method, to avoid creating the entire file every time
        // TODO: Make a similar segmentstatistics.xml file and update it likewise, elements segment name, child element user with attributes activity time, speed
        createFile();

    }

    private void updateFile()
    {

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
