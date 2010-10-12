package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.util.Set;

public class WallConfiguration {
    Set<Line2D> walls;
    
    public WallConfiguration(Set<Line2D> walls) {
        this.walls = walls;
    }
}
