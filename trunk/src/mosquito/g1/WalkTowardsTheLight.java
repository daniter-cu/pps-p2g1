package mosquito.g1;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

public class WalkTowardsTheLight extends Player {
	private Set<Line2D> walls;
	private int numLights;
	private int INTERVAL = 40;
	private int ON_TIME = 20;
	private int OUTER_ON_TIME = 10;
	private double BASE = 63;
	//private double DISPLACEMENT = 9;
	private double DISPLACEMENT = 14;
	private boolean [][] openGridSpots = new boolean[100][100];
	
	@Override
	public String getName() {
		return "Walk Towards The Light 0";
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
		this.walls = walls;
	}

	Point2D lastLight = null;
	@Override
	public Set<Light> getLights() {
		testPositions();
		HashSet<Light> ret = new HashSet<Light>();
		
		Light l1 = new Light(BASE,BASE,1,1,0);
		
		Light l2 = new Light(BASE - DISPLACEMENT,BASE + DISPLACEMENT,INTERVAL,ON_TIME,9);
		Light l3 = new Light(BASE + DISPLACEMENT,BASE - DISPLACEMENT,INTERVAL,ON_TIME,9);
		Light l4 = new Light(BASE + DISPLACEMENT,BASE + DISPLACEMENT,INTERVAL,ON_TIME,9);
		Light l5 = new Light(BASE - DISPLACEMENT,BASE - DISPLACEMENT,INTERVAL,ON_TIME,9);
		
		Light l6 = new Light(BASE - DISPLACEMENT,BASE + DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		Light l7 = new Light(BASE + DISPLACEMENT,BASE - DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		Light l8 = new Light(BASE + DISPLACEMENT,BASE + DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		Light l9 = new Light(BASE - DISPLACEMENT,BASE - DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		
		Light l10 = new Light(BASE - 2*DISPLACEMENT,BASE + 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0);
		Light l11 = new Light(BASE + 2*DISPLACEMENT,BASE - 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0);
		Light l12 = new Light(BASE + 2*DISPLACEMENT,BASE + 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0);
		Light l13 = new Light(BASE - 2*DISPLACEMENT,BASE - 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0);
		
		Light l14 = new Light(BASE-0.5, BASE, 2, 1, 0);
		Light l15 = new Light(BASE+0.5, BASE, 2, 1, 1);
		
		if(numLights < 8)
			ret.add(l1);
		
		if(numLights > 1 && numLights < 6)
			ret.add(l2);
			
		if(numLights > 2 && numLights < 7)
			ret.add(l3);
			
		if(numLights > 3 && numLights < 9)
			ret.add(l4);
		
		if(numLights > 4 && numLights < 10)
			ret.add(l5);
		
		if(numLights > 5)
		{
			ret.add(l6);
			ret.add(l10);
		}
		
		if(numLights > 6)
		{
			ret.add(l7);
			ret.add(l11);
		}
		
		if(numLights > 7)
		{
			ret.add(l14);
			ret.add(l15);
		}
		
		if(numLights > 8)
		{
			ret.add(l8);
			ret.add(l12);
		}
		
		if(numLights > 9)
		{
			ret.add(l9);
			ret.add(l13);
		}
		
		for(int i=10; i<numLights; i++)
			ret.add(new Light(0,0,0,0,0));
		
		return ret;
	}

	private void testPositions() {
		Point2D light = new Point2D.Double();
		boolean free;
		for(int i=0; i<80; i++)
		{
			for(int j=0; j<80; j++)
			{
				free = true;
				light.setLocation(i, j);
				Iterator<Line2D> iter = walls.iterator();
				while(iter.hasNext())
				{
					if(iter.next().ptSegDist(light) < 10)
						free = false;
				}
				openGridSpots[i][j] = free;
			}
		}
	}

	@Override
	public Collector getCollector() {
		Collector c;
		if(numLights < 8)
			c = new Collector(BASE-0.5,BASE);
		else
			c = new Collector(BASE, BASE);
		return c;
	}


}

