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

import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import Jama.*;

public class Node{
	
	public ABObject obj;				// object type
	public String type;					// merge objects
	public int level;			
	public ArrayList<Node> children;	// list for child/children of each Node
	public ArrayList<Node> parent;		// list for parent/s of each Node
	
	// for features
	public double myWeight = 0;				// Node's Area
	public double totalWeight = 0;				// Parents Cumulative Weight
	public double slingDistance = -1.00;		// Node's distance of slingshot
	public double NPdistance = 0;				// field to show us the distance from the nearest Pig or TNT
	public double relevantHeight = -1.00;
	public double topdown = 0;
	public double bottomup = 0;
	
	// Node's Feasibility
	public boolean reachable = true;
	public boolean sceneFeasible = true;		// field to show us if an object is in the initial scene
	public boolean feasible = true; 
	public boolean WhiteFeasible = false;
	
	public int NodeType = -1;										// field to show the type of regressor that is used for predict the expected value
	public Matrix PhiX;
	public double hitvalue = Double.NEGATIVE_INFINITY;			// expected reward		

	public ArrayList<TargetNode> targetNode;						// Points where a Node can be hit
	public double TapInterval = 0;									// field, that used for tbm is Pig or TNT, to compute right tap						
	
	
	public Node(ABObject object, int lev){
		this.level = lev;
		this.obj = object;
		this.obj.angle = Math.toDegrees(this.obj.angle);
		this.children = new ArrayList<Node>();
		this.parent = new ArrayList<Node>();
		String type_ = object.type.toString();
		this.type = type_;
		this.hitvalue = 0;
		this.setmyWeight();
		this.targetNode = new ArrayList<TargetNode>();
	}

	public Node(String newType, ABObject object, int lev){
		this.level = lev;
		this.obj = object;
		this.children = new ArrayList<Node>();
		this.parent = new ArrayList<Node>();
		this.type = newType;
		this.hitvalue = 0;
		this.setmyWeight();
		this.targetNode = new ArrayList<TargetNode>();
	}
	
	// function to compute Phi*w for each Node
	public void WFproduct(Matrix w){
		// Check Matrices dimensions 
		//System.out.println("Phi size(): Row: " + this.PhiX.getRowDimension() + " " + this.PhiX.getColumnDimension());
		//System.out.println("w size(): Row: " + w.getRowDimension() + " " + w.getColumnDimension());
		Matrix hitvalueM = this.PhiX.times(w);
		this.hitvalue = hitvalueM.get(0,0);
	}
	
	/*
	 * function to choose targetPoint
	 */
	public TargetNode targetPointSelection(){	
		
		return(targetNode.get(0));
	}
	
	/*
	 * function to choose releasePoint given the targetPoint
	 */
	public Point releasePointSelection(TargetNode tpNode, Rectangle sling, TrajectoryPlanner tp){
		Point releasePoint = null;
		if(tpNode.getLabel().equals("up")){		// up side: priority in high-arching shot
			if(tpNode.getReleasePointList().size() == 2){
				releasePoint = tpNode.getReleasePointList().get(1);
				System.out.println("Get Trajectory 1...");
				System.out.println("Choose trajectory 1, up side Node...." + " Angle: " + Math.toDegrees(tp.getReleaseAngle(sling, releasePoint)));
				return releasePoint;
				
			}
			else{
				System.out.println("Up side of Node.....");
				releasePoint = tpNode.getReleasePointList().get(0);
				System.out.println("Angle: " + Math.toDegrees(tp.getReleaseAngle(sling, releasePoint)));
				return releasePoint;
			}
		}
		else if(tpNode.getLabel().equals("left-3/4") || tpNode.getLabel().equals("left-1/2")){		// left side 
			System.out.println("Left side of Node.... " + tpNode.getLabel());
			releasePoint = tpNode.getReleasePointList().get(0);
			System.out.println("Angle: " + Math.toDegrees(tp.getReleaseAngle(sling, releasePoint)));
			return releasePoint;
		}
		else{	// center, objects with strange angle
			System.out.println("Center side ....");
			if(this.obj.angle > 135){
				if(tpNode.getReleasePointList().size() == 2){
					releasePoint = tpNode.getReleasePointList().get(1);
				}
				else{
					releasePoint = tpNode.getReleasePointList().get(0);
				}
			}
			else if(this.obj.angle < 80){
				releasePoint = tpNode.getReleasePointList().get(0);
			}
			else{
				releasePoint = tpNode.getReleasePointList().get(0);
			}
			System.out.println("Angle: " + Math.toDegrees(tp.getReleaseAngle(sling, releasePoint)));
			return releasePoint;
		}
	}

	private void setmyWeight(){
		int res, weight = 0;
		
		switch(this.obj.type.ordinal()){
			case 9 : 
				res = 1;	// Wood: 9 
				break;
			case 8 : 
				res = 1;	// Ice: 8 
				break;
			case 10 : 
				res = 1;	// Stone: 10
				break;
			case 11 : 
				res = 1;	// Tnt: 11 	TARGET
				break;	 
			case 7 : 
				res = 1;	// Pig: 7	TARGET
				this.obj.area = this.obj.width*this.obj.height;		// findPigsMBR() used
				break; 
			case 12: 
				res = 0;	// Root: 12
				break;
			case 14: 
				res = -1;
				System.out.println(this.type);
				System.out.println("unknown type of node...");
				break;
			default : 
				res = 10;
				System.out.println("Default case in setmyWeight()...");
				break;
		}
		
		// find area in MBR representation 
		if(this.obj.area == 0){
			this.obj.area = this.obj.width*this.obj.height;
		}
		weight = this.obj.area*res;	
		this.myWeight = (double) weight;
	}

	public double SlingDistance(Rectangle slingshot){
		Point NodeC = this.obj.getCenter();
		Point slingC;
		double dist = -1;
		
		if (slingshot != null){
			slingC = new Point((int)slingshot.getCenterX(), (int)slingshot.getCenterY());
		}
		else{
			System.out.println("there is no slingshot... distance = -1");
			return(dist);
		}
		
		dist = distance(NodeC, slingC);
		return(dist);
	}
	
	private double distance(Point p1, Point p2) {
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
	}
	
	public boolean areBrothers(Node node, Node father){
		boolean brother = false;
		int bro = 0;
		
		if (father.children.contains(this))
			bro++;
		
		if (father.children.contains(node))
			bro++;
		
		if (bro == 2)
			brother = true;
		
		return(brother);
	}
	
	static public void freeNode(Node node){
		
		for(int i=0; i<node.targetNode.size(); i++)
			TargetNode.freeTargetNode(node.targetNode.get(i));
		
		node.targetNode.clear();
		node.children.clear();
		node.parent.clear();
		node = null;
	}

}