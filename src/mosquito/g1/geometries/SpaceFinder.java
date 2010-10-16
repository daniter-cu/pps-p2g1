package mosquito.g1.geometries;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;


public class SpaceFinder {
	
	private Logger log = Logger.getLogger(this.getClass());
	private double radius;
	private Point2D center;
	private LinkedList<OurLight> seeds; 
	private static final int gap = 9;
	
	public SpaceFinder(Set<Line2D> walls)
	{
		seeds = new LinkedList<OurLight>();
		center = new Point2D.Double(50,50);
		radius = 0;
		walls.add(new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(100,0)));
		walls.add(new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(0,100)));
		walls.add(new Line2D.Double(new Point2D.Double(100,0), new Point2D.Double(100,100)));
		walls.add(new Line2D.Double(new Point2D.Double(0,100), new Point2D.Double(100,100)));
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		//center = new Point2D.Double(50,50);
		//radius = 0;
		for(int i=0; i<100; i++)
		{
			for(int j=0; j<100; j++)
			{
				Point2D.Double p = new Point2D.Double(i, j);
				/*Iterator<Line2D> iter = walls.iterator();
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
				}*/
				//find area
				points.add(p);
				double area = LightConfiguration.calculateAreaCovered(points);
				points.remove(p);
				OurLight l = new OurLight(p,area);
				//add and prune
				addNprune(l);
			}
		}
		Collections.shuffle(seeds);
		log.debug("THESE ARE THE SEED POINTS:");
		for(OurLight a : seeds)
		{
			log.debug(a);
		}
	}
	
	private void addNprune(OurLight l)
	{
		boolean added = false;
		boolean useless = false;
		if(seeds.isEmpty())
		{
			seeds.add(l);
			return;
		}
		ListIterator<OurLight> it = seeds.listIterator();
		OurLight seed;
		while(it.hasNext())
		{
			seed = it.next();
			if(added)
			{
				//prune close ones
				if(seed.point.distance(l.point) < gap)
					it.remove();
			}
			else
			{
				//check if larger one within range, if so break
				if(seed.point.distance(l.point) < gap && seed.coverage >= l.coverage)
				{
					useless = true;
					break;
				}
				//insert
				if(l.coverage > seed.coverage)
				{
					//seeds.add(seeds.indexOf(seed), l);
					it.previous();
					it.add(l);
					added = true;
					/*if(l.point.distance(seed.point) < 5)
					{
						it.remove();
					}*/
				}
			}
		}
		if(!added && !useless)
		{
			seeds.add(l);
		}
	}
	
	public LinkedList<Point2D> getSeeds()
	{
		LinkedList<Point2D> sorted = new LinkedList<Point2D>();
		for(OurLight l : seeds)
		{
			sorted.add(l.point);
		}
		return sorted;
	}

	@Deprecated
	public double getRadius()
	{
		return radius;
	}
	
	@Deprecated
	public Point2D getCenter()
	{
		return center;
	}
	
	private class OurLight {
		public Point2D point;
		public double coverage;
		
		public OurLight(Point2D p, double c)
		{
			point = p;
			coverage = c;
		}
		
		public String toString()
		{
			return point.toString();
		}
	}
}
