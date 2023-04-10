import java.util.ArrayList;

public class Route {
    private ArrayList<Waypoint> waypoints;
    private String username;
    private int routeID;
    private static int idGenerator = 0;

    public Route(ArrayList<Waypoint> waypoints, String username) {
        this.waypoints = waypoints;
        this.username = username;
        this.routeID = idGenerator++;
    }

    public ArrayList<Waypoint> waypoints() {
        return this.waypoints;
    }

    public int getRouteID() {
        return routeID;
    }
}
