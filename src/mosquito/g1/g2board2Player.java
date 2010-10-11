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

public class g2board2Player extends Player {

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
		return "G2Board2 Player";
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
		this.walls = walls;
	}

	Point2D lastLight = null;
	@Override
	public Set<Light> getLights() {
		HashSet<Light> ret = new HashSet<Light>();
		if(numLights == 3)
		{
			Light l1 = new Light(76,50,40,20,20);
			Light l2 = new Light(63,63,40,40,0);
			Light l3 = new Light(51,76,40,20,20);
			ret.add(l1);
			ret.add(l2);
			ret.add(l3);
			
		}
		if(numLights == 10)
		{
		    ret.add(new Light(44, 80, 1, 1, 0));
		    ret.add(new Light(57,68, 55, 25, 0));
		    ret.add(new Light(70,58, 55, 25, 30));
		    ret.add(new Light(82,45, 55, 25, 60));
		    ret.add(new Light(29,90, 55, 25, 0));
		    ret.add(new Light(12,92, 55, 25, 30));
		    ret.add(new Light(3,83, 55, 25, 60));
		    ret.add(new Light(12,68, 55, 25, 35));
		    ret.add(new Light(22,54, 55, 25, 65));
		    ret.add(new Light(40,52, 55, 25, 40));
		}
		return ret;
	}



	@Override
	public Collector getCollector() {
		Collector c;
		if(numLights == 3)
			c = new Collector(62.5,63);
		else
			c = new Collector(44.5,80);

		return c;
	}

	
}
