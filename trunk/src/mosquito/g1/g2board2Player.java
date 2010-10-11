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
			Light l1 = new Light(76,50,80,20,0);
			Light l2 = new Light(63,63,40,20,20);
			Light l3 = new Light(50,77,80,20,40);
			ret.add(l1);
			ret.add(l2);
			ret.add(l3);
			
		}
		if(numLights == 10)
		{
			
		}
		return ret;
	}



	@Override
	public Collector getCollector() {
		Collector c;
		if(numLights == 3)
			c = new Collector(65,61);
		else if(numLights == 10)
			c = new Collector(50,51);
		else
			c = new Collector(50,51);

		return c;
	}

	
}
