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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import Jama.Matrix;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
	
public class Tree {
	private ArrayList<ArrayList<Node>> level_list; 			// list for tree
	private Random ran = new Random();
	private TrajectoryPlanner tp;
	
	// class's constructor
	public Tree(TrajectoryPlanner tp_){
		tp = tp_;
		level_list = new ArrayList<ArrayList<Node>>();
	}
	
	/*
	 * help function to construct level_list 
	 * 	scan game scene in bottom-up fashion to construct tree
	 */
	private boolean findIntersectionList(List<ABObject> objects, int level, Line2D line){
		boolean flag = false;
		ABObject check = null;
		
		if(!objects.isEmpty()){
			for(int i=0; i<objects.size(); i++){
				try{
					check = objects.get(i); 
				} catch(IndexOutOfBoundsException e) {
					System.err.println("Caught get exception in findIntersectionList(): " + e.getMessage());
				}
				Rectangle2D tmp = new Rectangle2D.Double(check.getX(), check.getY(), check.getWidth(), check.getHeight());
				if (tmp.intersectsLine(line)){ 
					level_list.get(level).add(new Node(check, level));
					objects.remove(i);
					i = i-1;
					flag = true;
				}
			}
		}
		return flag;
	}
		
	/*
	 * function to create level-list in its initial form
	 */
	private void create_level_list(Rectangle ourRoom, List<ABObject> Objects_for_Tree, int xmin, int ymin, int root_width){
		int level = 0;
		boolean breakFlag = false;
		
		// level-list construction
		while(true){
			// find the object which is located near to the ground
			int maxobj = findMaxObject(Objects_for_Tree);					
			double ymax = Objects_for_Tree.get(maxobj).getCenterY();
	
			// Line2D to see what intersect in each level of the list we want to create	
			Line2D.Double line = new Line2D.Double((double)0, ymax, (double)840, ymax);
			//System.out.println("x1: " + line.getX1() + " x2: " + line.getX2() + " y1: " + line.getY1() + " y2: " + line.getY2());
			
			level_list.add(level,new ArrayList<Node>());
			boolean interflag = findIntersectionList(Objects_for_Tree, level, line);
			
			if(!interflag){
				System.out.println("No INTERSECTION: Object: " + Objects_for_Tree.get(maxobj).type.toString() + " x: " + Objects_for_Tree.get(maxobj).getCenterX() 
				+ " y: " + Objects_for_Tree.get(maxobj).getCenterY() + " width: " + Objects_for_Tree.get(maxobj).width + " height: " + Objects_for_Tree.get(maxobj).height);
				
				// remove object to construct our Tree properly
				Objects_for_Tree.remove(maxobj);
				level_list.remove(level);
				level--;
			} 
			
			// in case that the "no-intersection" is the last element of Objects_for_Tree
			if(Objects_for_Tree.isEmpty()){ 		// termination means that all objects inserted in our list
				level++;
				breakFlag = true;
				break;
			} 
			else{
				level++;
			}
		}
		
		// After level_list construction, "Root" Node must be inserted on top of level_list
		level_list.add(level, new ArrayList<Node>());
		Rectangle root = new Rectangle(0, ymin-2, 8000, 1);
		
		level_list.get(level).add(new Node(new ABObject(root, ABType.Root), level));
			
		// Error in level_list construction
		if(!breakFlag){
			System.out.println("While didn't break normal.....");
			System.out.println("Something's wrong... It musn't be printed....");
			mySleep(2000);
		}
		else {
			System.out.println("List construction completed: While broke normal!");
		}
	}
	
	// help function to construct level_list
	private int findMaxObject(List<ABObject> list){
		int tmp = 0;
		double ymax =  list.get(0).getCenterY();
		for (int i=1; i<list.size(); i++){
			if (list.get(i).getCenterY() > ymax) {	
				ymax = 	list.get(i).getCenterY();
				tmp = i;
			}	
		}
		return tmp;
	}

	// our modified "Bubblesort" helps to sort each level's nodes, according to their x coordinate
	// and not only, it is also used to order the children and parents, because we're interested in order
	private void bubblesort(ArrayList<Node> list){
		int n = list.size();
		
		for (int i=0; i<n; i++){
			for (int j=1; j<(n-i); j++){
				int a = list.get(j-1).obj.x, b = list.get(j).obj.x;
				if(a > b){
					Node tmp1 = list.get(j);
					list.remove(j);
					Node temp = list.get(j-1);
					list.remove(j-1);
					list.add(j-1, tmp1);
					list.add(j,temp);
				}	
			}
		}	
	}
		
	// sort each level according to obj.x coordinates, small to large
	private void OrderLevelList(){
		for (int i=0; i<level_list.size(); i++){
			if (level_list.get(i).size() > 1){
				bubblesort(level_list.get(i));
			}
		}
	}
	
	public void Print(int typeOfFeatures){
		System.out.println("PRINT CONSTRUCTED TREE");
		System.out.println("TOTAL LEVELS: "+ level_list.size());
		for (int i=level_list.size()-1; i>=0; i--){
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("level: "+ i +" size: "+ level_list.get(i).size());
			for (int j=0; j<level_list.get(i).size(); j++){
				Node tmp = level_list.get(i).get(j);
				
				if(typeOfFeatures == 0){ // Relevant Height feature
					System.out.println("Node "+ j + " type: " + tmp.obj.type + " CenterX: " + tmp.obj.getCenterX() +" CenterY: "+ tmp.obj.getCenterY()
						+ " PerWeight: " + tmp.myWeight + " TopDown: " + tmp.topdown + " BottomUp: "+ tmp.bottomup+ " RelHeight: " + tmp.relevantHeight + " npDistance: " + tmp.NPdistance 
						+ " ExpectedReward: " + tmp.hitvalue);
				}
				else{		// Parents' Cumulative Weight
					System.out.println("Node "+ j + " type: " + tmp.obj.type + " height: " + tmp.obj.height +  " CenterX: " + tmp.obj.getCenterX() +" CenterY: "+ tmp.obj.getCenterY()
						+ " PerWeight: " + tmp.myWeight + " totalWeight: " + tmp.totalWeight + " npDistance: " + tmp.NPdistance 
						+ " ExpectedReward: " + tmp.hitvalue + " Feasible: " + tmp.feasible + " reachable: " + tmp.reachable + " Points: " + tmp.targetNode.size());
				}
			}
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
		}
		System.out.println("END OF LIST PRINTING");
	}
		
	/*
	 * function to merge Tree's Nodes 
	 * according to their material and geometrical properties
	 */
	private void MergeNodesSameLevel(){
		Node toInsert = null;
		for (int i=0; i<level_list.size()-1; i++){
			int m = level_list.get(i).size();
			for (int j=0; j<m-1; j++){ 					// m-1: check next Node 
				Node tmp = level_list.get(i).get(j);
				Node next = level_list.get(i).get(j+1);
				boolean containsPig = false;
				
				boolean typeflag = false, Circleflag = false;
				
				if(tmp.type.equals(next.type)){
					typeflag = true;
				}
				
				if(tmp.obj.shape.equals("Circle") || (next.obj.shape.equals("Circle"))){
					Circleflag = true;
				}
				
				if ((tmp.type.contains("Pig")) || (next.type.contains("Pig"))){
					containsPig = true;
				}
				
				// to check geometrical properties
				if ((!containsPig) && (typeflag) && (!Circleflag)){
					boolean createNode = false;
					int compY = Math.abs(tmp.obj.y-next.obj.y);
					int compH = Math.abs(tmp.obj.height - next.obj.height);
					if (compY <= 10){	
						if(compH <= 5){ 	
							int compare = tmp.obj.x + tmp.obj.width;
							int compW = Math.abs(next.obj.x - compare); 
							if( compW <= 10){		
								createNode = true;
								String newType = new String(tmp.type.concat(next.type)); 
								int toInWidth = tmp.obj.width+next.obj.width;		
								int toInHeight = tmp.obj.height;
								Rectangle RtoInsertRect = new Rectangle(tmp.obj.x, tmp.obj.y, toInWidth, toInHeight);
								ABObject ABOtoInsert = new ABObject(RtoInsertRect, tmp.obj.type);
								ABOtoInsert.angle = tmp.obj.angle;
								ABOtoInsert.area = tmp.obj.area + next.obj.area;
								ABOtoInsert.shape = tmp.obj.shape;
								toInsert = new Node(newType, ABOtoInsert, i);
							}
						}
					}
					
					// Node creation and insertion in right position
					if (createNode){
						// pop and insertion
						level_list.get(i).remove(j+1);
						level_list.get(i).remove(j);
						level_list.get(i).add(j,toInsert);
						m = level_list.get(i).size();
						j--;
						toInsert = null;
					}
				}
			}
		}
	}
			
	private void MergeNodesDifferentLevel(){
		Node toInsert = null;
		int n = level_list.size();
	
		for (int i=0; i<level_list.size()-1; i++){
			for (int j=0; j<level_list.get(i).size(); j++){
				Node thisLevelNode = level_list.get(i).get(j);
				int mNextLevel = level_list.get(i+1).size();
				boolean createNode = false;
				
				for (int k=0; k<level_list.get(i+1).size(); k++){
					Node nextLevelNode = level_list.get(i+1).get(k);
					
					boolean Circleflag = false;
					if(thisLevelNode.obj.shape.equals("Circle") || nextLevelNode.obj.shape.equals("Circle"))
						Circleflag = true;
					
					boolean containsPig = false;
					if ((thisLevelNode.type.contains("Pig")) || (nextLevelNode.type.contains("Pig"))){
						containsPig = true;
					}
						
					// no need to check the other Nodes if next condition returns true
					if (nextLevelNode.obj.x > thisLevelNode.obj.x)
						break;
					
					// to avoid merging something like StoneStone--WoodStone
					String thisLevelNodeType = thisLevelNode.obj.type.toString();
					String nextLevelNodeType = nextLevelNode.obj.type.toString();
					
					if(!thisLevelNodeType.equals(nextLevelNodeType)){
						break;
					}
					
					// to check geometrical properties
					if ((!containsPig) && (!Circleflag)){
						int compX = Math.abs(nextLevelNode.obj.x-thisLevelNode.obj.x);
						int compW = Math.abs(nextLevelNode.obj.width-thisLevelNode.obj.width);
						if (compX <= 5){		
							if (compW <= 5){		
								int compare = nextLevelNode.obj.y+nextLevelNode.obj.height ;
								int compF = Math.abs(thisLevelNode.obj.y - compare);
								if (compF <= 5){		
									createNode = true;
									String newType = thisLevelNode.type.concat("--"+nextLevelNode.type); 		
									int newWidth = thisLevelNode.obj.width;
									int newHeight = thisLevelNode.obj.height + nextLevelNode.obj.height;
									Rectangle RtoInsert = new Rectangle(nextLevelNode.obj.x, nextLevelNode.obj.y, newWidth, newHeight);
									ABObject ABOtoInsert = new ABObject(RtoInsert, thisLevelNode.obj.type); 
									ABOtoInsert.angle = thisLevelNode.obj.angle;
									ABOtoInsert.area = thisLevelNode.obj.area + nextLevelNode.obj.area;
									ABOtoInsert.shape = thisLevelNode.obj.shape;
									toInsert = new Node(newType, ABOtoInsert, i);
								}
							}
							
							// Node creation and insertion in right position
							if(createNode){
								level_list.get(i).remove(j);
								level_list.get(i+1).remove(k);
								level_list.get(i).add(j,toInsert);
								mNextLevel = level_list.get(i+1).size();
								toInsert = null;
								if (mNextLevel == 0){
									level_list.remove(i+1);
									n = level_list.size();
									for(int a=0; a<n; a++){
										for(int b=0; b<level_list.get(a).size(); b++){
											level_list.get(a).get(b).level = a;
										}
									}
									break;
								}	
							}	
						}
					}
				}	
			}
		}
	}
		
	// sort each level according to obj.x coordinates, small to large
	private void OrderChildren(){
		for (int i=1; i<level_list.size(); i++){
			int m = level_list.get(i).size();
		
			for (int j=0; j<m; j++){
				int k=level_list.get(i).get(j).children.size();
				for (int l=0; l<k; l++){
					bubblesort(level_list.get(i).get(j).children);
				}
			}
		}
	}
		
	// for each Node print Children
	private void PrintChildren(){
		System.out.println("CHILDREN PRINT ");
		for(int i=1; i<level_list.size(); i++){
			System.out.println("LEVEL: "+i);
			for (int j=0; j<level_list.get(i).size(); j++){
				Node tmp = level_list.get(i).get(j);
				int child = tmp.children.size();
			
				if (child > 0){
					System.out.println("Node "+ j +" from level: "+ tmp.level +" ABType: "+ tmp.obj.type +" type: "+ tmp.type +" has ---"+ child + "--- child/children");
					for (int k=0; k<child; k++){
						Node tmpCHILD = level_list.get(i).get(j).children.get(k);
						System.out.println(k +"--> Node from level "+ tmpCHILD.level +" ABType: "+ tmpCHILD.obj.type +" type: "+ tmpCHILD.type +" x: "+ tmpCHILD.obj.x);
					}	
					System.out.println(" ");
				}
				else{
					System.out.println("No Children");
				}
			}
		}
	}
	
	// sort each level according to obj.x coordinates, small to large
	private void OrderParents(){
		for (int i=0; i<level_list.size(); i++){
			int m = level_list.get(i).size();
			
			for (int j=0; j<m; j++){
				int k=level_list.get(i).get(j).parent.size();
				for (int l=0; l<k; l++){
					bubblesort(level_list.get(i).get(j).parent);
				}
			}
		}
	}
		
	// for each Node print Parents
	private void PrintParents(){
		System.out.println("PARENTS PRINT ");
		
		for(int i=0; i<level_list.size(); i++){
			System.out.println("LEVEL --> "+i);
			for (int j=0; j<level_list.get(i).size(); j++){
				Node tmp = level_list.get(i).get(j);
				int par = tmp.parent.size();
				
				if (par > 0){
					System.out.println("Node "+j+" from level " +tmp.level +" ABType: " + tmp.obj.type +" x: "+tmp.obj.x+" type: "+tmp.type+" has --"+par+"-- parents");
					for (int k=0; k<par; k++){
						Node tmpPAR = level_list.get(i).get(j).parent.get(k);
						System.out.println("Node " +k+" from level "+ tmpPAR.level+ " ABType: " +tmpPAR.obj.type+ " type: "+tmpPAR.type+" x: "+tmpPAR.obj.x);
					}	
				}
				else{
					System.out.println("ROOT: This is the root of our Tree, no father for him");
				}
			}
		}
	}
	
	/*
	 *  function to construct our Tree
	 */
	public void constructEdges(int ymin){
		for (int i=0; i<level_list.size(); i++){
			for (int j=0; j<level_list.get(i).size(); j++){
				Node tmp = level_list.get(i).get(j);
				// helps to see the intersection between our objects
				Rectangle area = new Rectangle(tmp.obj.x, ymin-4, tmp.obj.width+1, tmp.obj.y-(ymin-4));
						
				int next_level = i+1;
				for (int a=next_level; a<level_list.size(); a++){
					boolean found_child = false;
					for (int k=0; k < level_list.get(a).size(); k++){
						Node tmp_ = level_list.get(a).get(k);
						
						boolean cond = area.intersects(tmp_.obj);
						if (cond){
							level_list.get(a).get(k).children.add(tmp);
							level_list.get(i).get(j).parent.add(tmp_);
							found_child = true;
						}
					}

					if(found_child) {
						break;
					}
				}
				area = null;
			}
		}
	}
	
	/*
	 * function to detect Pig--Stone
	 * Pig with a Hut-Stone
	 */
	private void PigStone(){
		Node toInsert = null;
	
		for (int i=0; i<level_list.size()-1; i++){
			for (int j=0; j<level_list.get(i).size(); j++){
				Node thisLevelNode = level_list.get(i).get(j);
				
				for (int k=0; k<level_list.get(i+1).size(); k++){
					Node nextLevelNode = level_list.get(i+1).get(k);
					boolean containsPigStone = false;
					
					if ((thisLevelNode.type.contains("Pig")) && (nextLevelNode.type.contains("Stone")) && (nextLevelNode.obj.shape.toString().equals("Poly"))) {
						if(thisLevelNode.obj.intersects(nextLevelNode.obj)) {
							containsPigStone = true;
						}
					}
					
					if (nextLevelNode.obj.x > thisLevelNode.obj.x)
						break;

					if (containsPigStone){
						int compX = Math.abs(nextLevelNode.obj.x-thisLevelNode.obj.x);
						if (compX <= 5){
							String newType = thisLevelNode.type;			//.concat("---Stone");
							int newWidth = thisLevelNode.obj.width;
							int newHeight = thisLevelNode.obj.height + nextLevelNode.obj.height;
							Rectangle RtoInsert = new Rectangle(nextLevelNode.obj.x, nextLevelNode.obj.y, newWidth, newHeight);
							ABObject ABOtoInsert = new ABObject(RtoInsert, thisLevelNode.obj.type); 
							ABOtoInsert.angle = thisLevelNode.obj.angle;
							ABOtoInsert.area = thisLevelNode.obj.area + nextLevelNode.obj.area;
							ABOtoInsert.shape = thisLevelNode.obj.shape;
							toInsert = new Node(newType, ABOtoInsert, i);
							
							//System.out.println("Pig and Stone-Hut detected .....");
							
							// Node creation and insertion in right position
							level_list.get(i).remove(j);
							level_list.get(i+1).remove(k);
							level_list.get(i).add(j,toInsert);
							toInsert = null;
							if (level_list.get(i+1).size() == 0){
								level_list.remove(i+1);
								for(int a=0; a<level_list.size(); a++) {
									for(int b=0; b<level_list.get(a).size(); b++) {
										level_list.get(a).get(b).level = a;
									}
								}
								break;
							}	
						}
					}
				}	
			}
		}
	}
	
	/*
	 * function to return the right type of bird
	 */
	private int RightBird(int bird){
		switch(bird){
			case 2: 
				return(0);
			case 3:
				return(1);
			case 4: 
				return(2);
			case 5: 
				return(3);
			case 6: 
				return(4);
			case 14:
				return(2);		// unknown sometimes means blue 
			default:
				System.out.println("Error in RightBird, called by getNodeType...");
				return(-1);
		}
	}
	
	/*
	 * function to return the right type
	 * to get the right regressor for the prediction
	 */
	private int getNodeType(Node tmp, int bird){
		int pointer;
		int point = 0;
		
		switch(tmp.obj.type.ordinal()){
			case 7: // pig
				pointer = RightBird(bird);
				break;
			case 9: // wood
				if (tmp.obj.shape.ordinal() == 2){	// cycle wood
					point = 25;
					pointer = point + RightBird(bird);
				} 
				else {								// simple wood
					point = 5;
					pointer = point + RightBird(bird);
				}
				break;			
			case 10: // stone
				if (tmp.obj.shape.ordinal() == 2){	// cycle stone
					point = 30;
					pointer = point + RightBird(bird);
				}
				else{								// simple stone
					point = 10;
					pointer = point + RightBird(bird);
				}
				break;
			case 8: // ice
				point = 15;
				pointer = point + RightBird(bird);
				break;		
			case 11: // tnt
				point = 20;
				pointer = point + RightBird(bird);
				break;				
			default: // something else that cannot be recognized
				  pointer = -1;
				  System.out.println(tmp.type);
				  System.out.println("Error in recognition in getNodeType, default case....");
				  break;
		}
		return(pointer);
	}
	
	/*
	 *  set for each Node, the type of Regressor that will be used 
	 *  to predict the expected reward
	 */
	private void SetNodeRegressorType(ABType bird) {
		for(int i = 0; i<LevelSize()-1; i++) {
			for(int j=0; j<LevelSize(i); j++) {
				Node tmp = GetElement(i,j);
				tmp.NodeType = getNodeType(tmp, bird.ordinal());
			}
		}
	}
	
	// function to construct the Tree for each Shot
	public void TreeConstruction(Rectangle room, List<ABObject> PigsObjects, int x, int y,
								   int rootWidth, List<ABObject> pigs, List<ABObject> TNT, Rectangle sling, 
								   ABType bird, double limit) {

		boolean PrintFlag = false;
		
		// Total tree's constructions
		create_level_list(room, PigsObjects, x, y, rootWidth);
		
		// Sort level_list according to their x coordinate
		OrderLevelList();
		
		// merge Nodes if needed 
		MergeNodesDifferentLevel();
		MergeNodesSameLevel();
		MergeNodesDifferentLevel();
		
		// detects stones on a pigs' head
		PigStone();
		
		// Sort again level_list according to their x coordinate, due to merge
		OrderLevelList();
		
		// function which constructs our Tree
		constructEdges(y);
		
		// order children for each Node of our Tree
		OrderChildren();

		// order parents for each Node of our Tree
		OrderParents();
		
		// Print Children and Parents only if needed 
		if(PrintFlag){
			PrintChildren();
			PrintParents();
		}
		
		// set the regressor type which will be used for expected reward prediction
		SetNodeRegressorType(bird);
		
		// for each Node, find points that it can be hit
		// if bird that is on sling is not the White Bird
		if(!bird.equals(ABType.WhiteBird))
			findTargetPoints(sling);
	
	}
	
	public void SetFeatures(Rectangle ourRoomNew,  List<ABObject> pigs, List<ABObject> TNT, Rectangle sling, ABType bird, double limit) {
		
		//Distance from nearest pig or TNTs
		setNPdistance(pigs, TNT, sling, bird, limit, ourRoomNew);

		// RelevantHeight in Tree
		SetRelevantHeight();
				
		// set Cumulative Parents Weight
		SetAboveWeight(sling);
	}
	
	/*
	 * function to find targetPoints for each Node
	 * store all elements needed in an ArrayList<TargetNode>
	 * designed specifically for this purpose
	 * used for non-WhiteBirds
	 */
	private void findTargetPoints(Rectangle sling){
		Point th, tw;
		ArrayList<Point> pts;
				
		for (int i=0; i<LevelSize()-1; i++){			
			for (int j=0; j<LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				
				// Target Points are computed only if the Node is in the Game Scene
				if(tmp.sceneFeasible){
					if(tmp.type.equals("Pig") || tmp.type.equals("TNT")){
						Point tpt = tmp.obj.getCenter();
						pts = tp.estimateLaunchPoint(sling, tpt);
						tmp.targetNode.add(new TargetNode(tpt, pts, "PT-center"));		
					}
					else{
						if((Math.abs(tmp.obj.angle - 90) > 20) && (Math.abs(tmp.obj.angle) > 20)){		// Experimental threshold 20 	
							Point tpt = tmp.obj.getCenter();
							pts = tp.estimateLaunchPoint(sling, tpt);
						
							tmp.targetNode.add(new TargetNode(tpt, pts, "center"));							
						}
						else{	
							// TargetPoints for left side
							if(tmp.obj.height > 10){
								th = new Point(tmp.obj.x, tmp.obj.y + tmp.obj.height/4);
								pts = tp.estimateLaunchPoint(sling, th);
				
								tmp.targetNode.add(new TargetNode(th,pts,"left-3/4"));
									
								th = new Point(tmp.obj.x, tmp.obj.y + tmp.obj.height/2);
								pts = tp.estimateLaunchPoint(sling, th);
				
								tmp.targetNode.add(new TargetNode(th,pts,"left-1/2"));
							}
							else{
								th = new Point(tmp.obj.x, tmp.obj.y + tmp.obj.height/2);
								pts = tp.estimateLaunchPoint(sling, th);
			
								tmp.targetNode.add(new TargetNode(th,pts,"left-1/2"));
							}
							
							// targetPoint for up-side
							tw = new Point(tmp.obj.x + tmp.obj.width/2, tmp.obj.y);
							pts = tp.estimateLaunchPoint(sling, tw);
								
							tmp.targetNode.add(new TargetNode(tw,pts,"up"));
						}	
					}
				}
			}		
		}			
	}
	
	/*
	 * select Node that will be hit 
	 * Random Selection or Selection according to Expected Reward prediction
	 */
	public Node NodeSelection(ABType bird){
		double randomHit = 0.1;				// random Node Selection, 1 of 10 shots
		System.out.println(" ");
		System.out.println("------------------------------------------------------------------");
		System.out.println("-----------------------NODE SELECTION-----------------------------");
		System.out.println(" ");
		
		Node target = null;
		
		if(ran.nextDouble() < randomHit){				// random hit in training
			System.out.println("RANDOM HIT NODE ");
			ArrayList<Node> candidates = new ArrayList<Node>();

			// find candidates for random shot
			for (int i=0; i< LevelSize()-1; i++){
				for (int j=0; j< LevelSize(i); j++){
					Node tmp = GetElement(i, j);
					if((tmp.sceneFeasible) && (tmp.feasible) && (tmp.reachable)) {
							candidates.add(tmp);
					}
				}
			}
			
			if(candidates.size() == 1) {
				target = candidates.get(0);
			}
			else if (candidates.size() > 1){
				System.out.println("\n" + "Random selection... " + candidates.size() +" candidates for target..." + "\n");
				int randomNode = ran.nextInt(candidates.size()-1);
				target = candidates.get(randomNode);
			}
			candidates.clear();
		}
		else{
			System.out.println("Hit Maximum Expected Reward Node... ");
			target = findMaxHitValueNode(bird);
		}
	
		if(target != null){
			System.out.println("----------------------TARGET NODE----------------------");
			System.out.println("PROPERTIES: Node from level "+ target.level + " ABType: "+ target.obj.type + " type: "+ target.type);
			System.out.println("TARGET x: " + target.obj.getCenterX() + " y: " + target.obj.getCenterY());
			System.out.println("Width: " + target.obj.width + " height: " + target.obj.height + " Target points: " + target.targetNode.size() + " Node's Angle: " + target.obj.angle);
			System.out.println("FEASIBILITY: Scene: "+ target.sceneFeasible + " feasible: " + target.feasible + " reachable: " 
													 + target.reachable + " WhiteFeasible: " + target.WhiteFeasible );
			System.out.println("RegressorType: " + target.NodeType + " Expected Reward: " + target.hitvalue);
			System.out.println("Distance from Nearest Pig/TNT or Most external Point : " + target.NPdistance);
		}
		return(target);
	}
	
	/*
	 * function to return a random scenefeasible Node, in case that target Node is null
	 */
	public Node NullTargetNode(Rectangle sling){
		ArrayList<Node> cands = new ArrayList<Node>();
		Node target = null;
		
		System.out.println("Hit a random Node due to null target....");
		for(int i=0; i< LevelSize()-1; i++){						// We must not consider "Root" as a target
			for(int j=0; j< LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				
				if(tmp.sceneFeasible) {
					cands.add(tmp);
				}
			}
		}
		
		Random rand = new Random();
		if(cands.isEmpty()){
			int random1 = 0;
			if(LevelSize()-2 == 0)
				random1 = 0;
			else if(LevelSize()-2 > 0)
				random1 = rand.nextInt(LevelSize()-2);					// We must not consider "Root" as a target
			
			int random2 = rand.nextInt(LevelSize(random1));
			target = GetElement(random1, random2); 
			System.out.println("Target: Node " + random2 + " level: " + random1);
		}
		else{
			target = cands.get(rand.nextInt(cands.size()));
			System.out.println("NODE: type: " + target.type + " level: " + target.level);
		}
		
		ArrayList<Point> pts = tp.estimateLaunchPoint(sling, target.obj.getCenter());
		TargetNode tpNode = new TargetNode(target.obj.getCenter(), pts, "RANDOM-center");
		target.targetNode.add(tpNode);	
		
		cands.clear(); 
		cands = null;
		
		return target;
	}
	
	/*
	 *  function to find max hitvalue Node
	 */
	private Node findMaxHitValueNode(ABType bird){
		double max = Double.NEGATIVE_INFINITY;
		Node result = null;
		
		int n = LevelSize();
		
		for (int i=0; i<n-1; i++){
			for (int j=0; j< LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				
				if(tmp.sceneFeasible){
					if(!(bird.equals(ABType.WhiteBird))){
						if ((tmp.feasible) && (tmp.reachable)){																								
							if (tmp.hitvalue > max){
								max = tmp.hitvalue;
								result = tmp;
							}
						}
					}
					else{
						if(tmp.WhiteFeasible){
							if (tmp.hitvalue > max){
								max = tmp.hitvalue;
								result = tmp;
							}
						}
					}
				}
			}
		}

		// if there are many Nodes with max hitvalue
		ArrayList<Node> candidates = new ArrayList<Node>();
		
		for (int i=0; i<n-1; i++){					// "Root" is not considered		
			for (int j=0; j<LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				if(!(bird.equals(ABType.WhiteBird))) {
					if(tmp.sceneFeasible && tmp.feasible && tmp.reachable && tmp.hitvalue == max ) {
						candidates.add(tmp);
					}
				}
				else {
					if(tmp.WhiteFeasible && tmp.hitvalue > max){
							max = tmp.hitvalue;
							result = tmp;
					}
				}
			}
		}
		
		if(candidates.size() == 1) {
			result = candidates.get(0);
		}
		else if (candidates.size() > 1){
			System.out.println("\n" + "RANDOM SELECTION... " + candidates.size() +" candidates for target..." + "\n");
			int r = ran.nextInt(candidates.size()-1);
			result = candidates.get(r);
		}
		candidates.clear();
		
		return(result);
	}
	
	public Node GetElement(int i, int j) {
		return level_list.get(i).get(j);
	}
	public int LevelSize() {
		return level_list.size();
	}
	public int LevelSize(int i) {
		return level_list.get(i).size();
	}
	
	public void SetPhi(Matrix Phi, int i, int j) {
		 GetElement(i,j).PhiX = Phi;
	}
	
	public double GetMyWeight(int i, int j) {
		return  GetElement(i,j).myWeight;
	}
	
	/*
	 * for each Node set RelevantHeight feature
	 */
	private void SetRelevantHeight(){
		int n = LevelSize();
		
		// level_list consists of only two levels
		// top level contains Node "Root"	
		// there are only two levels
		if(n-3 < 0){
			System.out.println("Tree Structure consists of 2 levels .... 1st: 'Root' 2nd: other Nodes ... ");
		}
		else{	// there are more than two levels
			/*
			 * top-down propagation of heights
			 */	
			for (int i=n-2; i>=0; i--){	
				int m = LevelSize(i);
				for(int j=0; j<m; j++){
					Node thisNode = GetElement(i, j);
					double min = 800;
				
					for(int par=0; par<thisNode.parent.size(); par++){
						Node parentNode = thisNode.parent.get(par); 
						
						if(thisNode.parent.get(0).type.equals("Root")){
							min = thisNode.obj.getCenterY() + thisNode.obj.height/2.00;
						}
						else if(parentNode.topdown < min){
							min = parentNode.topdown;
						}
					}
					thisNode.topdown = min;
				}
			}
			
			// to distinguish Rarent-"Root" nodes
			for (int i=n-2; i>=0; i--){		
				for(int j=0; j<LevelSize(i); j++){
					Node tmp = GetElement(i,j);
					
					if(tmp.parent.get(0).type.equals("Root")){
						tmp.topdown = 0;
					}
				}
			}
		}
		
		/*
		 * bottom-up propagation of heights
		 */
		for(int i=0; i<n-1; i++){		// Root out
			int m = LevelSize(i);
			
			for(int j=0; j<m; j++){
				Node thisNode = GetElement(i,j);
				double max = -800;
				
				if(thisNode.children.isEmpty())
					max = thisNode.obj.getCenterY() + thisNode.obj.height/2.00;
				else{
					for(int child=0; child<thisNode.children.size(); child++){
						if(thisNode.children.get(child).bottomup > max){
							max = thisNode.children.get(child).bottomup;
						}
					}
				}
				thisNode.bottomup = max;
			}
		}
	
		// to distinguish "no-Children" nodes
		for (int i=0; i<LevelSize()-1; i++){	
			for(int j=0; j<LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				
				if(tmp.children.isEmpty()){
					tmp.bottomup = tmp.obj.getCenterY()+tmp.obj.height/2.00;
				}
			}
		}
		
		// final computation of Relevant Height
		for (int i=0; i<LevelSize()-1; i++){		// "Root out of computations
			for(int j=0; j<LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				
				if(tmp.topdown == 0){
					tmp.relevantHeight = 0;
				}
				else{
					tmp.relevantHeight = (double)Math.abs(((tmp.obj.getCenterY()+tmp.obj.height/2.00) - tmp.topdown))/200;
					if(tmp.relevantHeight > 1.0)
						tmp.relevantHeight = 1.0;
				}
			}
		}
	}
	
	public double GetRelevantHeight(int i, int j) {
		return  GetElement(i,j).relevantHeight;
	}
	
	/*
	 * function which computes the total weight of each node
	 * totalWeight refers to the Nodes above current Node
	 * Cumulative Parents' Weight
	 */
	public void SetAboveWeight(Rectangle slingshot){
		if (slingshot != null){
			int n = LevelSize();  
			for (int i=n-2; i>=0; i--){
				int m = LevelSize(i);

				for(int j=0; j<m; j++){
					Node thisLevelNode = GetElement(i,j);
					
					thisLevelNode.totalWeight = 0;
						
					for (int a=0; a<thisLevelNode.parent.size(); a++) {
						thisLevelNode.totalWeight += (int) (thisLevelNode.parent.get(a).totalWeight + thisLevelNode.parent.get(a).myWeight); 
					}
				}
			}
		}
	}
	
	public double GetAboveWeight(int i, int j) {
		return  GetElement(i,j).totalWeight;
	}
	
	/*
	 * function to compute the distance from nearest Pig or TNT
	 */
	private void setNPdistance(List<ABObject> pigs, List<ABObject> TNT, Rectangle sling, ABType bird, double limit, Rectangle room){
		List<ABObject> newList = new ArrayList<ABObject>();
		newList.addAll(pigs);
		newList.addAll(TNT);
		
		for (int i=LevelSize()-2; i>=0; i--){		
			for(int j=0; j<LevelSize(i); j++){
				Node tmp = GetElement(i,j);
				if(tmp.feasible && tmp.sceneFeasible && tmp.reachable) { 
					
					boolean tmptype = ((!tmp.type.equals("Pig")) && (!tmp.type.equals("TNT")));	 		// Not Pig AND not Tnt
					
					if(tmptype){
						double min = Double.POSITIVE_INFINITY;
						double dist = -1;
						Point tmpCenter = tmp.obj.getCenter();
							
						// find the nearest pig or TNT
						for(int a=0; a<newList.size(); a++){
							dist = distance(newList.get(a).getCenter(), tmpCenter);
							if(!tmp.obj.equals(newList.get(a))){
								if(dist < min){
									min = dist;
								}					
							}
						}
						tmp.NPdistance = min;
					}
					else{
						// Euclidean distance between nearest pig
						// System.out.println(tmp.type + " Center_X: " + tmp.obj.getCenterX() + " Center_Y: " + tmp.obj.getCenterY());
						double min_ = 1000;
						double [][] NP = {{0,0,0,0},{0,0,0,0}}; 
						int loc = 0;
						
						for(int a=0; a<tmp.targetNode.get(0).getReleasePointList().size(); a++){
							NP[a] = findTapTimeforPigTNT(tmp, sling, tmp.targetNode.get(0).getReleasePointList().get(a), room, tmp.targetNode.get(0).getTargetPoint(), bird, limit);
							if(NP[a][1] < min_){
								min_ = NP[a][1];
								loc = a;
							}
						}
						tmp.TapInterval = NP[loc][0];
						tmp.NPdistance = min_;
						
						// remove needless trajectory
						if(tmp.targetNode.get(0).getReleasePointList().size() == 2){
							//System.out.println(tmp.type);
							if(loc == 0){
								tmp.targetNode.get(0).getReleasePointList().remove(1);
							}
							else{
								tmp.targetNode.get(0).getReleasePointList().remove(0);
							}
						}
					}
					
					if(tmp.NPdistance > 100) {
						tmp.NPdistance = 100;
					}
					
					// normalization: 100 is considered the maximum "affecting" distance
					tmp.NPdistance = tmp.NPdistance/100.0;
				}
			}	
		}
	}
	public double GetNPDistance(int i, int j) {
		return  GetElement(i,j).NPdistance;
	}
	
	/*
	 * compute Expected Reward for Candidate Targets
	 * depending on pair {bird, material}
	 * Right type of Regressor and add UCB factor
	 */
	public void ComputeHitvalue(ArrayList<Regressor> w){
		int n = level_list.size();
		double term = 0;
		
		double ucbfactor = 3000;
		int N = 0;
		for(int i =0; i<w.size(); i++) {
			N += w.get(i).GetN();
		}
		term = ((2*Math.log10(N)));
		
		for (int i=n-2; i>=0; i--){		
			for(int j=0; j<level_list.get(i).size(); j++){
				Node tmp = level_list.get(i).get(j);
				
				if(tmp.type.equals("Pig") && (tmp.feasible) ) {
					//System.out.println(tmp.NodeType);
				//	tmp.WFproduct(w.get(tmp.NodeType).GetW());
				}
				if(tmp.sceneFeasible && (tmp.feasible) && (tmp.reachable)){
					tmp.WFproduct(w.get(tmp.NodeType).GetW());
					
					double Ni = ((double)(w.get(tmp.NodeType).GetN()));
					if(Ni > 0.0) {
						tmp.hitvalue = tmp.hitvalue + ucbfactor*(Math.sqrt(term/(Ni)));
					} 
					else if(Ni == 0.0) {
						tmp.hitvalue = tmp.hitvalue + Double.POSITIVE_INFINITY;
					}
				}
				else {
					tmp.hitvalue = Double.NEGATIVE_INFINITY;
				}
			}
		}
	}
	
	// function to return Euclidean distance between two Points
	private double distance(Point p1, Point p2) {
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
	}
	
	/*
	 * function to estimate tap time 
	 * if target is a "sheltered" Pig 
	 * if the bird is Blue or Yellow or Black 
	 * 
	 * - returns: double [] matrix
	 * [0]: tap time percentage
	 * [1]: most external Point distance
	 * [2]: external Point's coordinate x
	 * [3]: external Point's coordinate y
	 */
	private double [] findTapTimeforPigTNT(Node obj, Rectangle sling, Point LaunchPoint, Rectangle room, Point targetPoint, ABType bird, double limit){
		double [] result = {0,0,0,0};
		
		List<Point> trajPoint = tp.predictTrajectory(sling,LaunchPoint);
		Point compPoint = findRealTargetforPigTNT(sling, obj, LaunchPoint, room, targetPoint, limit, bird);

		int total_points = 0;
		int partial_points = 0;
		if(compPoint != null){
			boolean compPointFind = false;
			//System.out.println("CompNode: " + comp.type + " level: " + comp.level);
			result[1] = distance(compPoint, targetPoint);
			
			for(int i=0; i<trajPoint.size(); ++i) {
				if((obj.obj.x <= trajPoint.get(i).x)) {
					total_points = i;
				}
				if((compPoint.x <= trajPoint.get(i).x) && !compPointFind) {
					partial_points = i;
					compPointFind = true;
				}
			}
		}
		
		double percentage = 0;
		switch (bird) {
			case RedBird:
				percentage = 90;											
				break;               
			case YellowBird:
				percentage = 85;
				break;
			case BlueBird:
				percentage = 75; 
				break;
			case BlackBird:
				percentage = 160;
				break;
			default:
				percentage = 80;
				break;
		}

		if(!bird.equals(ABType.BlackBird)){
			if(total_points != 0){
				percentage -= (double)((total_points - partial_points) / total_points)*100;
			}
		}
		
		if(partial_points !=0){
			result[0] = percentage;
			result[2] = compPoint.x;
			result[3] = compPoint.y;
		}
		else{
			result[0] = percentage;
			result[1] = 0;
		}
	
		return (result);
	}
	
	/*
	 * find the most external Point for a "shletered" Pig or TNT
	 */
	private Point findRealTargetforPigTNT(Rectangle sling, Node target, Point LaunchPoint, Rectangle room, Point targetPoint, double limit, ABType bird){
		int pos = 0;
		
		List<Point> trajPoint = tp.predictTrajectory(sling,LaunchPoint);
		Point resP = null;
		
		// find the first Point that is in ourRoom
		for (int i=0; i<trajPoint.size(); i++) {
			if (room.contains(trajPoint.get(i))){
				pos = i;
				break;
			}
		}
				
		for(int a=pos; a<trajPoint.size(); a++){
			
			// most right from interest Point
			if((targetPoint.y <= trajPoint.get(a).y) && (targetPoint.x <= trajPoint.get(a).x))
				break;
			
			//System.out.println(trajPoint.get(a));
			for(int i=0; i<level_list.size()-1; i++){
				for(int j=0; j<level_list.get(i).size(); j++){
					Node tmp = level_list.get(i).get(j);
					
					Rectangle tmpbird;
					tmpbird = new Rectangle(trajPoint.get(a).x, trajPoint.get(a).y, 5, 5);			// average bird's width and height
					if (tmp.obj.intersects(tmpbird)){
						if(!tmp.equals(target)){
							if(!(tmp.type.equals("Pig") || tmp.type.equals("TNT"))){
								//System.out.println("Point: " + trajPoint.get(a));
								return(trajPoint.get(a));
							}
						}
						else{
							return(targetPoint);
						}
					}
					tmpbird = null;
				}
			}
		}
		
		return(resP);
	}
	
	/*
	 * function to free the constructed Tree
	 */
	public void myfree(){
		for(int i=0; i< LevelSize(); i++){
			for(int j=0; j< LevelSize(i); j++) {
				Node.freeNode(GetElement(i, j));
			}
		}
		
		for(int i=0; i< LevelSize(); i++){
			level_list.get(i).clear();
		}
		level_list.clear();
		System.gc();
		
	}
	
	/*
	 * to pause whenever needed
	 */
	private void mySleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
