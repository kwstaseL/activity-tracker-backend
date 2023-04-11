import java.util.ArrayList;
import java.util.Queue;

public class WorkDispatcher
{
    private ArrayList<WorkerHandler> workers;
    private Queue<Route> filesToWorker;

    public WorkDispatcher(ArrayList<WorkerHandler> workers, Queue<Route> filesToWorker)
    {
        this.workers = workers;
        this.filesToWorker = filesToWorker;
    }

    public void start()
    {

    }



}
