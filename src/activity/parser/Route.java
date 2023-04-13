package activity.parser;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable
{
    private ArrayList<Waypoint> waypoints;
    private String user;
    private int routeID;
    private int clientID;
    private boolean moreFragments = true;
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
