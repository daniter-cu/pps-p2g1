package mosquito.g1;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mosquito.g1.geometries.SpaceFinder;
import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

public class WalkTowardsTheLight extends Player {
	private Set<Line2D> walls;
	private int numLights;
	private int INTERVAL = 40;
	private int ON_TIME = 20;
	private double []DISPLACEMENTS = {0,14,14,14,14,13,12,11,10,9};
	private int OUTER_ON_TIME = 10;
	//private double BASE = 50;
	private double baseX = 50;
	private double baseY = 50;
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
		//if empty board, return optimal configuration
		if(walls.size() == 0)
			return getCentralShape(DISPLACEMENTS[numLights - 1], 2);
		
		//find largest circle and extract coordinates
		SpaceFinder finder = new SpaceFinder(walls);
		double radius = finder.getRadius();
		//System.err.println(radius);
		baseX = finder.getCenter().getX();
		baseY = finder.getCenter().getY();
		//System.err.println(baseX + ", " + baseY);
		
		//calculate proper displacement
		double offset = 1;
		int levels = 1;
		offset = Math.sqrt( Math.pow(radius, 2) / 2 );
		if(numLights > 7)
		{
			offset /= 2;
			levels = 2;
		}
		
		//System.err.println(offset);
		
		//find relative best spot given configuration
		return getCentralShape(Math.min(offset, 14), levels);
	}
	
	//returns the set of lights representing the largest center shape that can fit on the board
	private HashSet<Light> getCentralShape(double displacement, int levels)
	{
		HashSet<Light> ret = new HashSet<Light>();
		
		Light l1 = new Light(baseX,baseY,1,1,0);
		
		Light l2 = new Light(baseX - displacement,baseY + displacement,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		Light l3 = new Light(baseX + displacement,baseY - displacement,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		Light l4 = new Light(baseX + displacement,baseY + displacement,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		Light l5 = new Light(baseX - displacement,baseY - displacement,INTERVAL,ON_TIME,OUTER_ON_TIME-1);
		
		Light l10 = new Light(baseX - levels*displacement,baseY + levels*displacement,INTERVAL,OUTER_ON_TIME,(2 - levels) * (OUTER_ON_TIME-1));
		Light l11 = new Light(baseX + levels*displacement,baseY - levels*displacement,INTERVAL,OUTER_ON_TIME,(2 - levels) * (OUTER_ON_TIME-1));
		Light l12 = new Light(baseX + levels*displacement,baseY + levels*displacement,INTERVAL,OUTER_ON_TIME,(2 - levels) * (OUTER_ON_TIME-1));
		Light l13 = new Light(baseX - levels*displacement,baseY - levels*displacement,INTERVAL,OUTER_ON_TIME,(2 - levels) * (OUTER_ON_TIME-1));
		
		Light l14 = new Light(baseX-0.5, baseY, 2, 1, 0);
		Light l15 = new Light(baseX+0.5, baseY, 2, 1, 1);
		
		if(numLights < 8)
			ret.add(l1);
		
		if(numLights > 1)
			ret.add(l2);
			
		if(numLights > 2)
			ret.add(l3);
			
		if(numLights > 3)
			ret.add(l4);
		
		if(numLights > 4)
			ret.add(l5);
		
		if(numLights > 5)
			ret.add(l10);
		
		if(numLights > 6)
			ret.add(l11);
		
		if(numLights > 7)
		{
			ret.add(l14);
			ret.add(l15);
		}
		
		if(numLights > 8)
			ret.add(l12);
		
		if(numLights > 9)
			ret.add(l13);
		
		
		
		for(int i=ret.size(); i<numLights; i++)
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
			c = new Collector(baseX-0.5,baseY);
		else
			c = new Collector(baseX, baseY);
		return c;
	}


}

