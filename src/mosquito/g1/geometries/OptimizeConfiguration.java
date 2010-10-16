package mosquito.g1.geometries;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class OptimizeConfiguration {
	private LinkedList<Point2D> seedLights;
	private LinkedList<LightConfiguration> randConfigs;
	private int numLights;
	private double radius = 20;
	private final double AREA_THRESHOLD = 50;
	private final int MAX_ITERATIONS = 50;
	private final int MAX_CONFIGS = 10;

	public OptimizeConfiguration(LinkedList<Point2D> seedLights, int numLights)
	{
		this.seedLights = seedLights;
		this.numLights = numLights;
	}
	
	public LightConfiguration calcOptimumConfig()
	{
		if(seedLights.size() == 0)
		{
			System.err.println("No seed lights!");
			return null;
		}
		
		randConfigs = new LinkedList<LightConfiguration>();
		LightConfiguration currentConfig = new LightConfiguration();
		
		//create 100,000 configurations
		int num_iterations = 0;
		for(Point2D p : seedLights)
		{
			for(int j=0; j<MAX_CONFIGS; j++)
			{
				currentConfig = new LightConfiguration();
				currentConfig.addLight(p);
				
				currentConfig = findRandomConfiguration(currentConfig);
				ListIterator<LightConfiguration> it = randConfigs.listIterator();
				boolean inserted = false;
				while(it.hasNext() && !inserted)
				{
					LightConfiguration config = it.next();
					if(currentConfig.areaCovered() > config.areaCovered())
					{
						it.previous();
						it.add(currentConfig);
						inserted = true;
					}
				}
				if(!inserted)
					it.add(currentConfig);
			}
			
			num_iterations++;
			if(num_iterations > MAX_ITERATIONS)
				break;
		}
		
		return randConfigs.getFirst();
	}

	//need to add uniqueness check
	//need to keep track of pruned points
	private LightConfiguration findRandomConfiguration(LightConfiguration currentConfig) {
		
		//instantiate local variables
		LightConfiguration config = new LightConfiguration(currentConfig);
		List<Point2D> currentLights;
		LinkedList<Point2D> points = new LinkedList<Point2D>();
		LinkedList<Double> areas;
		Point2D l;
		double x, y, area=0, maxArea, totalArea;
		boolean reachable;

		//create a random configuration
		for(int i=0; i<numLights-1; i++)
		{
			//reset local variables
			currentLights = config.getLights();
			maxArea = 0;
			totalArea = 0;
			areas = new LinkedList<Double>();
			
			//recalculate marginal area for all seen points
			ListIterator<Point2D> it = points.listIterator();
			while(it.hasNext())
			{
				Point2D p = it.next();
				area = LightConfiguration.marginalArea(p, currentLights);
				if(area < AREA_THRESHOLD)
				{
					it.remove();
				}
				else
				{
					areas.add(area);
					if(area > maxArea)
						maxArea = area;
					totalArea += area;
				}
			}
			
			//add leftmost point to potentials
			Point2D newestLight = currentLights.get(i);
			double centerX = newestLight.getX();
			double centerY = newestLight.getY();
			
			x = centerX - radius;
			y = centerY;
			l = new Point2D.Double(x, y);
			reachable  = config.isReachableFromConfiguration(l);
			area = LightConfiguration.marginalArea(l, currentLights);
			if(reachable && area > AREA_THRESHOLD)
			{
				points.add(l);
				areas.add(area);
				if(area > maxArea)
					maxArea = area;
				totalArea += area;
			}
				
			//add rightmost point to potentials
			x = centerX + radius;
			l = new Point2D.Double(x, y);
			reachable  = config.isReachableFromConfiguration(l);
			area = LightConfiguration.marginalArea(l, currentLights);
			if(reachable && area > AREA_THRESHOLD)
			{
				points.add(l);
				areas.add(area);
				if(area > maxArea)
					maxArea = area;
				totalArea += area;
			}
			
			//add all central points to potentials
			double increment = radius / 9.0;
			for(int j=-8; j<9; j++)
			{
				x = radius + ( (double)j * increment );
				y = getY(x, centerX, centerY);
				l = new Point2D.Double(x, y);
				
				reachable  = config.isReachableFromConfiguration(l);
				area = LightConfiguration.marginalArea(l, currentLights);
				if(reachable && area > AREA_THRESHOLD)
				{
					points.add(l);
					areas.add(area);
					if(area > maxArea)
						maxArea = area;
					totalArea += area;
				}
				
				y = -1.0 * y;
				l = new Point2D.Double(x, y);
				
				reachable  = config.isReachableFromConfiguration(l);
				area = LightConfiguration.marginalArea(l, currentLights);
				if(reachable && area > AREA_THRESHOLD)
				{
					points.add(l);
					areas.add(area);
					if(area > maxArea)
						maxArea = area;
					totalArea += area;
				}
			}
			
			//weight by marginal area and choose a random point for light placement
			int next = (int) (Math.random() * totalArea);
			ListIterator<Double> itr = areas.listIterator();
			ListIterator<Point2D> litr = points.listIterator();
			Point2D curLight = litr.next();
			double curArea = itr.next();
			
			//System.err.println("total area: " + totalArea + " next:" + next);
			//System.err.println("list sizes:" + areas.size() + " " + points.size());
			
			for(double j=curArea; j<next; j += curArea)
			{
					curArea = itr.next();
					curLight = litr.next();
			}
			
			//add chosen light to the configuration and remove it from potentials 
			config.addLight(curLight);
			litr.remove();
		}
		
		return config;
	}
	
	//calculate the positive y value on the circle, given the x
	private double getY(double x, double centerX, double centerY)
	{
		return Math.sqrt(Math.pow(radius, 2) - Math.pow(x - centerX, 2)) + centerY;
	}

}
