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
	private LinkedList<LightWithArea> points;
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
				
				currentConfig = findRandomConfiguration(currentConfig);
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
	private LightConfiguration findRandomConfiguration(LightConfiguration currentConfig) {
		long curTime = System.currentTimeMillis();
		//instantiate local variables
		config = new LightConfiguration(currentConfig);
		currentLights = config.getLights();
		points = new LinkedList<LightWithArea>();
		double currentRadius = RADIUS;

		//create a random configuration
		int lightsAdded = 1;
		int lastLightsAdded = 1;
		while(lightsAdded < numLights)
		{
			//reset local variables
			totalArea = 0;
			
			if(lastLightsAdded != lightsAdded)
			{
				//recalculate marginal area for all seen points
				ListIterator<LightWithArea> it = points.listIterator();
				while(it.hasNext())
				{
					LightWithArea current = it.next();
					area = LightConfiguration.marginalArea(current.light, currentLights);
					if(area < AREA_THRESHOLD)
					{
						it.remove();
					}
					else
					{
						current.marginalArea = area;
						totalArea += area;
					}
				}
			}
			
			addAllPointsAroundCircle(lightsAdded - 1, lightsAdded, currentRadius);
			
			//weight by marginal area and choose a random point for light placement
			int next = (int) (Math.random() * totalArea);
			ListIterator<LightWithArea> litr = points.listIterator();
			//Point2D curLight = litr.next();
			//double curArea = itr.next();
			
//			for(double j=curArea; j<next; j += curArea)
//			{
//					curArea = itr.next();
//					curLight = litr.next();
//			}
			
			LightWithArea curLight;
			LightWithArea maxLight = new LightWithArea();
			while(litr.hasNext())
			{
				curLight = litr.next();
				
				if(maxLight.marginalArea == -1)
					maxLight = curLight;
				else if(curLight.marginalArea > maxLight.marginalArea)
				{
					maxLight = curLight;
				}
			}
			
			//check that a usable light position was found
			if(maxLight.marginalArea < 1)
			{
				currentRadius--;
				if(currentRadius < 1)
				{
					if(currentLights.size() > 0)
					{
						fillWithDummies(lightsAdded);
						return config;
					}
					else
						return null;
				}
				else
				{
					for(int i=0; i<currentLights.size() - 1; i++)
						addAllPointsAroundCircle(i, lightsAdded, currentRadius);
				}
			}
			else
			{
				//add chosen light to the configuration and remove it from potentials 
				lightsAdded++;
				currentRadius = RADIUS;
				config.addLight(maxLight.light);
				points.remove(maxLight);
				
				if(config.areaCovered() > 5000)
				{
					fillWithDummies(lightsAdded);
					return config;
				}
			}
		}
		
		//System.out.println(System.currentTimeMillis() - curTime);
		return config;
	}
	
	private void addAllPointsAroundCircle(int index, int lightsAdded, double currentRadius)
	{
		//add leftmost point to potentials
		Point2D newestLight = currentLights.get(index);
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
	}
	
	private void fillWithDummies(int lightsAdded)
	{
		for(int i=lightsAdded; i<numLights; i++)
			config.addLight(new Point2D.Double(-1, -1));
	}
	
	private void addIfValid(double x, double y, Point2D l)
	{
		reachable = config.isReachableFromConfiguration(l);
		area = LightConfiguration.marginalArea(l, currentLights);
		if(reachable && area > AREA_THRESHOLD && !points.contains(l))
		{
			points.add(new LightWithArea(l, area));
			totalArea += area;
		}
	}
	
	//calculate the positive y value on the circle, given the x
	private double getY(double x, double centerX, double centerY, boolean negate)
	{
		return (negate ? -1 : 1) * Math.sqrt(Math.pow(RADIUS, 2) - Math.pow(x - centerX, 2)) + centerY;
	}

	private class LightWithArea implements Comparable<LightWithArea>
	{
		public double marginalArea;
		public Point2D light;
		
		public LightWithArea()
		{
			marginalArea = -1;
		}
		
		public LightWithArea(Point2D light, double area)
		{
			this.light = light;
			this.marginalArea = area;
		}

		public int compareTo(LightWithArea other) {
			if(other.light.getX() == this.light.getX() 
					&& other.light.getY() == this.light.getY() 
					&& other.marginalArea == this.marginalArea)
				return 0;
			else if(other.marginalArea < this.marginalArea)
				return 1;
			else
				return -1;
		}
	}
}
