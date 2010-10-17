package mosquito.g1;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.g1.geometries.LightConfiguration;
import mosquito.g1.geometries.OptimizeConfiguration;
import mosquito.g1.geometries.SpaceFinder;
import mosquito.sim.Collector;
import mosquito.sim.GameListener;
import mosquito.sim.Light;
import mosquito.sim.Player;
import mosquito.sim.GameListener.GameUpdateType;

public class WalkTowardsTheLight extends Player {
	private Logger log = Logger.getLogger(this.getClass());
	private Set<Line2D> walls = new HashSet<Line2D>();
	private int numLights;
	private int INTERVAL = 40;
	private int ON_TIME = 20;
	private double []DISPLACEMENTS = {0,14,14,14,14,13,12,11,10,9};
	private int OUTER_ON_TIME = 10;
	//private double BASE = 50;
	private double baseX = 50;
	private double baseY = 50;
    private static double rounds;
	private boolean [][] openGridSpots = new boolean[100][100];
	private boolean isSimulated = false;
	private Collector collector;
	private Set<Light> lights;
	private Collector simCollector;
	private Set<Light> simLights;
	
	@Override
	public String getName() {
		return "Walk Towards The Light";
	}
	
	@Override
	public void startSimulatedGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
	}
	
	private double runSimulator(Set<Light> lights, Collector col)
	{
		isSimulated = true;
		simCollector = col;
		simLights = lights;
		this.runSimulation(5000,new GameListener() {
			
			@Override
			public void gameUpdated(GameUpdateType type) {
				if(type.equals(GameUpdateType.MOVEPROCESSED))
				{
					rounds = getSimulationRounds();
					//log.debug("We had a move happen, " + getSimulationRounds() +", caught: " + getSimulationNumCaught());
				}
				else if(type.equals(GameUpdateType.GAMEOVER))
				{
					rounds = getSimulationRounds();
					//log.debug("Game ended at ticks: " + getSimulationRounds());
				}
			}
		});
		isSimulated = false;
		return rounds;
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
        LightConfiguration.clearBoard();
        if(walls != null)
        	this.walls = walls;
        LightConfiguration.addWalls(this.walls);
        
        if(walls.size() == 0)
			lights = getCentralShape(DISPLACEMENTS[numLights - 1], 2);
		
		SpaceFinder finder = new SpaceFinder(this.walls);
		LinkedList<Point2D> seeds = finder.getSeeds();
		OptimizeConfiguration optimum = new OptimizeConfiguration(seeds, numLights);
		LightConfiguration l = optimum.calcOptimumConfig();
		
		//List<LightConfiguration> bestConfigs = optimum.calcOptimumConfigs();
		//l = getBestConfig(bestConfigs);
		
		l.calculateOptimalDepths();
		System.out.println("printing lights");
		for(Point2D p : l.getLights())
		{
			System.out.println(p.getX() + " " + p.getY());
		}
		
		collector = l.getCollector();
		lights =  l.getActualLights();
	}

	
	private LightConfiguration getBestConfig(List<LightConfiguration> lcs)
	{
		return null;
	}
	
	@Override
	public Set<Light> getLights() {
		if(isSimulated)
			return simLights;
		else
			return lights;
	}
	
	//returns the set of lights representing the largest center shape that can fit on the board
	private HashSet<Light> getCentralShape(double displacement, int levels)
	{
		HashSet<Light> ret = new HashSet<Light>();
		
		Light l1 = new Light(baseX,baseY,1,1,0);
		collector = new Collector(50.5, 50);
		
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
			collector = new Collector(50, 50);
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
		if(isSimulated)
			return simCollector;
		return collector;
	}
}

