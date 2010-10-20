package mosquito.g1;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mosquito.g1.geometries.LightConfiguration;
import mosquito.g1.geometries.OptimizeConfiguration;
import mosquito.g1.geometries.SpaceFinder;
import mosquito.sim.Collector;
import mosquito.sim.GameListener;
import mosquito.sim.Light;
import mosquito.sim.Player;
import mosquito.sim.GameListener.GameUpdateType;

import org.apache.log4j.Logger;

/**
 * The root player for Group 1
 * @author Dan Wilkey, Dan Iter, and Zack Sheppard
 */
public class WalkTowardsTheLight extends Player {
	private Logger log = Logger.getLogger(this.getClass());
	private Set<Line2D> walls = new HashSet<Line2D>();
	private int numLights;
	private int INTERVAL = 40;
	private int ON_TIME = 20;
	private int OUTER_ON_TIME = 10;
	private double baseX = 50;
	private double baseY = 50;
    private static int rounds;
	private boolean [][] openGridSpots = new boolean[100][100];
	private boolean isSimulated = true;
	private Collector collector;
	private Set<Light> lights;
	private static Collector simCollector;
	private static Set<Light> simLights;
	
	private static int MAX_ROUNDS = 3000;
	private static int MIN_ON = 17;
	private static int MAX_ON = 35;
	private static final int TRIALS = 5;
	private final static double []DISPLACEMENTS = {0,14,14,14,14,13,12,11,10,9};
    private final static double GAP_THRESHOLD = 0.6;
	private static double CRITICAL_RADIUS = 2*Math.sqrt(2.0 * Math.pow(9, 2)) + 15.0;
	
	@Override
	public String getName() {
		return "Walk Towards The Light";
	}
	
	@Override
	public void startSimulatedGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
	}
	
	private int runSimulator(Set<Light> lights, Collector col)
	{
		simCollector = col;
		simLights = lights;
		this.runSimulation(MAX_ROUNDS,new GameListener() {
			
			@Override
			public void gameUpdated(GameUpdateType type) {
				if(type.equals(GameUpdateType.MOVEPROCESSED)) {
					rounds = getSimulationRounds();
					//log.debug("We had a move happen, " + getSimulationRounds() +", caught: " + getSimulationNumCaught());
				}
				else if(type.equals(GameUpdateType.GAMEOVER)) {
					rounds = getSimulationRounds();
					//log.debug("Game ended at ticks: " + getSimulationRounds());
				}
			}
		});
		//isSimulated = false;
		return rounds;
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
        LightConfiguration.clearBoard();
        if(walls != null)
        	this.walls = tightenWalls(walls);
        LightConfiguration.addWalls(this.walls);
        ArrayList<LightConfiguration> bestConfigs = new ArrayList<LightConfiguration>();
        
        if(walls.size() == 0) {
        	Set<Light> bestLights = getCentralShape(DISPLACEMENTS[Math.min(numLights - 1, 9)], 2);
        	lights =  bestLights;
        	isSimulated = false;
        	return;
        }
        else
        {
			SpaceFinder finder = new SpaceFinder(this.walls);
			
			if(finder.getRadius() >= CRITICAL_RADIUS)
			{
				//System.out.println(finder.getRadius());
				//System.out.println(CRITICAL_RADIUS);
				baseX = finder.getCenter().getX();
				baseY = finder.getCenter().getY();
				Set<Light> bestLights = getCentralShape(DISPLACEMENTS[Math.min(numLights - 1, 9)], 2);
	        	lights =  bestLights;
	        	isSimulated = false;
	        	return;
			}
			
			LinkedList<Point2D> seeds = finder.getSeeds();
			OptimizeConfiguration optimum = new OptimizeConfiguration(seeds, numLights);
			
			bestConfigs = optimum.calcOptimumConfigs();
        }
		
		for(LightConfiguration config : bestConfigs) {
			config.calculateOptimalDepths();
//			System.out.println("printing lights");
//			for(Light p : config.getActualLights())
//			{
//				System.out.println(p.getX() + " " + p.getY());
//			}
//			System.out.println("area covered: " + config.areaCovered());
		}
		
		LightConfiguration l = getBestConfig(bestConfigs);
		isSimulated = false;
		collector = l.getCollector();
		lights =  l.getActualLights();
		
		//System.out.println("collector: " + collector.getX() + " " + collector.getY());

	}
	
	private LightConfiguration getBestConfig(ArrayList<LightConfiguration> lcs) {
		long startTime = System.currentTimeMillis();
		long maxTime = 1000 * 60 * 30;
		double bestRound = Integer.MAX_VALUE;
		int bestOn = 0;
		int bestGap = 0;
		LightConfiguration best = null;
		for(LightConfiguration lc : lcs)
		{
			for(int on = MIN_ON; on <= MAX_ON; on++)
			{
				double average = 0;
				for(int i=0; i<TRIALS; i++)
				{
				    lc.setOnAndGap(on, 5);
				    //System.out.println("simulation started");
					average += runSimulator(lc.getActualLights(), lc.getCollector());
				    //System.out.println("simulation ended, num rounds: " + temp);
					if(System.currentTimeMillis() - startTime > maxTime)
					{
						best.setOnAndGap(bestOn, bestGap);
						return best;
					}
				}
				average /= (double)TRIALS;
				if(average < bestRound)
				{
					bestRound = average;
					best = lc;
					bestOn = on;
					bestGap = 5;
				}
			}
		}
		
		//System.out.println("best round: " + bestRound);
		best.setOnAndGap(bestOn, bestGap);
		return best;
	}
	
	@Override
	public Set<Light> getLights() {
		if(isSimulated)
			return simLights;
		else
			return lights;
	}
	
	//returns the set of lights representing the largest center shape that can fit on the board
	private HashSet<Light> getCentralShape(double displacement, int levels)	{
		HashSet<Light> ret = new HashSet<Light>();
		
		Light l1 = new Light(baseX,baseY,1,1,0);
		collector = new Collector(baseX + .5,baseY);
		
		int bonus= 0;
		
		if(numLights == 8 || numLights == 5)
			bonus = 7;
		if(numLights == 5)
		{
			bonus += 9;
			INTERVAL += 8;
		}
		
		Light l2 = new Light(baseX - displacement,baseY + displacement,INTERVAL+5,ON_TIME+bonus+4,(OUTER_ON_TIME-1)*0);
		Light l3 = new Light(baseX + displacement,baseY - displacement,INTERVAL+5,ON_TIME+bonus+4,(OUTER_ON_TIME-1)*0);
		Light l4 = new Light(baseX + displacement,baseY + displacement,INTERVAL+5,ON_TIME+bonus+4,(OUTER_ON_TIME-1)*0);
		Light l5 = new Light(baseX - displacement,baseY - displacement,INTERVAL+5,ON_TIME+bonus+4,(OUTER_ON_TIME-1)*0);
		
		Light l10 = new Light(baseX - levels*displacement,baseY + levels*displacement,INTERVAL,OUTER_ON_TIME+4,((2 - levels) * (OUTER_ON_TIME-1))*0);
		Light l11 = new Light(baseX + levels*displacement,baseY - levels*displacement,INTERVAL,OUTER_ON_TIME+4,((2 - levels) * (OUTER_ON_TIME-1))*0);
		Light l12 = new Light(baseX + levels*displacement,baseY + levels*displacement,INTERVAL,OUTER_ON_TIME+4,((2 - levels) * (OUTER_ON_TIME-1))*0);
		Light l13 = new Light(baseX - levels*displacement,baseY - levels*displacement,INTERVAL,OUTER_ON_TIME+4,((2 - levels) * (OUTER_ON_TIME-1))*0);
		
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
		
		if(numLights > 7) {
			ret.add(l14);
			ret.add(l15);
			collector = new Collector(baseX, baseY);
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
	
	private static Set<Line2D> tightenWalls(Set<Line2D> wallSet) {
	    List<Line2D> wallList = new ArrayList<Line2D>(wallSet);
	    Line2D wall, otherWall;
	    Point2D launchPoint;
	    double m, m2, b, b2, x;
	    for(int i = 0; i < wallList.size(); i++) {
	        for(int j = i + 1; j < wallList.size(); j++) {
	            wall = wallList.get(i);
	            otherWall = wallList.get(j);
	            launchPoint = null;
	            
	            if(!wall.intersectsLine(otherWall)) {
	                if(otherWall.ptSegDist(wall.getP1()) < GAP_THRESHOLD) {
	                    launchPoint = wall.getP1();
	                } else if(otherWall.ptSegDist(wall.getP2()) < GAP_THRESHOLD) {
	                    launchPoint = wall.getP2();
	                }
	                
	                if(launchPoint != null) {
	                    if(otherWall.getP1().distance(launchPoint) < GAP_THRESHOLD) {
	                        wallList.add(new Line2D.Double(launchPoint, otherWall.getP1()));
	                    } else if(otherWall.getP2().distance(launchPoint) < GAP_THRESHOLD) {
	                        wallList.add(new Line2D.Double(launchPoint, otherWall.getP1()));
	                    } else {
	                        m = ((otherWall.getP2().getY() - otherWall.getP1().getY()) /
	                                (otherWall.getP2().getX() - otherWall.getP1().getX()));
	                        m2 = 1/m;
	                        b = otherWall.getP2().getY() - (m * otherWall.getP2().getX());
	                        b2 = launchPoint.getY() - (m2 * launchPoint.getX());
	                        x = ((b2 - b) / (m - m2));
	                        Point2D closest = new Point2D.Double(x, (m2 * x + b2));
	                        if(otherWall.contains(closest)) {
	                            wallList.add(new Line2D.Double(launchPoint, closest));
	                        }
	                    }
	                }
	            }
	        }
	    }
	    
	    return (new HashSet<Line2D>(wallList));
	}

	@Override
	public Collector getCollector() {
		if(isSimulated)
			return simCollector;
		return collector;
	}
}

