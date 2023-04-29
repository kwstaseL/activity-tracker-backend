package activity.parser;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Queue;

public class GPXParser {
    public static Route parseRoute(ByteArrayInputStream inputStream, Queue<Segment> segments) {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        Route route = null;
        try {
            // Parsing the GPX file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            // Normalizing the XML structure to prevent errors
            doc.getDocumentElement().normalize();

            // This will return all the <wpt> tags
            NodeList nodeList = doc.getElementsByTagName("wpt");

            String creator = null;

            // Iterate through all the <wpt> tags.
            for (int i = 0; i < nodeList.getLength(); i++) {
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
                throw new RuntimeException("Could not parse the GPX data successfully.");
            }
            route = new Route(waypoints, creator, "GPX Data");
            System.out.println("Parsed: " + route);

            // Check for all the segments if the route is inside them
            for (Segment segment : segments)
            {
                if (route.containsSegment(segment))
                {
                    System.out.println("Segment " + segment + " is inside route " + route);
                }
                else
                {
                    System.out.println("Segment " + segment + " is NOT inside route " + route);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return route;
    }

    public static Segment parseSegment(File inputStream)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        Segment segment = null;
        try {
            // Parsing the GPX file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            // Normalizing the XML structure to prevent errors
            doc.getDocumentElement().normalize();

            // This will return all the <wpt> tags
            NodeList nodeList = doc.getElementsByTagName("wpt");

            // Iterate through all the <wpt> tags.
            for (int i = 0; i < nodeList.getLength(); i++) {
                // Get the <wpt> tag we are currently processing.
                Element element = (Element) nodeList.item(i);
                String latitude = element.getAttribute("lat");
                String longitude = element.getAttribute("lon");
                String elevation = element.getElementsByTagName("ele").item(0).getTextContent();
                String time = element.getElementsByTagName("time").item(0).getTextContent();

                // Add the waypoint to the list
                waypoints.add(new Waypoint(Double.parseDouble(latitude), Double.parseDouble(longitude),
                        Double.parseDouble(elevation), time));
            }

            if (waypoints.isEmpty())
            {
                throw new RuntimeException("Could not parse the file successfully.");
            }
            segment = new Segment(waypoints);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return segment;
    }
}
