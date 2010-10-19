package mosquito.g1.geometries;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class OptimizeConfiguration {
	private LinkedList<Point2D> seedLights;
	private LinkedList<LightConfiguration> randConfigs = new LinkedList<LightConfiguration>();
	private int numLights;
	private final double RADIUS = 20;
	private final double AREA_THRESHOLD = 0;
	private final int MAX_ITERATIONS = 100;
	private final int MAX_CONFIGS = 1;

	private LightConfiguration config;
	private List<Point2D> currentLights;
	private LinkedList<Point2D> points;
	LinkedList<Double> areas;
	Point2D l;
	double x, y, area, totalArea;
	boolean reachable;
	
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
				
				currentConfig = findRandomConfiguration(currentConfig, RADIUS);
				if(currentConfig != null)
				{
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
			}
			
			num_iterations++;
			if(num_iterations > MAX_ITERATIONS)
				break;
		}
		
		return randConfigs.getFirst();
	}
	
	public List<LightConfiguration> calcOptimumConfigs()
	{
		if(randConfigs.size() == 0)
			calcOptimumConfig();
		List<LightConfiguration> bestConfigs = new ArrayList<LightConfiguration>();
		ListIterator<LightConfiguration> itr = randConfigs.listIterator();
		for(int i=0; i<10 && itr.hasNext(); i++)
		{
			bestConfigs.add(itr.next());
		}
		
		return bestConfigs;
	}

	//need to add uniqueness check
	//need to keep track of pruned points
	private LightConfiguration findRandomConfiguration(LightConfiguration currentConfig, double currentRadius) {
		long curTime = System.currentTimeMillis();
		//instantiate local variables
		config = new LightConfiguration(currentConfig);
		currentLights = config.getLights();
		points = new LinkedList<Point2D>();

		//create a random configuration
		for(int i=0; i<numLights-1; i++)
		{
			//reset local variables
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
					totalArea += area;
				}
			}
			
			//add leftmost point to potentials
			Point2D newestLight = currentLights.get(i);
			double centerX = newestLight.getX();
			double centerY = newestLight.getY();
			
			x = centerX - currentRadius;
			y = centerY;
			l = new Point2D.Double(x, y);
			addIfValid(x, y, l);
				
			//add rightmost point to potentials
			x = centerX + currentRadius;
			l = new Point2D.Double(x, y);
			addIfValid(x, y, l);
			
			//add all central points to potentials
			double increment = currentRadius / 9.0;
			for(int j = -8; j <= 8; j++)
			{
				x = centerX + ((double)j * increment);
				y = getY(x, centerX, centerY, false);
				l = new Point2D.Double(x, y);
				
				addIfValid(x, y, l);
				
				y = getY(x, centerX, centerY, true);
				l = new Point2D.Double(x, y);
				
				addIfValid(x, y, l);
			}
			
			//weight by marginal area and choose a random point for light placement
			int next = (int) (Math.random() * totalArea);
			ListIterator<Double> itr = areas.listIterator();
			ListIterator<Point2D> litr = points.listIterator();
			//Point2D curLight = litr.next();
			//double curArea = itr.next();
			
			if(areas.size() != points.size())
			{
				System.err.println("areas and points unequal!");
			}
			//System.err.println("total area: " + totalArea + " next:" + next);
			//System.err.println("list sizes:" + areas.size() + " " + points.size());
			
//			for(double j=curArea; j<next; j += curArea)
//			{
//					curArea = itr.next();
//					curLight = litr.next();
//			}
			
			Point2D curLight;
			double curArea;
			double maxArea = 0;
			Point2D maxLight = new Point2D.Double();
			while(itr.hasNext())
			{
				curArea = itr.next();
				curLight = litr.next();
				if(curArea > maxArea)
				{
					maxArea = curArea;
					maxLight = curLight;
				}
			}
			
			//check that a usable light position was found
			if(maxArea == 0)
			{
				currentRadius--;
				if(currentRadius < 1)
					return null;
				return findRandomConfiguration(currentConfig, currentRadius);
			}
			
			//add chosen light to the configuration and remove it from potentials 
			config.addLight(maxLight);
			points.remove(maxLight);
		}
		
		//System.out.println(System.currentTimeMillis() - curTime);
		return config;
	}
	
	private void addIfValid(double x, double y, Point2D l)
	{
		reachable = config.isReachableFromConfiguration(l);
		area = LightConfiguration.marginalArea(l, currentLights);
		if(reachable && area > AREA_THRESHOLD && !points.contains(l))
		{
			points.add(l);
			areas.add(area);
			totalArea += area;
		}
	}
	
	//calculate the positive y value on the circle, given the x
	private double getY(double x, double centerX, double centerY, boolean negate)
	{
		return (negate ? -1 : 1) * Math.sqrt(Math.pow(RADIUS, 2) - Math.pow(x - centerX, 2)) + centerY;
	}

}
