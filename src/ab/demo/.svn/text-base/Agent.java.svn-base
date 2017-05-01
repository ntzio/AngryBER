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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.ABUtil;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.real.shape.Poly;

public class Agent implements Runnable {
	private ActionRobot aRobot;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	private TrajectoryPlanner tp;
	private boolean firstShot;
	private int firstShotc;			
	private int shots = 0;									// variable to show number of shots in each level
	private boolean slingflag = false;					    // flag for mis-detection of sling
	private int scaleCT = 0;								// help variable for scale change control 
	private double [] half_heights = {24,14,14,14,50};		// used in setLimit(): bird's constant heights	
	private int t;											// help variable for score	
	private Rectangle slingshot = new Rectangle();			// help variable for slingshot detection
	private Settings settings;								// to set the method's parameters	
	private Features features;								// to manipulate available features 
	private ArrayList<Regressor> regressors;				// available regressors to predict expected reward for each shot
	private Rectangle room = null;							// Rectangle to allocate game scene
	
	// a standalone implementation of the Naive Agent
	public Agent() {	
		
		settings = new Settings();
		features = new Features(settings);
		regressors = new ArrayList<Regressor>();
		
		// ArrayList of regressors creation, according to settings
		for (int i=0; i<settings.get_num_of_regressors(); i++){
			regressors.add(new Regressor(settings.get_num_of_clusters(), settings.get_alpha(), settings.get_beta(), settings));
		}
		
		if(!settings.get_load_from_file()){			
			System.out.println("w0 and S0 are initialized in [0] and [(a-1)xI]... ");	
		}
		else {	
			System.out.println("W and S Matrices are loaded from existing files....");
			settings.LoadWSNFiles(regressors);
		}
	
		aRobot    = new ActionRobot();
		tp        = new TrajectoryPlanner();	
		
		t = 0;									// score variable
		firstShot = true;						// true if it is the first shot for each level
		firstShotc = 0;							// to count total number of shots 
		
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();
	}
	
	// run the standalone agent
	public void run() {
		
		// set to play levels of world 3 of Poached Eggs
		int episode = settings.get_num_of_episode();
		int [] world_3_levels = {10,11,12,13,14,15,16,17,18,19,20,21};
		int levelct = 0;
		
		if(episode == 3){
			System.out.println("WORLD 3 - Chosen levels for training....");
			aRobot.loadLevel(world_3_levels[0]);
		} 
		else
			aRobot.loadLevel(currentLevel);
				
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int score = StateUtil.getScore(ActionRobot.proxy);
				
				if(episode != 3){
					if(!scores.containsKey(currentLevel)) {
						scores.put(currentLevel, score);
					}
					else
					{
						if(scores.get(currentLevel) < score)
							scores.put(currentLevel, score);
					}
					int totalScore = 0;
					for(Integer key: scores.keySet()){
						totalScore += scores.get(key);
						System.out.println(" Level " + key + " Score: " + scores.get(key) + " ");
					}
					System.out.println("Total Score: " + totalScore);
				}
				
				// write score of each level in a file according to format: (level,score,shots)
				if(episode == 3){
					settings.WriteScoresInFile(world_3_levels[levelct],score, shots);
					levelct++;
				}
				else
					settings.WriteScoresInFile(currentLevel,score,shots);
				
				System.out.println("Scores.txt has been modified....");

				// if episode 3 is selected 
				if(episode == 3){
					if(levelct == 11)
						levelct = 0;
						
					aRobot.loadLevel(world_3_levels[levelct]);	
				}
				// if episode 3 is not selected
				else{ 
					if(currentLevel == 21)
						currentLevel = 0;
					
					aRobot.loadLevel(++currentLevel);
				}
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				firstShot = true;
				slingflag = false;
				t = 0;
			} 
			else if (state == GameState.LOST){
				System.out.println("Restart");
				
				if(episode == 3){
					settings.WriteScoresInFile(world_3_levels[levelct],0,shots);
				}
				else{
					settings.WriteScoresInFile(currentLevel,0,shots);
				}
				
				firstShot = true;
				shots = 0;
				t = 0;
				System.out.println("after restart t: " + t);
				aRobot.restartLevel();
			} 
			else if (state == GameState.LEVEL_SELECTION){
				System.out.println("Unexpected level selection page, go to the last current level : " + currentLevel);
				aRobot.loadLevel(currentLevel);
			} 
			else if (state == GameState.MAIN_MENU){
				System.out.println("Unexpected main menu page, go to the last current level : " + currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} 
			else if (state == GameState.EPISODE_MENU){
				System.out.println("Unexpected episode menu page, go to the last current level : " + currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}
		}

	}
	
	/*
	 * function to decide the target and execute the shot
	 * solve the run()-selected level
	 */
	public GameState solve(){
		int returnScore = 0;						// help variable for score
		boolean shotFlag = false;					// boolean variable to show if shot is made
		
		ActionRobot.fullyZoomOut();
		//ABUtil.mySleep(2000);
		
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();
		
		// In case where we cannot recognize the slingshot
		// capture the right sling, for first time
		if(!slingflag){					 
			slingshot = sling;
			slingflag = true;
		}
		
		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out.println("No slingshot detected. Please remove pop up or zoom out...");
			// it has been updated
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
			
			// for correct detection of slingshot
			if(sling == null)
				sling = slingshot;
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		/*Scene Detection*/
		List<ABObject> objects 		= new ArrayList<ABObject>();
		List<ABObject> pigs 		= new ArrayList<ABObject>();
		List<ABObject> PigsObjects  = new ArrayList<ABObject>();
		List<ABObject> tnts 		= new ArrayList<ABObject>();
		List<Poly> hills 			= new ArrayList<Poly>();
		ABObject mostDistantObj     = new ABObject();
		
		mostDistantObj  = ABUtil.SceneDetection(vision, objects, pigs,PigsObjects, tnts, hills);
		int initialpigs = pigs.size();
		/*end of Scene Detection*/
		///////////////////////////////////////////////////////////////////////////////////////////
		
		GameState state = aRobot.getState();
		
		if (sling != null){
			if (!pigs.isEmpty() && !PigsObjects.isEmpty()) {
				firstShotc++;
				System.out.println(" Shot No. "+ firstShotc);
				
				// allocate Game Scene
				if(firstShot){
					room = ABUtil.findOurRoom(pigs, objects);
					room = new Rectangle(room.x-8, room.y-8, room.width+16, room.height+16);
					scaleCT = 0;
				}
			
				// bird type on sling detection 
				ABType bird =  aRobot.getBirdTypeOnSling(sling);
				int birdtype = bird.ordinal();
				
				boolean whitebird = bird.equals(ABType.WhiteBird);
				double limit = setLimit(birdtype);
				
				//Tree Construction
				Tree tree = new Tree(tp);
				tree.TreeConstruction(room, PigsObjects, room.x, room.y, room.width, pigs, tnts, sling, bird, limit); 
				
				//Check feasibility of each node
				Feasibility feasibility = new Feasibility(room, tp);
				feasibility.CheckFeasibility(tree, bird, sling, hills, mostDistantObj, limit);
				
				// set Node's features
				tree.SetFeatures(room, pigs, tnts, sling, bird, limit);

				// set Phi Matrix for each feasible Node of our Tree 
				features.setPhiX(tree);
			
				//Compute hit value
				tree.ComputeHitvalue(regressors);
				
				// print the constructed Tree
			    tree.Print(settings.get_type_of_features());
				
				if (firstShot){
					t = 0;
					shots = 0;
				}
				
				// select the Tree's Node that will be hit in that shot
				Node target = tree.NodeSelection(bird);
				
				// to check case that a "null" target Node is returned
				// to avoid crash
				boolean nullflag = false;
				if(target == null){
					target = tree.NullTargetNode(sling);
					nullflag = true;
				}
			
				// preparation for shoot according to target Node
				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;
				{
					TargetNode tpNode;
					boolean typeOFbirdANDmaterial = ((bird.equals(ABType.BlueBird) || bird.equals(ABType.YellowBird) 
							|| bird.equals(ABType.RedBird) || bird.equals(ABType.Unknown) || bird.equals(ABType.BlackBird)) 
							&& (target.type.equals("Pig") || target.type.equals("TNT")));
					
					// give the target according to previous conditions
					if(!whitebird){			// not white bird on sling
						tpNode = target.targetPointSelection();
					}
					else{					// White Bird case
						System.out.println("White bird....");
						double point = 0;
						
						if(nullflag){
							System.out.println("Get point 0....");
							point = 0;
						}
						else{
							point = (int)Math.floor(0.80*(target.targetNode.size()-1));	// 0: the highest point of trajectory	
						}
						System.out.println("Point: " + point + " points: "+ (target.targetNode.size()-1));
						System.out.println("index: " + point + " -> " + "[" + target.targetNode.get((int)point).getTargetPoint().x 
																		+ "," + target.targetNode.get((int)point).getTargetPoint().getY() + "]");
						tpNode = target.targetNode.get((int)point); 
					}
					
					System.out.println("Hit side: " + tpNode.getLabel());
					System.out.println("Parent: " + target.parent.get(0).type);
					System.out.println("ReleasePoints : " + tpNode.getReleasePointList().size());
					System.out.println("bird on slingshot: " + bird);
					System.out.println("-------------------------------------------------------\n");
					
					// choose releasePoint given the targetPoint and side of the Node that will be hit
					if(!typeOFbirdANDmaterial){		
						if(!whitebird){
							System.out.println("-------------------------------------------------------");
							releasePoint = target.releasePointSelection(tpNode, sling, tp);
							System.out.println("-------------------------------------------------------");
						}
						else{
							releasePoint = tpNode.getReleasePointList().get(0);
						}
					}
					
					Point _tpt = new Point(tpNode.getTargetPoint());

					int tapInterval = 0;
					boolean tapFlag = false;
					
					if(typeOFbirdANDmaterial){
						releasePoint = tpNode.getReleasePointList().get(0);
						tapInterval = (int) target.TapInterval;
						tapFlag = true;	
					}
					
					// Get the reference point for sling
					Point refPoint = tp.getReferencePoint(sling);
	
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,releasePoint);
						System.out.println("Release Angle: "+ Math.toDegrees(releaseAngle) + "\n");
						
						// function to select tap time according to bird and trajectory
						// if tapFlag, then tapInterval is set
						if(!tapFlag)
							tapInterval = ABUtil.tapTimeSelection(bird, tpNode, releaseAngle);
						
						int tapTime = 0, gap = 0;
						
						// special treatment for taptime when a WhiteBird is on sling
						// positive and negative gradient
						if(whitebird){		
							Point tmp =  tpNode.getTargetPoint();
							List<Point> trajP = new ArrayList<Point>();
							trajP = tp.predictTrajectory(sling,releasePoint);
							
							gap = ABUtil.WhitefindGap(trajP, tmp);
							tmp.x = tmp.x - gap;
					//		tapTime =  tp.getTimeByDistance(sling, releasePoint, tmp);
							tapTime =  tp.getTapTime(sling, releasePoint, tmp, 100);
						} else {
							tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						}
						
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						System.out.println("Target Node is shot by the agent ...!!!!!");
						shotFlag = true;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else{
						System.err.println("No Release Point Found");
						tree.myfree();
						
						hills.clear();
						vision = null;
						return state;
					}
				}
				// Garbage Collector
				tree.myfree();
			
				boolean scoreflag = false; 
				// check whether the slingshot is changed, the change of the slingshot indicates a change in the scale
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					
					// for correct slingshot detection
					if(_sling == null)
						_sling = slingshot;
					
					// return right score for update parameters
					if(_sling != null){
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if((scale_diff < 25) || (scaleCT > 3)){
							scaleCT = 0;
							if(dx < 0){
								aRobot.cshoot(shot);		
								shots++;

								int tmp_score = 0;
								boolean fscore = false;
								
								state = aRobot.getState();
								if(state == GameState.PLAYING){
									ABUtil.mySleep(1000);
								}
								else if(state == GameState.LOST){
									fscore = true;
									tmp_score = StateUtil.getScore(ActionRobot.proxy);
								}
								
								if(!fscore)
									tmp_score = StateUtil.getScore(ActionRobot.proxy); 		
								
								scoreflag = false; 
								if(tmp_score >= 0){
									scoreflag = true;
									returnScore = getRightScore(target, initialpigs, tmp_score);			// function to get the right score
								}
								
								state = aRobot.getState();
								if (state == GameState.PLAYING){
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else{
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
							scaleCT++;
						}
					}
					else{
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
					}
				}
				
				/*
				 * Update parameters
				 * if 1. bird is not "Unknown"
				 * 	  2. PhiX != null
				 */
				if(!bird.equals(ABType.Unknown)){				
					if(target.PhiX != null){					
						if (scoreflag){
							System.out.println("REAL REWARD for update:  " + returnScore);
							System.out.println("PREDICTION FOR EXPECTED REWARD: " + target.hitvalue);
							System.out.println("DIFFERENCE: " + (returnScore-target.hitvalue));
							System.out.println("---------------------------------------------------------------------------------");
							
							regressors.get(target.NodeType).BayesianRegression(target, returnScore);	
							
							System.out.println("/////////////////////////////////////////////////////");
							System.out.println("Write modified parameters in Files....");
							
							settings.writeWSNFile(regressors, target.NodeType);
							
							System.out.println("Parameters updated......");
							System.out.println("/////////////////////////////////////////////////////");
						}
					}
					else
						System.out.println("Update did not performed due to null Phi vector...");
				}
				else
					System.out.println("Update did not performed due to 'Unknown' bird type...");
				
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			if (shotFlag){
				System.out.println("Shot No. " + firstShotc + " completed...");
				System.out.println(" ");
			}
			
			System.out.println("---------------Game State---------------");
			System.out.println("\t\t\t\t" + state);
			System.out.println("----------------------------------------");
			System.out.println(" ");
			
		}
		else
			System.out.println("ERROR: There is no slingshot ....!!!!");
		
		hills.clear();
		vision = null;
		
		return state;
	}
	
	/*
	 * function to get the limit according to bird type
	 * used to check feasibility
	 */
	private double setLimit(int bird){
		switch(bird){
		case 2: 
			return(half_heights[0]);
		case 3:
			return(half_heights[1]);
		case 4: 
			return(half_heights[2]);
		case 5: 
			return(half_heights[3]);
		case 6: 
			return(half_heights[4]);
		case 14:
			return(half_heights[2]);		
		default:
			System.out.println("Error in RightBird, called by getNodeType...");
			return(-1);
		}
	}
	
	/*
	 * function to return the correct score, which is used for parameters update
	 */
	private int getRightScore(Node target, int initialpigs, int tmp_score){
		int returnScore = 0;
		//boolean bonusForPigs = true;
		
		// Computation of score that will be used to update the parameters of right regressor
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("LAST SCORE: " + t); 
		System.out.println("CURRENT SCORE:  " + tmp_score );
		
		returnScore = tmp_score - t;
		t = tmp_score;
		
		System.out.println("REAL REWARD for update: " + returnScore);
		System.out.println("PREDICTION FOR EXPECTED REWARD: " + target.hitvalue);
		System.out.println("DIFFERENCE: " + (returnScore-target.hitvalue));
		System.out.println("---------------------------------------------------------------------------------");
	
		// sleeps for 7 seconds to reach 11 sec sleep, 4 from cshoot
		int pigsafter = PigsAfterShoot(); 
		//PigsAfterShoot();
		
		int ct = 0;
		GameState state = aRobot.getState();
		
		while((state == GameState.PLAYING) && (ct < 2)){
			state = aRobot.getState();
			if(state == GameState.PLAYING){
				System.out.println("wait 2 seconds to ensure all objects in the scene static");
				ABUtil.mySleep(2000);
			}
			ct ++;
			pigsafter = PigsAfterShoot();
			//PigsAfterShoot();
		}
				
		// BONUS if a pig is killed ....
		int bonus = 0;
		if((pigsafter < initialpigs) || (state == GameState.WON)){
			bonus = 10000;
			returnScore = returnScore + bonus;
			System.out.println("---------------------------------------------------------------------");
			System.out.println("Initial pigs: " + initialpigs + " after shoot: " + pigsafter);
			System.out.println("bonus added: REAL REWARD UPDATED: " + returnScore);
			System.out.println("---------------------------------------------------------------------");
		}
				
		state = aRobot.getState();
		if(state == GameState.PLAYING){
			// scene non-static: some objects fall 
			int tmp_score1 = StateUtil.getScore(ActionRobot.proxy);						
			if(tmp_score1 > 0){
				// score changed due to non-static objects in the scene
				if (tmp_score1 != tmp_score){			
					System.out.println("Score changed due to non-static objects in the scene...");
					tmp_score = tmp_score1;
					returnScore = returnScore + (tmp_score1 - t);
					t = tmp_score1;
				}
				else
					System.out.println("Continue to Regressor Matrices update...\n");
			}
		}
		return returnScore;
	}
	
	/*
	 * function to detect how many pigs are left after a shot
	 */
	private int PigsAfterShoot(){
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		List<ABObject> pigstmp = vision.findPigsMBR();
	
		if(pigstmp.size() == 0){
			//System.out.println("There are no other pigs...!!!");
			GameState state = aRobot.getState();
			
			int ct = 0;
			while(state != GameState.WON && pigstmp.size() == 0){
				state = aRobot.getState();
				if(ct == 0) {
					System.out.println("Try to turn to state: WON");
				}
				ct++;
				screenshot = ActionRobot.doScreenShot();
				vision = new Vision(screenshot);
				pigstmp = vision.findPigsMBR();
			}
			return(pigstmp.size());
		}
		else{
			screenshot = null;
			vision = null;
			System.gc();
			return(pigstmp.size());
		}	
	}

	public static void main(String args[]) {
		Agent na = new Agent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
