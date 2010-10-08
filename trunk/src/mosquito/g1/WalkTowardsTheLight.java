package mosquito.g1;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

public class WalkTowardsTheLight extends Player {
	private int numLights;
	private int INTERVAL = 40;
	private int OFFSET = 9;
	private int ON_TIME = 20;
	private int SEC_ON_TIME = 15;
	private int BASE = 50;
	private int X_DISPLACE = 22;
	private int Y_DISPLACE = 13;
	
	@Override
	public String getName() {
		return "Walk Towards The Light 0";
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
		System.err.println(numLights);
	}

	Point2D lastLight = null;
	@Override
	public Set<Light> getLights() {
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
			ret.add(l);
			ret.add(l2);
		}
		else if(numLights == 3)
		{
			ret.add(l);
			ret.add(l2);
			ret.add(l3);
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
		for(int i=6; i<numLights; i++)
			ret.add(new Light(0,0,0,0,1));
		return ret;
	}

	@Override
	public Collector getCollector() {
		Collector c = new Collector(50,50);
		return c;
	}


}

