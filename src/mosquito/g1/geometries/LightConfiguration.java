package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import mosquito.sim.Collector;
import mosquito.sim.Light;

/**
 * Storage and utility methods for light configuration
 * @author Zack Sheppard
 */
public class LightConfiguration {
	public int[] START = new int[100];
	
    public static final int LIGHT_RADIUS = 20;
    public static final int BOARD_DIMENSION = 100;
    public static final double AREA_RESOLUTION = 1.0;
    public static final double BASE_AREA = (Math.PI * Math.pow(LIGHT_RADIUS, 2));
    public static final double COLLECTOR_OFFSET = 0.5;
    
    private int cycleGap = 5;
    private int cycleOn = 20;
    
    private int cycleLength = (cycleOn * 2) + cycleGap;
    private double areaCovered = 0;
    private ArrayList<Point2D> lightSet;
    private static Set<Line2D> board;
    
    private Set<Light> actualLights;
    private Collector c;
    
    /**
     * Default empty constructor.
     */
    public LightConfiguration() {
        lightSet = new ArrayList<Point2D>();
    }
    
    /**
     * Cloning constructor.
     * @param other The other configuration to copy.
     */
    public LightConfiguration(LightConfiguration other)
    {
        lightSet = new ArrayList<Point2D>(other.lightSet);
        areaCovered = other.areaCovered;
    }
    
    /**
     * Add a light to the configuration's set.
     * @param light The light to add.
     */
    public void addLight(Point2D light) {
        areaCovered += marginalArea(light, lightSet);
        lightSet.add(light);
    }
    
    /**
     * Set the on-time and the cycle gap, then update the cycle length based on them.
     */
    public void setOnAndGap(int on, int gap) {
        this.cycleOn = on;
        this.cycleGap = gap;
        this.cycleLength = (cycleOn * 2) + cycleGap;
    }
    
    /**
     * Add the board configuration
     */
    public static void addWalls(Set<Line2D> board) {
        LightConfiguration.board = board;
    }
    
    /**
     * Wipe out the stored board.
     */
    public static void clearBoard() {
        board = null;
    }
    
    /**
     * @return the base set of light points
     */
    public List<Point2D> getLights() {
        return lightSet;
    }
    
    /**
     * Calculate the optimal depths for all the lights
     */
    public void calculateOptimalDepths()
    {
    	int[] depths = calculateDepthsPrivate();
    	int max = findMaxDepth(depths);
    	createLights(depths, max);
    }
    
    /**
     * @return the maximum depth
     */
    public int findMaxDepth()
    {
    	int[] depths = calculateDepthsPrivate();
    	return findMaxDepth(depths);
    }
    
    /**
     * Find the actual optimal depths for all the lights
     */
    private int[] calculateDepthsPrivate() {
        int[] bestDepths = new int[0];
        double bestAverage = Double.POSITIVE_INFINITY;
        Map<Integer, Set<Integer>> edges = calculateEdges();
        
        for(int trialCenter = 0; trialCenter < edges.size(); trialCenter++) {
            // Create an array for calculating these depths
            int[] depths = new int[edges.size()];
            // Set all depths to an unreasonable distance
            for(int i = 0; i < depths.length; i++) {
                depths[i] = depths.length + 100;
            }
            
            // Calculate the depths from the trialCenter
            depths[trialCenter] = 0;
            for(int walkOut = 0; walkOut < depths.length; walkOut++) {
                boolean foundOne = false;
                for(int i = 0; i < depths.length; i++) {
                    if(depths[i] == walkOut) {
                        foundOne = true;
                        Set<Integer> endPoints = edges.get(i);
                        for(Integer endPoint : endPoints) {
                            depths[endPoint] = Math.min(depths[endPoint], (walkOut+1));
                        }
                    }
                }
                if(!foundOne) {
                    break;
                }
            }
            
            // If it is a better average, use it
            double currentAverage = averageIntArray(depths);
            if(currentAverage< bestAverage) {
                bestAverage = currentAverage;
                bestDepths = depths;
            }
        }
        
        return bestDepths;
    }
    
    private int findMaxDepth(int[] depths) {
    	int max = 0;
    	for(int j : depths)
    		if( j > max && j < 100 )
    			max = j;
    	
    	return max;
    }
    
    private void createLights(int[] depths, int max) {
    	calculateStarts(max);
    	if(max == 1) {
    		calculateStarts(1);
    	} else if(max == 2) {
    		calculateStarts(2);
    	} else {
    	    calculateStarts(3);
    	}
    	
		actualLights = new HashSet<Light>();
		Light l;
		for(int i=0; i<depths.length; i++)
		{
			Point2D cur = lightSet.get(i);
			
			if(cur.getX() < 0)
			{
				l = new Light(0, 0, 0, 0, 0);
			}
			else if(depths[i] == 0)
			{
				l = new Light(cur.getX(), cur.getY(), 1, 1, START[0]);
				c = new Collector(cur.getX() - COLLECTOR_OFFSET, cur.getY());
				
				boolean intersects = false;
				for(Line2D wall : board)
					if(c.intersects(wall))
						intersects = true;
				
				if(intersects)
				{
					intersects = false;
					c = new Collector(cur.getX() + COLLECTOR_OFFSET, cur.getY());
					for(Line2D wall : board)
						if(c.intersects(wall))
							intersects = true;
				}
				
				if(intersects)
				{
					intersects = false;
					c = new Collector(cur.getX(), cur.getY() + COLLECTOR_OFFSET);
					for(Line2D wall : board)
						if(c.intersects(wall))
							intersects = true;
				}
				
				if(intersects)
				{
					intersects = false;
					c = new Collector(cur.getX(), cur.getY() - COLLECTOR_OFFSET);
					for(Line2D wall : board)
						if(c.intersects(wall))
							intersects = true;
				}
				
				double fix = .0001;
				while(intersects)
				{
					if(intersects)
					{
						intersects = false;
						c = new Collector(cur.getX() + (COLLECTOR_OFFSET + fix), cur.getY());
						for(Line2D wall : board)
							if(c.intersects(wall))
								intersects = true;
					}
					
					if(intersects)
					{
						intersects = false;
						c = new Collector(cur.getX() - (COLLECTOR_OFFSET + fix), cur.getY());
						for(Line2D wall : board)
							if(c.intersects(wall))
								intersects = true;
					}
					
					if(intersects)
					{
						intersects = false;
						c = new Collector(cur.getX(), cur.getY() - (COLLECTOR_OFFSET + fix));
						for(Line2D wall : board)
							if(c.intersects(wall))
								intersects = true;
					}
					
					if(intersects)
					{
						intersects = false;
						c = new Collector(cur.getX(), cur.getY() + (COLLECTOR_OFFSET + fix));
						for(Line2D wall : board)
							if(c.intersects(wall))
								intersects = true;
					}
					
					if(intersects)
						fix += .0001;
					//System.out.println("fix: " + fix);
				}
					
			}
			else
			{
				int startIndex = depths[i];
				if(startIndex > 14)
					startIndex = 14;
				l = new Light(cur.getX(), cur.getY(), cycleLength, cycleOn, START[startIndex]);
			}
			
			actualLights.add(l);
		}
			
	}
    
    private void calculateStarts(int max) {
    	//light next to the collector is always on
    	START[0] = 0;
    	START[max] = 0;
    	
    	for(int i=max-1; i>0; i--)
    		START[i] = (START[i+1] + cycleOn) % cycleLength;
    	
    	for(int i=max+1; i<START.length; i++)
    		START[i] = (START[i-1] + cycleOn + cycleGap) % cycleLength;
	}

	public Set<Light> getActualLights() {
		for(int i=actualLights.size(); i<lightSet.size(); i++)
			actualLights.add(new Light(0,0,0,0,0));
    	return actualLights;
    }
    
    public Collector getCollector() {
    	return c;
    }
    
    private static double averageIntArray(int[] toAverage) {
        int sum = 0;
        for(int i = 0; i < toAverage.length; i++) {
            sum += toAverage[i];
        }
        return sum / ((double) toAverage.length);
    }
    
    private Map<Integer, Set<Integer>> calculateEdges() {
        Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();
        
        boolean []valid = new boolean[lightSet.size()];
        for(int i = 0; i < lightSet.size(); i++) {
        	if(!(lightSet.get(i).getX() < 0))
        	{
        		result.put(i, new HashSet<Integer>());
        		valid[i] = true;
        	}
        	else
        		valid[i] = false;
        }
        
        for(int i = 0, k=0; i < lightSet.size() && k < result.size(); i++) {
        	if(valid[i])
        	{
	            for(int j = i + 1, l =k+1; j < lightSet.size() && l < result.size(); j++) {
	            	if(valid[j])
	            	{
		                Point2D light1 = lightSet.get(i);
		                Point2D light2 = lightSet.get(j);
		                if(areConnected(light1, light2)) {
		                    //System.out.println("Link found between lights "+i+" and "+j);
		                    result.get(k).add(l);
		                    result.get(l).add(k);
		                }
		                
		                l++;
	            	}
	            }
	            
	            k++;
        	}
        }
        
        return result;
    }

    /**
     * @return The total area illuminated by this configuration with the current shift amount.
     */
    public double areaCovered() {
        return areaCovered;
    }
    
    public static double calculateAreaCovered(List<Point2D> lightsCovered) {
        double areaSoFar = 0;
        List<Point2D> lights = new ArrayList<Point2D>();
        
        for(Point2D light : lightsCovered)
        {
            areaSoFar += marginalArea(light, lights);
            lights.add(light);
        }
        
        return areaSoFar;
    }
    
    /**
     * Calculate the area newly illuminated by a light added
     * @param newLight The light being added
     * @param lightsToIgnore The lights already on the board
     * @return The area newly illuminated (approximately)
     */
    public static double marginalArea(Point2D newLight, List<Point2D> lightsToIgnore) {        
        double area = 0.;
        Line2D connection;
        Point2D current;
        boolean newlyLit;
        boolean previouslyLit;
        Set<Line2D> wallsToConsider = wallsOverlapping(newLight);

        for(double x = (newLight.getX() - LIGHT_RADIUS); x <= (newLight.getX() + LIGHT_RADIUS); x+=AREA_RESOLUTION) {
            if(x < BOARD_DIMENSION && x >= 0) {
                for(double y = (newLight.getY() - LIGHT_RADIUS); y <= (newLight.getY() + LIGHT_RADIUS); y+=AREA_RESOLUTION) {
                    if(y < BOARD_DIMENSION && y >= 0) {
                        current = new Point2D.Double(x, y);
                        if(current.distance(newLight) <= LIGHT_RADIUS) {
                            connection = new Line2D.Double(current, newLight);
                            newlyLit = true;
                            for(Line2D wall : wallsToConsider) {
                                if(wall.intersectsLine(connection)) {
                                    newlyLit = false;
                                    break;
                                }
                            }
                            if(newlyLit) {
                                previouslyLit = false;
                                for(Point2D oldLight : lightsToIgnore) {
                                    boolean litByThisLight = true;
                                    if(oldLight.distance(current) > LIGHT_RADIUS) {
                                        litByThisLight = false;
                                    } else {
                                        connection = new Line2D.Double(current, oldLight);
                                        for(Line2D wall : wallsToConsider) {
                                            if(wall.intersectsLine(connection)) {
                                                litByThisLight = false;
                                                break;
                                            }
                                        }
                                    }
                                    if(litByThisLight) {
                                        previouslyLit = true;
                                        break;
                                    }
                                }
                                
                                if(newlyLit && !previouslyLit) {
                                    area += (AREA_RESOLUTION * AREA_RESOLUTION);
                                }
                            }
                        }
                    }
                }
            }
        }

        return area;
    }
    
    public boolean isReachableFromConfiguration(Point2D newLight) {
        for(Point2D light : lightSet) {
            if(areConnected(newLight, light)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean areConnected(Point2D light1, Point2D light2) {
        if(light1.distance(light2) > LIGHT_RADIUS) {
            return false;
        }
        
        Line2D connection = new Line2D.Double(light1, light2);
        Set<Line2D> walls = wallsOverlapping(light1);
        walls.addAll(wallsOverlapping(light2));
        for(Line2D wall : walls) {
            if(wall.intersectsLine(connection)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * @param index The light to check around
     * @return Which walls overlap that light's area after shifting.
     */
    private static Set<Line2D> wallsOverlapping(Point2D target) {
        Set<Line2D> result = new HashSet<Line2D>();
        if(board != null) {
            for(Line2D l : board) {
                if(wallShadows(target, l)) {
                    result.add(l);
                }
            }
        }
        
        return result;
    }
    
    /**
     * @return Whether the wall might enter the light's perimeter.
     */
    private static boolean wallShadows(Point2D light, Line2D wall) {
        return (wall.ptSegDist(light) < LIGHT_RADIUS);
    }
}
