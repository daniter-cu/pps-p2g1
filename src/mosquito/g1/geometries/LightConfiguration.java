package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    protected Set<Line2D> connectLights() {
        return null;
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
