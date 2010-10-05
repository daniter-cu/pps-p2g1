
package mosquito.sim;

import java.awt.geom.Line2D;
import java.util.Set;

/**
 *
 * @author Jon Bell
 */
public abstract class Player {

	
    /**
     * Returns the name for this player
     */
    public abstract String getName();
    
    /**
     * Called on the player when it is instantiated
     */
	public void Register()
	{
		//Do nothing is OK!
	}
	
	/**
	 * Called on the player when a new game starts
	 */
	public abstract void startNewGame(Set<Line2D> walls,int NumLights);

	/**
	 * Returns the set of lights that you would like to place. You must place
	 * exactly as many lights as numLights
	 * @return Set of lights
	 */
	public abstract Set<Light> getLights();
	
	/**
	 * Returns the collector that you would like to place
	 * @return
	 */
	public abstract Collector getCollector();

}

