import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable
{
    private ArrayList<Waypoint> waypoints;
    private String username;
    private int routeID;
    private String clientID;
    private static int idGenerator = 0;

    public Route(ArrayList<Waypoint> waypoints, String username) {
        this.waypoints = waypoints;
        this.username = username;
        this.routeID = idGenerator++;
    }

    public Route(ArrayList<Waypoint> waypoints, int routeID, String clientID) {
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

    public String getClientID() { return clientID; }

    public void setClientID(String clientID) { this.clientID = clientID; }


}
