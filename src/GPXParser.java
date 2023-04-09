import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.ArrayList;

public class GPXParser
{
    public ArrayList<Waypoint> parse(File file)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        try
        {
            // Parsing the GPX file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            // Normalizing the XML structure to prevent errors
            doc.getDocumentElement().normalize();
            System.out.println("Processing file: " + file.getName());

            // This will return all the <wpt> tags
            NodeList nodeList = doc.getElementsByTagName("wpt");

            // Iterate through all the <wpt> tags
            for (int i = 0; i < nodeList.getLength(); i++) {
                // Get the <wpt> tag we are currently processing
                Element element = (Element) nodeList.item(i);
                String creator = doc.getDocumentElement().getAttribute("creator");
                String latitude = element.getAttribute("lat");
                String longitude = element.getAttribute("lon");
                String elevation = element.getElementsByTagName("ele").item(0).getTextContent();
                String time = element.getElementsByTagName("time").item(0).getTextContent();

                // Add the waypoint to the list
                waypoints.add(new Waypoint(Double.parseDouble(latitude), Double.parseDouble(longitude),
                        Double.parseDouble(elevation), time, creator));

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return waypoints;
    }

    public static void main(String[] args)
    {
        GPXParser parser = new GPXParser();
        ActivityCalculator calculator = new ActivityCalculator();

        ArrayList<Waypoint> waypoints = parser.parse(new File("/Users/kwstasel/IdeaProjects/Activity Tracker/data/route3.gpx"));

        double totalDistance = 0.0;
        double totalElevation = 0.0;
        double totalTime = 0.0;
        double totalaverageSpeed = 0.0;

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint w1 = waypoints.get(i);
            Waypoint w2 = waypoints.get(i+1);
            ActivityStats stats = calculator.calculateStats(w1, w2);
            totalDistance += stats.getDistance();
            totalElevation += stats.getElevation();
            totalaverageSpeed += stats.getSpeed();
            totalTime += stats.getTime();
        }

        totalaverageSpeed = (totalTime > 0) ? totalDistance / (totalTime / 60.0) : 0.0;


        System.out.println("Total Distance: " + String.format("%.2f", totalDistance) + " km");
        System.out.println("Average Speed: " + String.format("%.2f", totalaverageSpeed) + " km/h");
        System.out.println("Total Elevation: " + totalElevation + " m");
        System.out.println("Total Time: " + totalTime + " minutes");

    }


}
