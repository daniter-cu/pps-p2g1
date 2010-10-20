package mosquito.g1.geometries;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A class to handle the orchestration of building an optimum configuration
 * @author Dan Wilkey
 */
public class OptimizeConfiguration {
	private static final double RADIUS = 20;
	private static final double AREA_THRESHOLD = 0;
	private static final double EDGE_POINT_RESOLUTION = 36.0;
	private static final int MAX_ITERATIONS = 100;
	private static final int MAX_CONFIGS = 1;
	private static final int NUM_TOP_CONFIGS = 5;
	private static final double AREA_LIMIT = 5000;

	private LinkedList<Point2D> seedLights;
    private LinkedList<LightConfiguration> randConfigs = new LinkedList<LightConfiguration>();
    private int numLights;
	private LightConfiguration config;
	private List<Point2D> currentLights;
	private LinkedList<LightWithArea> points;
	private double x, y, area, totalArea;
	private boolean reachable;
	
	/**
	 * Constructor
	 * @param seedLights The points where a configuration should be built out from
	 * @param numLights The number of lights to place in each configuration
	 */
	public OptimizeConfiguration(LinkedList<Point2D> seedLights, int numLights) {
		this.seedLights = seedLights;
		this.numLights = numLights;
	}
	
	/**
	 * From the passed values, calculate the optimum light configuration
	 * @return The calculated optimum configuration
	 */
	public LightConfiguration calcOptimumConfig() {
		if(seedLights.size() == 0) {
			System.err.println("No seed lights!");
			return null;
		}
		
		randConfigs = new LinkedList<LightConfiguration>();
		LightConfiguration currentConfig = new LightConfiguration();
		
		//create 100,000 configurations
		int num_iterations = 0;
		for(Point2D p : seedLights) {
			for(int j=0; j < MAX_CONFIGS; j++) {
				currentConfig = new LightConfiguration();
				currentConfig.addLight(p);
				
				currentConfig = findRandomConfiguration(currentConfig);
				if(currentConfig != null) {
					ListIterator<LightConfiguration> it = randConfigs.listIterator();
					boolean inserted = false;
					while(it.hasNext() && !inserted) {
						LightConfiguration config = it.next();
						if(currentConfig.areaCovered() > config.areaCovered()) {
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
	
	public ArrayList<LightConfiguration> calcOptimumConfigs()
	{
		if(randConfigs.size() == 0)
			calcOptimumConfig();
		ArrayList<LightConfiguration> bestConfigs = new ArrayList<LightConfiguration>();
		ListIterator<LightConfiguration> itr = randConfigs.listIterator();
		for(int i=0; i<NUM_TOP_CONFIGS && itr.hasNext(); i++)
		{
			bestConfigs.add(itr.next());
		}
		
		return bestConfigs;
	}

	private LightConfiguration findRandomConfiguration(LightConfiguration currentConfig) {
		//long curTime = System.currentTimeMillis();
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
			ListIterator<LightWithArea> litr = points.listIterator();
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
				
				LightConfiguration tempConfig = new LightConfiguration(config);
				tempConfig.addLight(maxLight.light);
				if(tempConfig.areaCovered() > AREA_LIMIT)
				{
					double difference = AREA_LIMIT - config.areaCovered();
					int bestDepth = tempConfig.findMaxDepth();
					int curDepth = 100;
					litr = points.listIterator();
					while(litr.hasNext())
					{
						curLight = litr.next();
						if(curLight.marginalArea > difference)
						{
							tempConfig = new LightConfiguration(config);
							tempConfig.addLight(curLight.light);
							curDepth = tempConfig.findMaxDepth();
							if(curDepth < bestDepth)
							{
								bestDepth = curDepth;
								maxLight = curLight;
							}
						}
					}
					
					config.addLight(maxLight.light);
					fillWithDummies(lightsAdded);
					return config;
				}
				
				config.addLight(maxLight.light);
				points.remove(maxLight);
			}
		}
		
		//System.out.println(System.currentTimeMillis() - curTime);
		return config;
	}
	
	/**
	 * Calculate what points are around the circle's circumference and add the pertinent ones.
	 * @param index The light to circle around.
	 * @param lightsAdded How many lights are added.
	 * @param currentRadius How far out from the point to look.
	 */
	private void addAllPointsAroundCircle(int index, int lightsAdded, double currentRadius)
	{
		Point2D newestLight = currentLights.get(index);
		double centerX = newestLight.getX();
		double centerY = newestLight.getY();
		
		//add all circumferential points to potentials
		double increment = 360 / EDGE_POINT_RESOLUTION;
		int incrementsPossible = (int) (90 / increment);
		Point2D l;
		double angle;
		for(int j = -incrementsPossible; j <= incrementsPossible; j++) {
		    angle = j * increment;
		    
			l = getPointFromCenter(centerX, centerY, angle, currentRadius, true);
			addIfValid(l);
			
			l = getPointFromCenter(centerX, centerY, angle, currentRadius, false);
            addIfValid(l);
		}
	}
	
	/**
	 * Gets a point from a center given angle and distance
	 * @param centerX The x of the center
	 * @param centerY The y of the center
	 * @param degrees The degrees (between -90 and 90) to travel from the x axis
	 * @param radius How far to move along that angle
	 * @param toTheLeft Whether to move left or right of the center
	 * @return The point computed from these parameters
	 */
	private Point2D getPointFromCenter(double centerX, double centerY, double degrees,
	        double radius, boolean toTheLeft) {
	    x = centerX + (toTheLeft ? -1 : 1) * radius * Math.cos(Math.toRadians(Math.abs(degrees)));
	    
	    Point2D result = new Point2D.Double(x, getY(x, centerX, centerY, radius, (degrees < 0)));
	    
	    return result;
	}
	
	/**
	 * Fill the empty points in the light set with off-board lights
	 * @param lightsAdded How many lights are already added.
	 */
	private void fillWithDummies(int lightsAdded) {
		for(int i=lightsAdded; i<numLights; i++)
			config.addLight(new Point2D.Double(-1, -1));
	}
	
	/**
	 * Add a point to the configuration if it is reachable, unique, and adds enough area.
	 * @param l The light point to check
	 */
	private void addIfValid(Point2D l) {
		reachable = config.isReachableFromConfiguration(l);
		area = LightConfiguration.marginalArea(l, currentLights);
		if(reachable && area > AREA_THRESHOLD && !points.contains(l)) {
			points.add(new LightWithArea(l, area));
			totalArea += area;
		}
	}
	
	/**
	 * Calculate the Y value of a circle's edge coordinate based on the x and the circle
	 * @param negate Whether to go above the center or below
	 * @return The y value of the coordinate
	 */
	private double getY(double x, double centerX, double centerY, double radius, boolean negate)
    {
        return (negate ? -1 : 1) * Math.sqrt(Math.pow(radius, 2) - Math.pow(x - centerX, 2)) + centerY;
    }

	private class LightWithArea implements Comparable<LightWithArea> {
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
