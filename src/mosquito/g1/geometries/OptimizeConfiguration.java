package mosquito.g1.geometries;

import java.awt.geom.Line2D;
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
		//config.addLight(center)
		//need to add uniqueness check
		for(int i=0; i<numLights-1; i++)
		{
			double []xs = new double[36];
			double []ys = new double[36];
			
			//xs[0] = currentConfig.
			//ys[0]
			double increment = radius / 9.0;
			for(double j=-9; j<10; j++)
			{
				double x = radius + ( j * increment );
			}
		}
		
		return config;
	}

}
