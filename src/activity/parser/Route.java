package activity.parser;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable
{
    // Waypoints is the ArrayList of Waypoints that the route contains
    private final ArrayList<Waypoint> waypoints;
    // User is the username of the user who recorded the route
    private final String user;
    // Route ID is the unique ID of the route
    private final int routeID;
    // Client ID is the unique ID of the client-handler who sent the route
    private int clientID;
    // ID generator is a static variable that is used to generate unique IDs for each route
    private static int idGenerator = 0;

    public Route(ArrayList<Waypoint> waypoints, String user)
    {
        this.waypoints = waypoints;
        this.user = user;
        this.routeID = idGenerator++;
    }

    public Route(ArrayList<Waypoint> waypoints, int routeID, int clientID, String user) {
        this.waypoints = waypoints;
        this.routeID = routeID;
        this.clientID = clientID;
        this.user = user;
    }

    public ArrayList<Waypoint> waypoints() {
        return this.waypoints;
    }

    public int getRouteID() {
        return routeID;
    }

    public int getClientID() { return clientID; }

    public void setClientID(int clientID) { this.clientID = clientID; }

    public String getUser() {
        return user;
    }

    public String toString()
    {
        return "Route ID: " + routeID + " Username: " + user + " Waypoints: " + waypoints.size() + " First waypoint: " + waypoints.get(0);
    }
}
