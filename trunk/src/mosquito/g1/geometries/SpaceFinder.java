package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

public class SpaceFinder {

	private double radius;
	private Point2D center;
	
	public SpaceFinder(Set<Line2D> walls)
	{
		walls.add(new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(100,0)));
		walls.add(new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(0,100)));
		walls.add(new Line2D.Double(new Point2D.Double(100,0), new Point2D.Double(100,100)));
		walls.add(new Line2D.Double(new Point2D.Double(0,100), new Point2D.Double(100,100)));
		center = new Point2D.Double(50,50);
		radius = 0;
		Point2D p = new Point2D.Double();
		for(int i=0; i<100; i++)
		{
			for(int j=0; j<100; j++)
			{
				p.setLocation(i, j);
				Iterator<Line2D> iter = walls.iterator();
				double mindist = 50;
				double temp = 0;
				while(iter.hasNext())
				{
					temp = iter.next().ptSegDist(p);
					if(temp < mindist)
						mindist = temp;
					
				}
				if(mindist > radius)
				{
					center.setLocation(i, j);
					radius = mindist;
				}
			}
		}
	}
	
	public double getRadius()
	{
		return radius;
	}
	
	public Point2D getCenter()
	{
		return center;
	}
	
}
