package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.*;

public class LightConfiguration {
    public static int CONNECTION_RADIUS = 25;
    
    private List<Light2D> lightSet;
    private int centerLightIndex;
    private WallConfiguration board;
    private Point2D shiftAmount;
    
    public LightConfiguration() {
        centerLightIndex = -1;
        lightSet = new ArrayList<Light2D>();
    }
    
    public void addCenterLight(Point2D center) {
        centerLightIndex = lightSet.size();
        this.addLight(center);
    }
    
    public void addLight(Point2D center) {
        lightSet.add(new Light2D(center));
    }
    
    public void addWalls(Set<Line2D> board) {
        this.board = new WallConfiguration(board);
    }
    
    /**
     * Allows you to set how much the configuration should be shifted from (0,0).
     * Used for creating configurations that will be tested at many places on the board.
     */
    public void setShift(double x, double y) {
        shiftAmount = new Point2D.Double(x, y);
    }
    
    /**
     * @return The paths that will be traveled by mosquitoes to the center light.
     */
    public Set<Line2D> connectLights() 
    {
    	Set<Line2D> lines = new HashSet<Line2D>();
    	ArrayList<Light2D> unused = new ArrayList<Light2D>(lightSet);
    	Light2D center = lightSet.get(centerLightIndex);
    	//remove center light
    	for(Light2D p : unused)
    	{
    		if(p.equals(center));
    			unused.remove(p);
    	}
    	
    	//find all lines connected to center light
    	for(Light2D p : unused)
    	{
    		if(p.distance(center) <= CONNECTION_RADIUS)
    		{
    			lines.add(new Line2D.Double(p.getCenter(), center.getCenter()));
    			unused.remove(p);
    		}
    	}
    	
    	while(!unused.isEmpty())
    	{
    		Light2D temp = unused.get(0);
    		unused.remove(temp);
    		for(Light2D p : lightSet)
        	{
    			if(p.equals(temp))
    				continue;
        		if(p.distance(temp) <= 25)
        		{
        			lines.add(new Line2D.Double(p.getCenter(), temp.getCenter()));
        		}
        	}	
    	}
    	
        return lines;
    }
    
    /**
     * @return Whether all of the lights can pulse to the center light successfully taking into account the shiftAmount.
     */
    public boolean isConfigurationConnected() {
        return true;
    }
    
    public double areaCovered() {
        return 0.;
    }
    
    private class Light2D {
        Point2D center;
        
        public Light2D(Point2D center) {
            this.center = center;
        }
        
        public Point2D getCenter() {
            return center;
        }
        
        public double distance(Light2D other) {
            return this.getCenter().distance(other.getCenter());
        }
    }
}