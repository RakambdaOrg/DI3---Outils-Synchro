package polytech.tours.di.parallel.tsp.parallel;

import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.Solution;
import java.util.Random;
import java.util.concurrent.Callable;

public abstract class Searcher implements Callable<Solution>
{
	private final long duration;
	private final long endTime;
	private final double[][] distances;
	protected Solution solution;
	protected final Random rnd;
	protected Solution best;
	protected final int nodeNumber;
	private boolean stop = false;
	
	public Searcher(long endTime, long duration, Solution solution, Random rnd, Instance instance)
	{
		System.out.println(duration);
		this.endTime = endTime;
		this.duration = duration;
		this.solution = solution;
		this.rnd = rnd;
		this.distances = instance.getDistanceMatrix();
		this.nodeNumber = instance.getN();
	}
	
	public double[][] getDistances()
	{
		return distances;
	}
	
	@Override
	public Solution call() throws Exception
	{
		long start = System.currentTimeMillis();
		while(!stop && (start + duration) > System.currentTimeMillis() && endTime > System.currentTimeMillis())
			loop();
		return best;
	}
	
	public void stop()
	{
		stop = true;
	}
	
	protected abstract void loop();
}
