import java.util.ArrayList;
import java.util.Queue;

public class WorkDispatcher implements Runnable
{
    private Queue<WorkerHandler> workers;
    private Queue<Route> filesToWorker;

    public WorkDispatcher(Queue<WorkerHandler> workers, Queue<Route> filesToWorker)
    {
        this.workers = workers;
        this.filesToWorker = filesToWorker;
    }

    public void run()
    {
        synchronized (filesToWorker)
        {
            while (true)
            {
                while (filesToWorker.isEmpty())
                {
                    try
                    {
                        filesToWorker.wait();
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                Route route = filesToWorker.poll();
                WorkerHandler worker = workers.poll();
                assert worker != null;
                System.out.println("WorkerDispatcher: Sending route to workerhandler");
                worker.processJob(route);
                // Add the worker to the end of the queue
                workers.add(worker);
            }
        }
    }



}
