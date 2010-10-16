package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

public class LightConfiguration {
    public static int LIGHT_RADIUS = 20;
    public static double BASE_AREA = (Math.PI * Math.pow(LIGHT_RADIUS, 2)); 
    
    private List<Point2D> lightSet;
    private int centerLightIndex;
    private static Set<Line2D> board;
    private double areaCovered = -1;
    
    public LightConfiguration() {
        centerLightIndex = -1;
        lightSet = new ArrayList<Point2D>();
    }
    
    public LightConfiguration(LightConfiguration other)
    {
    	lightSet = other.lightSet;
    	centerLightIndex = other.centerLightIndex;
    	areaCovered = other.areaCovered;
    }
    
    public void addCenterLight(Point2D center) {
        centerLightIndex = lightSet.size();
        this.addLight(center);
        areaCovered = calculateAreaCovered(0, new HashSet<Point2D>(), 0.0);
    }
    
    public void addLight(Point2D center) {
        lightSet.add(center);
        areaCovered = calculateAreaCovered(0, new HashSet<Point2D>(), 0.0);
    }
    
    public void addWalls(Set<Line2D> board) {
        if(board == null) {
            LightConfiguration.board = board;
        }
    }
    
    public List<Point2D> getLights()
    {
    	return lightSet;
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
        
        for(Line2D link : network) {
            for(Line2D wall : board) {
                if(wall.intersectsLine(link)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * @return The total area illuminated by this configuration with the current shift amount.
     */
    public double areaCovered() {
    	if(areaCovered == -1)
    		areaCovered = calculateAreaCovered(0, new HashSet<Point2D>(), 0.0);
        return areaCovered;
    }
    
    private double calculateAreaCovered(int position, Set<Point2D> lightsCovered, double areaSoFar) {
        areaSoFar += areaForLight(position, lightsCovered);
        lightsCovered.add(lightSet.get(position));
        return calculateAreaCovered(position + 1, lightsCovered, areaSoFar);
    }
    
    // TODO:finish this method
    private double areaForLight(int index, Set<Point2D> lightsToIgnore) {
        double area = BASE_AREA;
        
        
        
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
    
    /**
     * @param index The light to check around
     * @return Which walls overlap that light's area after shifting.
     */
    private Set<Line2D> wallsOverlapping(int index) {
        Point2D target = lightSet.get(index);
        Set<Line2D> result = new HashSet<Line2D>();
        
        for(Line2D l : board) {
            if(wallShadows(target, l)) {
                result.add(l);
            }
        }
        
        return result;
    }
    
    /**
     * @return Whether the wall might enter the light's perimeter.
     */
    private boolean wallShadows(Point2D light, Line2D wall) {
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
}
