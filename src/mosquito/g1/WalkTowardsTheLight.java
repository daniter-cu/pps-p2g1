package mosquito.g1;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

public class WalkTowardsTheLight extends Player {
	private Set<Line2D> walls;
	private int numLights;
	private int INTERVAL = 60;
	private int OFFSET = 9;
	private int ON_TIME = 25;
	private int OUTER_ON_TIME = 17;
	private int SEC_ON_TIME = 15;
	private int BASE = 50;
	private int X_DISPLACE = 22;
	private int Y_DISPLACE = 13;
	private int DISPLACEMENT = 13;
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
		lastLight = new Point2D.Double(47, 50);
		Light l = new Light(BASE - OFFSET,BASE, INTERVAL,ON_TIME,0);
		Light l2 = new Light(BASE + OFFSET,BASE, INTERVAL,ON_TIME,ON_TIME);
		Light l3 = new Light(BASE - X_DISPLACE,BASE - Y_DISPLACE,INTERVAL,SEC_ON_TIME,ON_TIME+5);
		Light l4 = new Light(BASE + X_DISPLACE,BASE + Y_DISPLACE,INTERVAL,SEC_ON_TIME,5);
		Light l5 = new Light(BASE + X_DISPLACE,BASE - Y_DISPLACE,INTERVAL,SEC_ON_TIME,5);
		Light l6 = new Light(BASE - X_DISPLACE,BASE + Y_DISPLACE,INTERVAL,SEC_ON_TIME,ON_TIME+5);
		
		if(numLights == 1)
		{
			ret.add(new Light(50,49,10,5,1));
		}
		else if(numLights == 2)
		{
//			ret.add(l);
//			ret.add(l2);
			ret.add(new Light(50, 50, 1, 1, 0));
			ret.add(new Light(BASE - 15,BASE - 10,27,15,0));
		}
		else if(numLights == 3)
		{
//			ret.add(l);
//			ret.add(l2);
//			ret.add(l3);
			ret.add(new Light(50, 50, 1, 1, 0));
			ret.add(new Light(BASE - 15,BASE - 10,27,15,0));
			ret.add(new Light(BASE + 15,BASE + 10,27,15,0));
		}
		if(numLights == 4)
		{
			ret.add(l);
			ret.add(l2);
			ret.add(l3);
			ret.add(l4);
		}
		else if(numLights == 5)
		{
			ret.add(l);
			ret.add(l2);
			ret.add(l3);
			ret.add(l4);
			ret.add(l5);
		}
		else if(numLights == 6)
		{
			ret.add(l);
			ret.add(l2);
			ret.add(l3);
			ret.add(l4);
			ret.add(l5);
			ret.add(l6);
		}
		else if(numLights == 10)
		{
			ret.add(new Light(50, 50, 1, 1, 0));
			ret.add(new Light(BASE - DISPLACEMENT,BASE - DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1));
			ret.add(new Light(BASE + DISPLACEMENT,BASE + DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1));
			ret.add(new Light(BASE - DISPLACEMENT,BASE + DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1));
			ret.add(new Light(BASE + DISPLACEMENT,BASE - DISPLACEMENT,INTERVAL,ON_TIME,OUTER_ON_TIME-1));
			ret.add(new Light(BASE - 2*DISPLACEMENT,BASE - 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0));
			ret.add(new Light(BASE + 2*DISPLACEMENT,BASE + 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0));
			ret.add(new Light(BASE - 2*DISPLACEMENT,BASE + 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0));
			ret.add(new Light(BASE + 2*DISPLACEMENT,BASE - 2*DISPLACEMENT,INTERVAL,OUTER_ON_TIME,0));
			ret.add(new Light(0,0,0,0,1));
		}
		//for(int i=5; i<numLights; i++)
		//	ret.add(new Light(0,0,0,0,1));
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
		Collector c = new Collector(50,51);
		return c;
	}


}

