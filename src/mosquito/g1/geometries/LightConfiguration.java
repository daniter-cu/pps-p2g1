package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.*;

public class LightConfiguration {
    private List<Point2D> lightSet;
    private int centerLightIndex;
    private WallConfiguration board;
    private Point2D shiftAmount;
    
    public LightConfiguration() {
        centerLightIndex = -1;
        lightSet = new ArrayList<Point2D>();
    }
    
    public void addCenterLight(Point2D center) {
        centerLightIndex = lightSet.size();
        this.addLight(center);
    }
    
    public void addLight(Point2D center) {
        lightSet.add(center);
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
        return true;
    }
    
    //TODO: Zack, make this.
    public double areaCovered() {
        return 0.;
    }
}
