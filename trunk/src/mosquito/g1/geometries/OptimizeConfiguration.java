package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import mosquito.sim.Light;

public class OptimizeConfiguration {
	private Light[] seedLights;
	private Set<Line2D> walls;
	private LinkedList<LightConfiguration> randConfigs = new LinkedList<LightConfiguration>();
	private int numLights;
	private double radius = 20;
	private final double AREA_THRESHOLD = 100;

	public OptimizeConfiguration(Light[] seedLights, Set<Line2D> walls, int numLights)
	{
		this.seedLights = seedLights;
		this.walls = walls;
		this.numLights = numLights;
	}
	
	public LightConfiguration calcOptimumConfig()
	{
		LightConfiguration currentConfig = new LightConfiguration();
		
		for(int i=0; i<seedLights.length; i++)
		{
			for(int j=0; j<1000; j++)
			{
				currentConfig = new LightConfiguration();
				currentConfig.addLight(seedLights[i].getLocation());
				
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
		}
		
		return currentConfig;
	}

	private LightConfiguration findRandomConfiguration(
			LightConfiguration currentConfig) {
		
		LightConfiguration config = new LightConfiguration(currentConfig);
		
		//need to add uniqueness check
		//need to keep track of pruned points
		List<Point2D> currentLights = config.getLights();
		double currentArea;
		
		LinkedList<Point2D> points = new LinkedList<Point2D>();
		LinkedList<Double> areas;
		Point2D l;
		double x, y, area, maxArea, totalArea;

		for(int i=0; i<numLights-1; i++)
		{
			currentArea = config.areaCovered();
			maxArea = 0;
			totalArea = 0;
			areas = new LinkedList<Double>();
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
			
			Point2D newestLight = currentLights.get(i);
			double centerX = newestLight.getX();
			double centerY = newestLight.getY();
			
			x = centerX - radius;
			y = centerY;
			l = new Point2D.Double(x, y);
			area = LightConfiguration.marginalArea(l, currentLights);
			if(area > AREA_THRESHOLD)
			{
				if(area > maxArea)
					maxArea = area;
				totalArea += area;
				points.add(l);
				areas.add(area);
			}
				
			
			x = centerX + radius;
			l = new Point2D.Double(x, y);
			area = LightConfiguration.marginalArea(l, currentLights);
			if(area > AREA_THRESHOLD)
			{
				if(area > maxArea)
					maxArea = area;
				totalArea += area;
				points.add(l);
				areas.add(area);
			}
			
			double increment = radius / 9.0;
			for(int j=-8; j<9; j++)
			{
				x = radius + ( (double)j * increment );
				y = getY(x, centerX, centerY);
				l = new Point2D.Double(x, y);
				
				if(area > AREA_THRESHOLD)
				{
					if(area > maxArea)
						maxArea = area;
					totalArea += area;
					points.add(l);
					areas.add(area);
				}
				
				y = -1.0 * y;
				l = new Point2D.Double(x, y);
				
				if(area > AREA_THRESHOLD)
				{
					if(area > maxArea)
						maxArea = area;
					totalArea += area;
					points.add(l);
					areas.add(area);
				}
			}
			
			int next = (int) (Math.random() * totalArea);
			ListIterator<Double> itr = areas.listIterator();
			ListIterator<Point2D> litr = points.listIterator();
			Point2D curLight = litr.next();
			double curArea = itr.next();
			for(int j=0, k=0; j<next; j++, k++)
			{
				if(k > curArea)
				{
					curArea = itr.next();
					curLight = litr.next();
				}
			}
			
			config.addLight(curLight);
		}
		
		return config;
	}
	
	//calculate the positive y value on the circle, given the x
	private double getY(double x, double centerX, double centerY)
	{
		return Math.sqrt(Math.pow(radius, 2) - Math.pow(x - centerX, 2)) + centerY;
	}

}
