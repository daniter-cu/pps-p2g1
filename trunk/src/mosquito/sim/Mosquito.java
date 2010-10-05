package mosquito.sim;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

public class Mosquito {
	public Point2D location;
	public boolean caught;
	public Mosquito(Point2D p)
	{
		this.location = p;
		this.caught=  false;
	}
	public void moveInDirection(int d, HashSet<Line2D> walls) {
		d = d -30 + GameConfig.random.nextInt(60);
		if(location.getY() - Math.sin(d*Math.PI/180) < 0 || location.getY() - Math.sin(d*Math.PI/180) > 100 || location.getX() + Math.cos(d*Math.PI/180) > 100 || location.getX() + Math.cos(d*Math.PI/180) < 0)
			return;
		Rectangle2D target = new Rectangle2D.Double(location.getX() + Math.cos(d*Math.PI/180) - Board.MOSQUITO_EPSILON, location.getY() - Math.sin(d*Math.PI/180) - Board.MOSQUITO_EPSILON, Board.MOSQUITO_EPSILON*2, Board.MOSQUITO_EPSILON*2);
		for(Line2D l : walls)
		{
			if(l.intersects(target))
				return;
		}
		location.setLocation(location.getX() + Math.cos(d*Math.PI/180), location.getY() - Math.sin(d*Math.PI/180));
	}
}
