// AngryBer AI Agent // Copyright (c) 2014 by Georgios Papagiannis <gepapagia@gmail.com> and Nikolaos Tziortziotis <ntziorzi@gmail.com>
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ab.vision.ABObject;
import ab.vision.real.shape.Poly;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABType;

public class Feasibility {

	private Rectangle ROOM;
	private TrajectoryPlanner tp;
	
	public Feasibility(Rectangle room, TrajectoryPlanner tp_){
		ROOM = new Rectangle(room);
		tp = tp_;
	}
	
	/*
	 * fills for each Node of our Tree the field sceneFeasible
	 * and if a pig is out of room, then the room gets bigger
	 */
	public void setSceneFeasible(Tree tree){
		ArrayList<Node> PigsOut = new ArrayList<Node>();
			
		// detect "out of scene" Pigs
		for (int i=0; i<tree.LevelSize()-1; i++){
			for (int j=0; j<tree.LevelSize(i); j++){
				Node tmp = tree.GetElement(i, j);
				
				if((tmp.type.equals("Pig")) && !ROOM.contains(tmp.obj)){
					PigsOut.add(tmp);
				}
			}
		}
		Rectangle checkRoom = ROOM;
		// there are Pigs out of scene
		if(PigsOut.size() != 0){
			System.out.println("ATTENTION !!!!! There are " + PigsOut.size() + " pigs out of scene....");
			for(int i=0; i<PigsOut.size(); i++) {
				checkRoom.add(PigsOut.get(i).obj);
			}
		}

		for (int i=0; i<tree.LevelSize()-1; i++){
			for (int j=0; j< tree.LevelSize(i); j++){
				Node tmp = tree.GetElement(i, j);
			
				if(!checkRoom.contains(tmp.obj)) {
					tmp.sceneFeasible = false;
				} else {
					tmp.sceneFeasible = true;
				}
			}
		}
		// free space
		PigsOut.clear();
		PigsOut = null;
	}
	
	/*
	 * function to find out if hills prevent shots 
	 */
	public void feasible_Nodes_Hill(Tree tree, List<Poly> hills, Rectangle slingshot, double limit){
		int n = tree.LevelSize();
		
		for (int i=0; i<n-1; i++){
			int m = tree.LevelSize(i);
			for (int j=0; j<m; j++){
				Node tmp = tree.GetElement(i,j);
			
				for(int a=0; a<tmp.targetNode.size(); a++) {
					shotPrediction(hills,tmp.targetNode.get(a).getReleasePointList(), tmp, slingshot, limit, tmp.targetNode.get(a).getTargetPoint());
				}
				
				for(int k=0; k<tmp.targetNode.size(); k++){
					if(tmp.targetNode.get(k).getReleasePointList().size() == 0){
						tmp.targetNode.remove(k);
						k = k-1;
					}
				}
			}
		}
	}
	
	/*
	 * function to predict the result of a shot
	 * specifically the first point which will hit the shot 
	 * a way to avoid to hit a hill 
	 * 
	 * hills - contains the hills in each level
	 * pts - contains the result of estimateLaunchPoint()
	*/
	public int shotPrediction(List<Poly> hills, ArrayList<Point> pts, Node target, Rectangle slingshot, double limit, Point targetPoint){
		List<Point> lPoints;
		List<Point> hPoints;
		int point = 1;
		
		boolean lowerFlag = false;
		boolean higherFlag = false, rem = false;
		
		// no hills in Game Scene
		if (hills.size() == 0) {
			return(3);
		}
		
		// there are hills 
		if (pts.size() == 1){	
			lPoints = tp.predictTrajectory(slingshot, pts.get(0));
			if(Math.toDegrees(tp.getReleaseAngle(slingshot, pts.get(0))) < 45){
				point = shotPredictionHelp(hills,lPoints, target, limit, targetPoint);
			}
			else{
				point = shotPredictionHelp(hills,lPoints, target, 0, targetPoint);
			}
			
			if(point == 0){				// cannot be hit
				pts.remove(0);
				return(0);
			}
			else if (point == 1){		// can be hit
				return(1);
			}
		}
		else if (pts.size() == 2){
			lPoints = tp.predictTrajectory(slingshot, pts.get(0));
			
			if(Math.toDegrees(tp.getReleaseAngle(slingshot, pts.get(0))) < 45){
				point = shotPredictionHelp(hills,lPoints, target, limit, targetPoint);
			}
			else{
				point = shotPredictionHelp(hills,lPoints, target, 0, targetPoint);
			}
			
			if(point == 0){				// cannot be hit
				pts.remove(0);
				rem = true;
			}
			else if (point == 1){		// can be hit
				lowerFlag = true;
			}
			
			if(rem) {
				hPoints = tp.predictTrajectory(slingshot, pts.get(0));
			}
			else {
				hPoints = tp.predictTrajectory(slingshot, pts.get(1));
			}
			
			if(Math.toDegrees(tp.getReleaseAngle(slingshot, pts.get(0))) < 45){
				point = shotPredictionHelp(hills,hPoints, target, limit, targetPoint);
			}
			else{
				point = shotPredictionHelp(hills,hPoints, target, 0, targetPoint);
			}
			
			if(point == 0){				// cannot be hit
				if(rem) {
					pts.remove(0);
				}
				else {
					pts.remove(1);
				}
			}
			else if (point == 1) {		// can be hit
				higherFlag = true;
			}
		}
		
		if (lowerFlag && higherFlag){
			return(3);
		}
		else if (!lowerFlag && higherFlag){
			return(2);
		}
		else if (lowerFlag && !higherFlag){
			return(1);
		}
	
		// reach this Point of code means that
		// higherFlag = false and lowerFlag = false
		// so Node cannot be hit
		return(0);
	}
	
	/*
	 * help function for ShotPrediction()
	 */
	private int shotPredictionHelp(List<Poly> hills, List<Point> Points, Node target, double limit, Point targetPoint){
		Point checkPoint = new Point();
		int loc = 800;
				
		boolean foundOnTraj = false;
		boolean hillflag = false;
		
		for(int i=0; i<Points.size(); i++){
			if((targetPoint.y < Points.get(i).y) && (targetPoint.x < Points.get(i).x)){
				loc = i;
				foundOnTraj = true;
				break;
			}
		}
		
		if (!foundOnTraj){
			int y1 = targetPoint.y;
			
			for(int a=0; a<Points.size(); a++){
				if((Points.get(a).y >= y1)){
					loc = a;
				}
			}
		}	
	
		// start from target and check backwards the Point List
		checkPoint = Points.get(loc);	
		while(loc > 1){
			hillflag = false;
			Point checkPoint1 = new Point(checkPoint.x, (int)(checkPoint.y+limit/2.0));				// be careful about this threshold
			for(int j=0; j<hills.size(); j++){
				if (hills.get(j).polygon.contains(checkPoint1.x,checkPoint1.y) && checkPoint1.y < target.obj.getCenterY()){
					hillflag = true;
					break;
				}		
			}
			if (hillflag) {
				break;
			}
			
			loc--;
			checkPoint = Points.get(loc);
		}
		checkPoint = null;
		
		if (hillflag) {
			return(0);
		}
		
		return(1);
	}
	
	/*
	 * function to show us if a Node is reachable or not
	 * or if it cannot be hit directly due to other Node 
	 */
	public void isReachable(Tree tree, Rectangle slingshot, Rectangle room, ABType bird, double limit){
		Node comp = null;
		for (int i=0; i<tree.LevelSize()-1; i++){
			for (int j=0; j<tree.LevelSize(i); j++){
				Node tmp = tree.GetElement(i,j);
				
				boolean tmptype = ((!tmp.type.equals("Pig")) && (!tmp.type.equals("TNT")));
				
				if((tmptype) || (bird.equals(ABType.WhiteBird))){		
					for(int b=0; b< tmp.targetNode.size(); b++){	
						for (int a=0; a < tmp.targetNode.get(b).getReleasePointList().size(); a++){
							
							Point LaunchPoint = tmp.targetNode.get(b).getReleasePointList().get(a);
							comp = findRealTarget(tree, slingshot, LaunchPoint, room, tmp.targetNode.get(b).getTargetPoint(), bird);
							
							if(comp != null){
								if (!tmp.equals(comp)){
									tmp.targetNode.get(b).getReleasePointList().remove(a);
									a = a-1;
								}
							}
						}
							
						if(tmp.targetNode.get(b).getReleasePointList().size() == 0){
							tmp.targetNode.remove(b);
							b =	b-1;
						}
					}				
				} 
				// if a Node has no releasePoints then is unreachable
				if(tmp.targetNode.size() == 0) { 
					tmp.reachable = false;
				} else {
					tmp.reachable = true;
				}
			}
		}
	
	}
	
	/*
	 *  for previous function: isReachable()
	 *  for a trajectory finds the real target
	 */
	private Node findRealTarget(Tree tree, Rectangle sling, Point LaunchPoint, Rectangle room, Point targetPoint, ABType bird){
		int pos = 0;
			
		List<Point> trajPoint = tp.predictTrajectory(sling,LaunchPoint);
		
		// find the first Point that is in ourRoom
		for (int i=0; i<trajPoint.size(); i++){
			if (room.contains(trajPoint.get(i))){
				pos = i;
				break;
			}
		}
				
		
		for(int a=pos; a<trajPoint.size(); a++){
			if((targetPoint.y < trajPoint.get(a).y) && (targetPoint.x < trajPoint.get(a).x)) {
				break;
			}
			
			for(int i=0; i<tree.LevelSize()-1; i++){
				for(int j=0; j<tree.LevelSize(i); j++){
				
					Node tmp = tree.GetElement(i, j);
					
					
					if(!bird.equals(ABType.WhiteBird)) {
		  				if (tmp.obj.contains(new Point(trajPoint.get(a).x, trajPoint.get(a).y))){
		  					return(tmp);
		  				}
					} else if (tmp.obj.intersects(new Rectangle(trajPoint.get(a).x, trajPoint.get(a).y, 20, 20))){
							return(tmp);
					}
				}							
			}
		}
		return(null);
	}
	
	/*
	* function to show us if a Node of our Tree 
	* can be hit or not, "good" angle
	* check the sqrt internal term of trajectoryPlanner 
	* and decide if this Node is feasible or infeasible
	*/
	public void feasible_Nodes_Angle(Tree tree, Rectangle slingshot){ 
		for (int i=0; i<tree.LevelSize()-1; i++){
			for (int j=0; j<tree.LevelSize(i); j++){
				Node tmp = tree.GetElement(i, j);
			
				for(int a=0; a<tmp.targetNode.size(); a++)
				{
					boolean feasible = tp.feasibleNodeAngle(slingshot, tmp.targetNode.get(a).getTargetPoint());
					if(!feasible){
						tmp.targetNode.remove(a);
						a = a-1;
					}
				}
				if(tmp.targetNode.size() == 0) {
					tmp.feasible = false;
				} else {
					tmp.feasible = true;
				}
			}
		}
		
	}
	
	/*
	 * fills the field WhiteFeasible of each Node 
	 * and fills the targetNode field 
	 * of the selected WhiteFeasible Nodes
	 */
	public void setWhiteFeasible(Tree tree, List<Poly> hills, Rectangle sling){		
		
		for (int i=0; i<tree.LevelSize()-1; i++){			
			for (int j=0; j<tree.LevelSize(i); j++){
				Node tmp = tree.GetElement(i,j);
				
				if(tmp.sceneFeasible){
					ArrayList<Node> parTmp = tmp.parent;
					
					boolean tmptype = (tmp.type.equals("TNT") || tmp.type.equals("Pig"));
					if(parTmp.get(0).type.equals("Root") || tmptype){
						tmp.WhiteFeasible = true;
						Rectangle line = new Rectangle((int)tmp.obj.getCenterX(),0,1,tmp.obj.y-4);				// 4 above the target													
						
						// check if there is a hill above from the object: split line in cut pieces
						if(!(hills.isEmpty())){
							
							for(int a=0; a<hills.size(); a++){								
								if(hills.get(a).polygon.intersects(line)){
									tmp.WhiteFeasible = false;
									break;
								}
							}

						}
						
						int pieces = 100;
						if(tmp.WhiteFeasible){
							for(int a=1; a<=pieces; a++){
								int point = (int)Math.floor(0 + (a*line.height/pieces)); 
								if(point < sling.y + 1*sling.height) {
									Point targetPoint = new Point((int)line.getCenterX(), point);
									ArrayList<Point> pts = tp.estimateLaunchPoint(sling, targetPoint);
									
									if(pts.size() > 0) {
										tmp.targetNode.add(new TargetNode(targetPoint,pts,"White-UP"));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * make nodes infeasible if they are located right 
	 * from the most distant pig or rolling stone
	 */
	public void makeNodesINFEASIBLE(Tree tree, ABObject object, ABType bird){
		for (int i=0; i<tree.LevelSize()-1; i++){
			for (int j=0; j<tree.LevelSize(i); j++){
				Node tmp = tree.GetElement(i, j);
				
				if(tmp.obj.getCenterX() > (object.x + object.width + 5)){
					tmp.reachable = false;
					tmp.sceneFeasible = false;
					
					if(bird.equals(ABType.WhiteBird))
						tmp.WhiteFeasible = false;
					}
			}
		}
	}
	
	/*
	 * general function to extract the Final feasible Nodes
	 */
	public void CheckFeasibility(Tree tree, ABType bird, Rectangle sling, List<Poly> hills, ABObject mostDistantObj, double limit) {
		
		setSceneFeasible(tree);
		
		boolean WhiteBirdonSling = bird.equals(ABType.WhiteBird);
		
		Rectangle RoomForWhiteBirds = null;
		
		if(WhiteBirdonSling){
			setWhiteFeasible(tree, hills, sling);
			RoomForWhiteBirds = new Rectangle(ROOM.x, 0, ROOM.width, ROOM.y + ROOM.height);
		}
	
		feasible_Nodes_Angle(tree, sling);
		
		if((hills.size() !=0))
			feasible_Nodes_Hill(tree, hills, sling, limit);
		
		if(!WhiteBirdonSling){
			isReachable(tree, sling, ROOM, bird, limit);
			makeNodesINFEASIBLE(tree, mostDistantObj, bird);
		}
		else{
			isReachable(tree, sling, RoomForWhiteBirds, bird, limit);
			makeNodesINFEASIBLE(tree, mostDistantObj, bird);
		}
	}
}
