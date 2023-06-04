package com.activity_tracker.backend.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Queue;

public class GPXParser
{
    /**
     * Parses a GPX file from an input stream and returns a Route object.
     *
     * @param inputStream the input stream containing the GPX data
     * @param segments a queue of Segment objects to check the route against
     * @return a Route object representing the parsed GPX data
     * @throws RuntimeException if the GPX data cannot be parsed successfully
     */
    public static Route parseRoute(ByteArrayInputStream inputStream, Queue<Segment> segments)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        Route route = null;
        try
        {
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

            if (creator == null || waypoints.isEmpty())
            {
                throw new RuntimeException("Could not parse the GPX data successfully.");
            }

            route = new Route(waypoints, creator, "GPX Data");

            // Check for all the segments if the route is inside them
            for (Segment segment : segments)
            {
                route.checkForSegment(segment);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return route;
    }

    /**
     * Parses a GPX file from a file object and returns a Segment object.
     *
     * @param segmentFile the GPX file to parse
     * @return a Segment object representing the parsed GPX data
     * @throws RuntimeException if the file cannot be parsed successfully
     */
    public static Segment parseSegment(File segmentFile)
    {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        Segment segments = null;
        try {
            // Parsing the GPX file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(segmentFile);
            String fileName = segmentFile.getName();

            // Normalizing the XML structure to prevent errors
            doc.getDocumentElement().normalize();

            // This will return all the <wpt> tags
            NodeList nodeList = doc.getElementsByTagName("wpt");

            // Iterate through all the <wpt> tags.
            for (int i = 0; i < nodeList.getLength(); i++)
            {
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
            segments = new Segment(waypoints, fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return segments;
    }
}
