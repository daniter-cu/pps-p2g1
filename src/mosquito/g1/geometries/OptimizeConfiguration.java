package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import mosquito.sim.Light;

public class OptimizeConfiguration {
	private Light[] seedLights;
	private Set<Line2D> walls;

	public OptimizeConfiguration(Light[] seedLights, Set<Line2D> walls)
	{
		this.seedLights = seedLights;
		this.walls = walls;
	}
	
	public LightConfiguration calcOptimumConfig()
	{
		LightConfiguration currentConfig = new LightConfiguration();
		LinkedList<LightConfiguration> randConfigs = new LinkedList<LightConfiguration>();
		
		for(int i=0; i<seedLights.length; i++)
		{
			for(int j=0; j<1000; j++)
			{
				currentConfig = new LightConfiguration();
				currentConfig.addWalls(walls);
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
		return null;
	}

}
