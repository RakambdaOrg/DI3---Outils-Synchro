package polytech.tours.di.parallel.tsp.parallel;

import polytech.tours.di.parallel.tsp.Algorithm;
import polytech.tours.di.parallel.tsp.Instance;
import polytech.tours.di.parallel.tsp.InstanceReader;
import polytech.tours.di.parallel.tsp.Solution;
import java.util.*;
import java.util.concurrent.*;

public class ParallelAlgorithm implements Algorithm
{
	@Override
	public Solution run(Properties config)
	{
		try
		{
			InstanceReader ir = new InstanceReader();
			ir.buildInstance(config.getProperty("instance"));
			Instance instance = ir.getInstance();
			long max_cpu = Long.valueOf(config.getProperty("maxcpu")) * 1000;

			long seed = Long.valueOf(config.getProperty("seed"));
			Random rnd = seed > 0 ? new Random(seed) : new Random();
			long startTime = System.currentTimeMillis();
			long endTime = startTime + max_cpu;
			int threadCount = Integer.valueOf(config.getProperty("nbThreads"));
			ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
			
			int pointsCount = Integer.valueOf(config.getProperty("startingPoints"));
			double ratio = threadCount / (double) pointsCount;
			Solution solution = new Solution();
			//Heuristic.closest(solution, instance, rnd);
			for(int i = 0; i < instance.getN(); i++)
				solution.add(i);
			ArrayList<Future<Solution>> futures = new ArrayList<>();
			for(int i = 0; i < pointsCount; i++)
			{
				switch(Integer.valueOf(config.getProperty("searchID")))
				{
					default:
					case 0:
						futures.add(executorService.submit(new ShuffleReseach(endTime, (long) (ratio * max_cpu), solution.clone(), rnd, instance)));
						break;
					case 1:
						futures.add(executorService.submit(new SwapResearch(endTime, (long) (ratio * max_cpu), solution.clone(), rnd, instance)));
						break;
					case 2:
						futures.add(executorService.submit(new InvertResearch(endTime, (long) (ratio * max_cpu), solution.clone(), rnd, instance)));
						break;
					case 3:
						futures.add(executorService.submit(new Swap2Research(endTime, (long) (ratio * max_cpu), solution.clone(), rnd, instance)));
				}
				Collections.shuffle(solution, rnd);
			}
			
			try
			{
				executorService.awaitTermination(max_cpu, TimeUnit.MILLISECONDS);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			executorService.shutdown();
			
			int count = 0;
			Solution best = null;
			for(Future<Solution> future : futures)
			{
				if(future.isDone())
				{
					try
					{
						if(future.get() != null)
						{
							count++;
							if(best == null || future.get().getOF() < best.getOF())
								best = future.get();
						}
					}
					catch(ExecutionException e)
					{
						System.out.println("Error -> " + e.getCause());
					}
				}
			}
			
			
			System.out.println(String.format("Time: %ds, Searched %d/%d points, Score: %f", (System.currentTimeMillis() - startTime) / 1000, count, pointsCount, best.getOF()));
			
			//return the solution
			return best;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
