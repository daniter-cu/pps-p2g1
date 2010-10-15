package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import mosquito.sim.Light;

public class OptimizeConfiguration {
	private Light[] seedLights;
	private Set<Line2D> walls;
	private LinkedList<LightConfiguration> randConfigs = new LinkedList<LightConfiguration>();
	private int numLights;
	private double radius = 20;

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
		
		LightConfiguration config = new LightConfiguration();
		Point2D seedLight = currentConfig.getLights().get(0);
		config.addLight(seedLight);
		
		//need to add uniqueness check
		//need to increment over all current lights
		//need to calculate area for all points
		//need to weight areas and choose one at random
		//need to keep track of pruned points
		double []xs = new double[36 * numLights];
		double []ys = new double[36 * numLights];
		for(int i=0; i<numLights-1; i++)
		{
			Point2D newestLight = config.getLights().get(i);
			double centerX = newestLight.getX();
			double centerY = newestLight.getY();
			int baseIndex = 36 * i;
			
			xs[baseIndex] = centerX - radius;
			ys[baseIndex] = centerY;
			xs[baseIndex+35] = centerX + radius;
			ys[baseIndex+35] = centerY;
			double increment = radius / 9.0;
			for(int j=baseIndex; j<baseIndex + 17; j++)
			{
				double x = radius + ( ((double)j-8.0) * increment );
				double curY = getY(x, centerX, centerY);
				
				xs[(2*j)+1] = x;
				ys[(2*j)+1] = curY;
				
				xs[(2*j)+2] = x;
				ys[(2*j)+2] = -1.0 * curY;
			}
		}
		
		return config;
	}
	
	//calculate the positive y value on the circle, given the x
	private double getY(double x, double centerX, double centerY)
	{
		return Math.sqrt(Math.pow(radius, 2) - Math.pow(x - centerX, 2)) + centerY;
	}

}
