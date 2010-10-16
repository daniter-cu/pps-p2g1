package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import mosquito.sim.Collector;
import mosquito.sim.Light;

public class LightConfiguration {
	public static final int GAP = 4;
	public static final int ON = 17;
	public static final int CYCLE = (ON * 2) + GAP;
	public static final int START1 = 0;
	public static final int START2 = (START1 + ON + GAP) % CYCLE;
	public static final int START3 = (START2 + ON + GAP) % CYCLE;
	public static final int START4 = (START3 + ON + GAP) % CYCLE;
	public static final int START5 = (START4 + ON + GAP) % CYCLE;
	public static final int START6 = (START5 + ON + GAP) % CYCLE;
	
    public static int LIGHT_RADIUS = 20;
    public static int BOARD_DIMENSION = 100;
    public static double AREA_RESOLUTION = 0.5;
    public static double BASE_AREA = (Math.PI * Math.pow(LIGHT_RADIUS, 2)); 
    
    private ArrayList<Point2D> lightSet;
    private int centerLightIndex;
    private static Set<Line2D> board;
    private double areaCovered = 0;
    
    private Set<Light> lights;
    private Collector c;
    
    public LightConfiguration() {
        centerLightIndex = -1;
        lightSet = new ArrayList<Point2D>();
    }
    
    public LightConfiguration(LightConfiguration other)
    {
        lightSet = new ArrayList<Point2D>(other.lightSet);
        centerLightIndex = other.centerLightIndex;
        areaCovered = other.areaCovered;
    }
    
    public void addCenterLight(Point2D center) {
        centerLightIndex = lightSet.size();
        addLight(center);
    }
    
    public void addLight(Point2D light) {
        areaCovered += marginalArea(light, lightSet);
        lightSet.add(light);
    }
    
    public static void addWalls(Set<Line2D> board) {
        if(board == null) {
            LightConfiguration.board = board;
        }
    }
    
    public static void clearBoard() {
        board = null;
    }
    
    public List<Point2D> getLights() {
        return lightSet;
    }
    
    public void calculateOptimalDepths() {
        int[] bestDepths = new int[0];
        double bestAverage = Double.POSITIVE_INFINITY;
        Map<Integer, Set<Integer>> edges = calculateEdges();
        
        for(int trialCenter = 0; trialCenter < lightSet.size(); trialCenter++) {
            // Create an array for calculating these depths
            int[] depths = new int[lightSet.size()];
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
        
        for(int i=0; i<bestDepths.length; i++)
        {
        	System.out.println(bestDepths[i]);
        }
        
        createLights(bestDepths);
    }
    
    private void createLights(int[] depths)
	{
		lights = new HashSet<Light>();
		Light l;
		for(int i=0; i<depths.length; i++)
		{
			int start = 0;
			Point2D cur = lightSet.get(i);
			
			if(depths[i] == 0)
			{
				l = new Light(cur.getX(), cur.getY(), 1, 1, 0);
				c = new Collector(cur.getX() - 0.5, cur.getY());
			}
			else
			{
				switch(depths[i])
				{
				case 1:
					start = START1;
					break;
				case 2:
					start = START2;
					break;
				case 3:
					start = START3;
					break;
				case 4:
					start = START4;
					break;
				case 5:
					start = START5;
					break;
				case 6:
					start = START5;
					break;
				default:
					start = 5000;
				}
				
				l = new Light(cur.getX(), cur.getY(), CYCLE, ON, start);
			}
			
			lights.add(l);
		}
			
	}
    
    public Set<Light> getActualLights()
    {
    	return lights;
    }
    
    public Collector getCollector()
    {
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
        
        for(int i = 0; i < lightSet.size(); i++) {
            result.put(i, new HashSet<Integer>());
        }
        
        for(int i = 0; i < lightSet.size(); i++) {
            for(int j = i + 1; j < lightSet.size(); j++) {
                Point2D light1 = lightSet.get(i);
                Point2D light2 = lightSet.get(j);
                if(light1.distance(light2) <= LIGHT_RADIUS) {
                    result.get(i).add(j);
                    result.get(j).add(i);
                }
            }
        }
        
        return result;
    }
    
    /**
     * @return The paths that will be traveled by mosquitoes to the center light.
     */
    public Set<Line2D> connectLights() 
    {
        Set<Line2D> lines = new HashSet<Line2D>();
        ArrayList<Point2D> unused = new ArrayList<Point2D>(lightSet);
        Point2D center = lightSet.get(centerLightIndex);
        //remove center light
        for(Point2D p : unused)
        {
            if(p.equals(center));
                unused.remove(p);
        }
        
        //find all lines connected to center light
        for(Point2D p : unused)
        {
            if(p.distance(center) <= 25)
            {
                lines.add(new Line2D.Double(p,center));
                unused.remove(p);
            }
        }
        
        while(!unused.isEmpty())
        {
            Point2D temp = unused.get(0);
            unused.remove(temp);
            for(Point2D p : lightSet)
            {
                if(p.equals(temp))
                    continue;
                if(p.distance(temp) <= 25)
                {
                    lines.add(new Line2D.Double(p,temp));
                }
            }   
        }
        
        return lines;
    }
    
    /**
     * @return Whether all of the lights can pulse to the center light successfully taking into account the shiftAmount.
     */
    public boolean isConfigurationConnected() {
        Set<Line2D> network = connectLights();

        if(board != null) {
            for(Line2D link : network) {
                for(Line2D wall : board) {
                    if(wall.intersectsLine(link)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
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
    
    public static double marginalArea(Point2D newLight, List<Point2D> lightsToIgnore) {
        double area = 0.;
        Line2D connection;
        Point2D current;
        boolean newlyLit;
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

                                for(Point2D oldLight : lightsToIgnore) {
                                    if(wall.intersectsLine(new Line2D.Double(current, oldLight))) {
                                        newlyLit = false;
                                        break;
                                    }
                                }
                            }
                            
                            if(newlyLit) {
                                area += (AREA_RESOLUTION * AREA_RESOLUTION);
                            }
                        }
                    }
                }
            }
        }

        return area;
    }
    
    /**
     * @return The set of (unshifted) light centers that overlap with the specified one.
     */
    private Set<Point2D> lightsOverlapping(int index) {
        Point2D target = lightSet.get(index);
        Set<Point2D> result = new HashSet<Point2D>();
        
        for(int i = 0; i < lightSet.size(); i++) {
            Point2D p = lightSet.get(i);
            if(i != index && target.distance(p) < (2 * LIGHT_RADIUS)) {
                result.add(p);
            }
        }
        
        return result;
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
        return (wall.ptLineDist(light) < LIGHT_RADIUS);
    }
    
    /**
     * @return A set of intersection points between the wall an the perimeter of the light.
     */
    private Set<Point2D> intersectionPoints(Point2D light, Line2D wall) {
        double m = (wall.getX2() - wall.getX1()) / (wall.getY2() - wall.getY1());
        double b = wall.getY1() - (m * wall.getX1());
        double h = light.getX();
        double k = light.getY();
        double r = LIGHT_RADIUS;
        
        double sqrtVal = -(b*b)-(2*b*h*m)+(2*b*k)-Math.pow(h*m, 2)+2*h*k*m-k*k+Math.pow(r*m, 2)+r*r;
        Set<Point2D> result = new HashSet<Point2D>();
        if(sqrtVal > 0) {
            sqrtVal = Math.sqrt(sqrtVal);
            for(int i = 1; i < 2; i++) {
                double x = ((i==2 ? -sqrtVal : sqrtVal) - (b*m+h+k*m)) / (m*m+1);
                Point2D point = new Point2D.Double(x, (m*x+b));
                if(wall.contains(point)) {
                    result.add(point);
                }
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        List<Point2D> lights = new ArrayList<Point2D>();
        Set<Line2D> board = new HashSet<Line2D>();
        int i;
        for(i = 0; !args[i].equals("and"); i++) {
            if(i%2 == 1) {
                lights.add(new Point2D.Double(Double.parseDouble(args[i-1]), Double.parseDouble(args[i])));
            }
        }
        
        i++;
        
        for(int initI = i; i < args.length; i++) {
            if((initI - i)%4 == 3) {
                board.add(new Line2D.Double(new Point2D.Double(Double.parseDouble(args[i-3]), Double.parseDouble(args[i-2])),
                    new Point2D.Double(Double.parseDouble(args[i-1]), Double.parseDouble(args[i]))));
            }
        }
        
        Point2D center = lights.get(0);
        lights.remove(0);
        LightConfiguration.clearBoard();
        LightConfiguration.addWalls(board);
        System.out.println(LightConfiguration.marginalArea(center, lights));
    }
}
