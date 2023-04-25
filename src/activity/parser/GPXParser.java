package activity.parser;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.ArrayList;

public class GPXParser
{
    public static Route parse(File file)
    {

        ArrayList<Waypoint> waypoints = new ArrayList<>();
        Route route = null;
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

            String creator = null;

            // Iterate through all the <wpt> tags.
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                // Get the <wpt> tag we are currently processing.
                Element element = (Element) nodeList.item(i);
                creator = doc.getDocumentElement().getAttribute("creator");
                String latitude = element.getAttribute("lat");
                String longitude = element.getAttribute("lon");
                String elevation = element.getElementsByTagName("ele").item(0).getTextContent();
                String time = element.getElementsByTagName("time").item(0).getTextContent();

                // Add the waypoint to the list
                waypoints.add(new Waypoint(Double.parseDouble(latitude), Double.parseDouble(longitude),
                        Double.parseDouble(elevation), time));
            }

            if (creator == null || waypoints.isEmpty()) {
                throw new RuntimeException("Could not parse the file successfully.");
            }
            route = new Route(waypoints, creator, file.getName());
            System.out.println("Parsed: " + route);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return route;
    }

}
