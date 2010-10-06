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
		Light l = new Light(40,50, 10,10,1);
		ret.add(l);
		Light l2 = new Light(60,50, 10,10,11);
		ret.add(l2);
		Light l3 = new Light(0,0,0,0,1);
		Light l4 = new Light(0,0,0,0,1);
		Light l5 = new Light(0,0,0,0,1);
		ret.add(l3);
		ret.add(l4);
		ret.add(l5);
		return ret;
	}

	@Override
	public Collector getCollector() {
		Collector c = new Collector(50,50);
		return c;
	}


}

