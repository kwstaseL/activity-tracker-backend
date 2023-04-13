package activity.parser;

/* ChunkedRoute: Wrapper class, which contains the ArrayList of Waypoints the chunk is supposed to contain, a variable indicating how many chunks in total
 * the route was split into, and a variable indicating the index of the chunk being currently processed amongst the total chunks
 */
public class Chunk {
    private Route route;
    private int totalChunks;
    private int chunkIndex;

    public Chunk(Route route, int totalChunks, int chunkIndex)
    {
        this.route = route;
        this.totalChunks = totalChunks;
        this.chunkIndex = chunkIndex;
    }

    public Route getRoute() {
        return route;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }
}
