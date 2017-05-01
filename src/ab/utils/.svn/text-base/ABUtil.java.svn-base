package ab.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ab.demo.TargetNode;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.real.shape.Poly;

public class ABUtil {
	
	public static int gap = 5; //vision tolerance
	private static TrajectoryPlanner tp = new TrajectoryPlanner();
	private static Random randomGenerator = new Random();

	/*
	 * function to detect Scene objects
	 * needed for tree construction
	 */
	public static ABObject SceneDetection(Vision vision, List<ABObject> objects, List<ABObject> pigs, List<ABObject> PigsObjects, List<ABObject> tnts, List<Poly> hills) {
		// get all objects: wood, stone, ice
		List<ABObject> objectsReal = vision.findBlocksRealShape();
		List<ABObject> objectsMBR  = vision.findBlocksMBR();
	
		// Keep the representation which detects the objects correctly
		if(objectsReal.size() - objectsMBR.size() <= -10){
			objects.addAll(objectsMBR);
			System.out.println("Tree construction under MBR representation....");
		}
		else{
			System.out.println("Tree construction under REAL representation...");
			objects.addAll(objectsReal);
		}
		
		// detect tnts and add them to list used for tree construction
		tnts.addAll(vision.getMBRVision().findTNTs());
		for(int i=0; i<tnts.size(); i++)
			objects.add(tnts.get(i));
		
		// findRealShape() mis-detects Pigs, so we use findPigsMBR() for Pig detection
		pigs.addAll(vision.findPigsMBR());
			
		// pop "empty" and "unknown" objects from our list
		if(!objects.isEmpty()){
			for (int i=0; i<objects.size(); i++){
				String test = new String(objects.get(i).type.toString());
				
				if(objects.get(i).isEmpty()){
					//System.out.println("Empty object removed....");
					objects.remove(i);
					i = i-1;
				}
				else if (test.equals("Unknown")){
					//System.out.println("Unknown object removed....");
					objects.remove(i);
					i = i-1;
				}
				test = null;
			}
		}
		
		// add pigs and rolling stones in a list
		List<ABObject> PigsAndRolling = new ArrayList<ABObject>();
		if(!pigs.isEmpty())
			PigsAndRolling.addAll(pigs);			
						
		if(!objects.isEmpty()) {
			for (int i=0; i<objects.size(); i++){
				String test = new String(objects.get(i).type.toString());
						
				if (test.equals("Stone") && objects.get(i).shape.toString().equals("Circle")) {
					PigsAndRolling.add(objects.get(i));
				}				
				test = null;
			}
		}
			
		// find most distant pig or rolling stone in the scene
		// to make the most "right" Nodes infeasible
		ABObject mostDistantObj = null;
		if(!PigsAndRolling.isEmpty()){
			mostDistantObj = PigsAndRolling.get(0);
					
			for(int i=1; i<PigsAndRolling.size(); i++) {
				if(PigsAndRolling.get(i).getCenterX() > mostDistantObj.getCenterX()) {
					mostDistantObj = PigsAndRolling.get(i);
				}
			}
		}
		
		// list of objects used at tree creation 
		PigsObjects.addAll(pigs);
		PigsObjects.addAll(objects);

		// Discover hills inside the scene
		List<ABObject> hillstmp = vision.findHills();
		
		for(int i=0; i<hillstmp.size(); i++)
			hills.add((Poly)hillstmp.get(i));
	
		hillstmp.clear();
		hillstmp = null;
		
		// return the most right object to make nodes infeasible
		return mostDistantObj;
	}
	
	/*
	 *  function for pause 
	 */
	public static void mySleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * instead of tapInterval for White Bird
	 */
	public static int WhitefindGap(List<Point> trajP, Point tpt){
		int loc = 0;
		
		for(int i=0; i<trajP.size(); i++){
			if((tpt.y < trajP.get(i).y) && (tpt.x < trajP.get(i).x)){
				loc = i;
				break;
			}
		}
			
		loc = loc - 10;
		Point tmp = new Point(trajP.get(loc).x, trajP.get(loc).y);
		System.out.println(tpt.y - tmp.y);																		// gap is set after experiments
		if(tpt.y - tmp.y == 0) {
			return 0;
		} else if(tpt.y - tmp.y <= 1) {
			System.out.println("Positive Gradient1");
			return (int) (0);						// gap -> [7,9]
		} else if(tpt.y - tmp.y <= 2){
			System.out.println("Positive Gradient2");
			return (int) (0);	
		}  else if(tpt.y - tmp.y <= 3){
			System.out.println("Positive Gradient3");
			return (int) (8);	
		}  else if(tpt.y - tmp.y <= 4){
			System.out.println("Positive Gradient4");
			return (int) (7);	
		} else	if(tpt.y - tmp.y <= 5){		
			System.out.println("Positive Gradient5");
			return (int) (20);					// gap -> [22,24]
		} else if(tpt.y - tmp.y > 5){		
			System.out.println("Positive Gradient5");
			return (int) (35);	
		} else {
			return (int) 0;
		}
	}

	/*
	 * function to return tapInterval according to bird 
	 */
	public static int tapTimeSelection(ABType bird, TargetNode tpNode, double releaseAngle){
		int tapInterval = 0;
		
		switch (bird) {
			case RedBird:
				tapInterval = 90;											
				break;               
			case YellowBird:
				if(tpNode.getLabel().equals("center")){			// hit the center of the object
					tapInterval = 90;	
					break;
				}
				if(Math.toDegrees(releaseAngle) > 45)		// high-arching trajectory
					tapInterval = 95; 				
				else										// direct trajectory 
					tapInterval = 90; 		
				break; 
			case WhiteBird:
				//System.out.println("Tap performed in case of WhiteBird due to random Node target....");
				tapInterval = 90;		
				break; 
			case BlackBird:
				tapInterval = 160;					
				break; 
			case BlueBird:
				tapInterval = 75; 							
				break;
			default:
				tapInterval = 80;
				break;
		}		
		return tapInterval;
	}
	
	/*
	 * my functions to allocate game Scene
	 */
	public static int findMinX(int xmin, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			if (tmp.x < xmin) {
				xmin = tmp.x;
			}
		}
		return xmin;
	}
		
	public static int findMinY(int ymin, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			if (tmp.y < ymin) {
				ymin= tmp.y;
			}
		}
		return ymin;
	}
		
	public static int findMaxX(int xmax, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			if ((tmp.x+tmp.width) > xmax) {
				xmax = tmp.x+tmp.width;
			}
		}
		return xmax;
	}
	
	public static int findMaxY(int ymax, List<ABObject> object){
		for (int i=0; i<object.size(); i++){
			ABObject tmp = object.get(i);
			
			if ((tmp.y + tmp.height) > ymax) {
				ymax = tmp.y+tmp.height;
			}
		}
		return ymax;
	}
	
	/*
	 * function to allocate game scene
	 * game scene includes all game objects
	 */
	public static Rectangle findOurRoom(List<ABObject> pigs, List<ABObject> objects){
		int xminP = 0, yminP = 0, xmaxP = 0, ymaxP = 0;
		int xminO = 0, yminO = 0, xmaxO = 0, ymaxO = 0;
		int xmin = 100000, ymin = 100000, xmax = -1, ymax = -1;
		
		if (pigs !=null){
			xminP = findMinX(xmin,pigs);
			yminP = findMinY(ymin,pigs);
			xmaxP = findMaxX(xmax,pigs);
			ymaxP = findMaxY(ymax,pigs);
		}
		
		if (objects !=null){
			xminO = findMinX(xmin,objects);
			yminO = findMinY(ymin,objects);
			xmaxO = findMaxX(xmax,objects);
			ymaxO = findMaxY(ymax,objects);
		}
		
		if(yminP < yminO)
			ymin = yminP;
		else
			ymin = yminO;
		
		if(xminP < xminO)
			xmin = xminP;
		else
			xmin = xminO;
		
		if(xmaxP > xmaxO)
			xmax = xmaxP;
		else
			xmax = xmaxO;
		
		if(ymaxP > ymaxO)
			ymax = ymaxP;
		else
			ymax = ymaxO;
		
		// compute width and height of our room
		int h = ymax-ymin;
		int w = xmax-xmin;
		
		return (new Rectangle(xmin,ymin,w,h));
	}
	
	// If o1 supports o2, return true
	public static boolean isSupport(ABObject o2, ABObject o1)
	{
		if(o2.x == o1.x && o2.y == o1.y && o2.width == o1.width && o2.height == o1.height)
				return false;
		
		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;
		
		int ey_o2 = o2.y + o2.height;
		if(
			(Math.abs(ey_o2 - o1.y) < gap)
			&&  
 			!( o2.x - ex_o1  > gap || o1.x - ex_o2 > gap )
		  )
	        return true;	
		
		return false;
	}
	
	//Return a link list of ABObjects that support o1 (test by isSupport function ). 
	//objs refers to a list of potential supporters.
	//Empty list will be returned if no such supporters. 
	public static List<ABObject> getSupporters(ABObject o2, List<ABObject> objs)
			{
				List<ABObject> result = new LinkedList<ABObject>();
				//Loop through the potential supporters
		        for(ABObject o1: objs)
		        {
		        	if(isSupport(o2,o1))
		        		result.add(o1);
		        }
		        return result;
			}

	//Return true if the target can be hit by releasing the bird at the specified release point
	public static boolean isReachable(Vision vision, Point target, Shot shot)
	{ 
		//test whether the trajectory can pass the target without considering obstructions
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy()); 
		int traY = tp.getYCoordinate(vision.findSlingshotMBR(), releasePoint, target.x);
		if (Math.abs(traY - target.y) > 100)
		{	
			//System.out.println(Math.abs(traY - target.y));
			return false;
		}
		boolean result = true;
		List<Point> points = tp.predictTrajectory(vision.findSlingshotMBR(), releasePoint);		
		for(Point point: points)
		{
		  if(point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
			for(ABObject ab: vision.findBlocksMBR())
			{
				if( 
						((ab.contains(point) && !ab.contains(target))||Math.abs(vision.getMBRVision()._scene[point.y][point.x] - 72 ) < 10) 
						&& point.x < target.x
						)
					return false;
			}
		  
		}
		return result;
	}
}
