package activity.mapreduce;

import activity.calculations.ActivityCalculator;
import activity.calculations.ActivityStats;

import java.util.HashMap;

public class Reduce
{
    private ActivityCalculator calculator;

    public Reduce()
    {
        calculator = new ActivityCalculator();
    }
    public ActivityStats reduce(HashMap<Integer, ActivityStats> intermediate_results)
    {
        // TODO: Connect all the stats in the intermediate_results hashmap
        return null;
    }
}
