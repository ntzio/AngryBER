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
import java.util.ArrayList;

public class TargetNode{

	private Point targetPoint;
	private ArrayList<Point> releasePoint;
	private String label;
	
	public TargetNode(Point tpt, ArrayList<Point> pts, String side){
		targetPoint = new Point(tpt);
		releasePoint = new ArrayList<Point>();
		
		for(int i=0; i<pts.size(); i++){
			releasePoint.add(pts.get(i));	
		}
		
		label = new String(side);
	}
	
	public TargetNode(){
		targetPoint = new Point();
		releasePoint = new ArrayList<Point>();
		label = new String();
	}
	
	public static void freeTargetNode(TargetNode tn){
		tn.targetPoint = null;
		tn.releasePoint.clear();
		tn.label = null;
	}
	
	public Point getTargetPoint(){
		return targetPoint;
	}
	
	public ArrayList<Point> getReleasePointList(){
		return releasePoint;
	}
	
	public String getLabel(){
		return label;
	}
}