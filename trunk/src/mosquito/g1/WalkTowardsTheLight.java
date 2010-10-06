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
	private int INTERVAL = 60;
	private int OFFSET = 9;
	private int ON_TIME = 15;
	private int BASE = 50;
	
	@Override
	public String getName() {
		return "Walk Towards The Light 0";
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
	}

	Point2D lastLight = null;
	@Override
	public Set<Light> getLights() {
		HashSet<Light> ret = new HashSet<Light>();
		lastLight = new Point2D.Double(47, 50);
		Light l = new Light(BASE - OFFSET,BASE, INTERVAL,ON_TIME,1);
		Light l2 = new Light(BASE + OFFSET,BASE, INTERVAL,ON_TIME,1+ON_TIME);
		Light l3 = new Light(BASE,BASE - OFFSET,INTERVAL,ON_TIME,1 + 2*ON_TIME);
		Light l4 = new Light(BASE,BASE + OFFSET,INTERVAL,ON_TIME,1 + 3*ON_TIME);
		
		if(numLights == 1)
		{
			ret.add(new Light(50,49,10,5,1));
		}
		if(numLights > 3)
		{
			ret.add(l);
			ret.add(l2);
			ret.add(l3);
			ret.add(l4);
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
			ret.add(new Light(0,0,0,0,1));
		}
		for(int i=4; i<numLights; i++)
			ret.add(new Light(0,0,0,0,1));
		return ret;
	}

	@Override
	public Collector getCollector() {
		Collector c = new Collector(50,50);
		return c;
	}


}

