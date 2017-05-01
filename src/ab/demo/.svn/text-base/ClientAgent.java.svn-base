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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.planner.TrajectoryPlanner;
import ab.utils.ABUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.real.shape.Poly;
import ab.vision.Vision;

// Agent (server/client version)
public class ClientAgent implements Runnable {
	public static int time_limit = 12;
	private boolean slingflag = false;					// boolean help flag to capture the right slingshot
	private int [] OpScores = {28880,60720,41840,30000,66260,30000};		// initialized to test
	private int [] MyScore;								
	private int [] passed;									// levels completed
	private int tryCounter = 0;
	private int pass = 0;									// variable to control level's assignment
	private int scaleCT = 0;								// help variable to control scale changes
	private Rectangle room = null;							// Rectangle to allocate game scene
	private double [] half_heights = {14,14,14,14,14}; 		// used for bird limit
	private int t;											// score help variable 
	private Rectangle slingshot = new Rectangle();			// help variable for slingshot detection
	private ClientActionRobotJava ar;						// Wrapper of the communicating messages
	public byte currentLevel = -1;
	public int failedCounter = 0;
	public int[] solved;									// solved levels 
	private TrajectoryPlanner tp; 
	private Settings settings;								// to set the method's parameters	
	private Features features;								// to manipulate available features 
	private ArrayList<Regressor> regressors;				// available regressors to predict expected reward for each shot
	private int id = 29590;								// team unique id: used for competition
	private boolean firstShot;							
	private boolean safeModeShot = true;
	
	/**
	 * Constructor using the default IP
	 * */
	public ClientAgent(){
		// the default ip is the localhost
		ar = new ClientActionRobotJava("127.0.0.1");
		tp = new TrajectoryPlanner();
		firstShot = true;
		initialization();					
	}
	
	/**
	 * Constructor with a specified IP
	 * */
	public ClientAgent(String ip){
		ar = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlanner();
		firstShot = true;
		initialization();					
	}
	
	/*
	 * Constructor with specified IP and Team ID
	 */
	public ClientAgent(String ip, int id){
		ar = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlanner();
		firstShot = true;
		this.id = id;
		initialization();
	}
	
	// initialize parameters' files
	private void initialization(){
		
		settings = new Settings();
		features = new Features(settings);
		regressors = new ArrayList<Regressor>();
		
		// ArrayList of regressors creation, according to settings
		for (int i=0; i<settings.get_num_of_regressors(); i++){
			regressors.add(new Regressor(settings.get_num_of_clusters(), settings.get_alpha(), settings.get_beta(), settings));
		}
		
		if(!settings.get_load_from_file()){			// initialization of S0, a->0
			System.out.println("w0 and S0 are initialized in [0] and [(a-1)xI]... ");	
		}
		else {	
			System.out.println("W and S Matrices are loaded from existing files....");
			settings.LoadWSNFiles(regressors);
		}
		
		t = 0;									// score variable
		firstShot = true;						// true if it is the first shot for each level
	}
	
	public int getNextLevel(){
		int level = 0;
		boolean unsolved = false;
		
		if(pass == 0){		
			//all the levels have been solved, then get the first unsolved level
			System.out.println("1st pass....");
			for (int i = 0; i < solved.length; i++){
				if(solved[i] == 0){
					unsolved = true;
					level = (byte)(i + 1);
					
					if(level <= currentLevel && currentLevel < solved.length){
						continue;
					}
					else{
						return level;
					}
				}
			}
			
			if(unsolved)
				return level;
		   
			level = (byte)((this.currentLevel + 1) % solved.length);
			
			if(level == 0)
				level = solved.length;
			return level; 
		}
		else{
			System.out.println("-----------------------------------------------------------------------");
			System.out.println(pass + " pass, level chosen according to Opponents scores....");
			int [] diffScore = new int[OpScores.length];
			int max = -100000;
			
			for(int i=0; i<OpScores.length; i++){
				diffScore[i] = OpScores[i] - MyScore[i];
	
				if(diffScore[i] > max && passed[i] < 1){	
					max = diffScore[i];
					level = i+1;
				}
			}
			
			for(int i=0; i<diffScore.length; i++){
				System.out.println("level " + (i+1) + " -> " + diffScore[i] + " passed: " + passed[i]);
			}
			
			diffScore = null;
			System.gc();
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("Level " + level + " loading.....");
			tryCounter++;
			System.out.println("-----------------------------------------------------------------------");
			if(tryCounter == 5){
				System.out.println("Trial completed ....");
				tryCounter = 0;
				for(int i=0; i<passed.length; i++){
					passed[i] = 0;
				}
			}
			else{
				passed[level-1]++;
			}
			
			return level;
		}
	}
	

    /* 
     * Run the Client (Naive Agent)
     */

	public void run() {	
		
		byte[] info = ar.configure(ClientActionRobot.intToByteArray(id));
		solved = new int[info[2]];
		passed = new int[info[2]];
		MyScore = new int[info[2]];
		//OpScores = new int[info[2]];

		// just to test that it distinguishes QR and Finals
		// info[0] = 2;
		
		System.out.println("Round info: " + info[0]);
		System.out.println("Time available (in minutes): " + info[1]);
		System.out.println("Available levels: " + info[2]);
		
		if(info[0] == 2){			// qualification Round 2
			System.out.println("Qualification Round 2, myScore from Round 1 loading....");
			loadScoresR1();
			pass = 2;   			// exei ginei hdh "1" perasma sto QR1
		}
		
		currentLevel = (byte)getNextLevel(); 
		ar.loadLevel(currentLevel);
	
		GameState state;
		
		// check the pass
		// in first pass to check about the unsolved
		// the second pass the max difference, according to diffScore
		while (true){
			state = solve();
			passed[currentLevel-1]++;
	
			if (state == GameState.WON){
				
				int[] scores;
			
				if(info[0] != 2){					//!= QR2
					scores = ar.checkMyScore();
					
					if(scores[0] < 0){
						System.out.println("Run - checkMyScore()...");
						storeData();
					}
					else{
						System.out.println("Copy scores to MyScore....");
						for(int i=0; i<MyScore.length; i++){
							MyScore[i] = scores[i];
						}
					}
				}
				else{		// QR2
					scores = ar.checkMyScore();
					if(scores[0] < 0){
						System.out.println("Run - checkMyScore()...");
						storeData();
					}
					
					if(MyScore[currentLevel-1] < scores[currentLevel-1]){
						System.out.println("New Score added to myScore.... In level: " + currentLevel);
						MyScore[currentLevel-1] = scores[currentLevel-1];
					}
					else{
						System.out.println("NO score added to myScore....");
					}
				}
				
				/*
				System.out.println("scores");
				for (int i = 0; i < scores.length ; i ++){
					System.out.print( " level " + (i+1) + ": " + scores[i]);
				    	if(scores[i] > 0)
				    		solved[i] = 1;	
				}
				System.out.println("\n");
				*/
				
				System.out.println("MyScore...");
				// was scores
				for (int i = 0; i < MyScore.length ; i ++){
					System.out.print( " level " + (i+1) + ": " + MyScore[i]);
				    	if(MyScore[i] > 0)
				    		solved[i] = 1;	
				}
				System.out.println("\n");
				
				int ct = 0;
				for(int i=0; i<passed.length; i++){
					if(passed[i] > 0){
						ct++;
					}
					else
						break;
				}
				
				if(ct == info[2]){
					pass++;
					for(int i=0; i<passed.length; i++){
						passed[i] = 0;
					}
					System.out.println("Pass -> " + pass);
				}
				
				currentLevel = (byte)getNextLevel(); 
				
				byte cload = ar.loadLevel(currentLevel);
				if(cload == -1){
					System.out.println("Run - ar.loadlevel....");
					storeData();
				}
			
				//display the global best scores
				// in comments to check Round 2
				scores = ar.checkScore();
				if(scores[0] < 0){
					System.out.println("Run - checkScore.....");
					storeData();
				}
				else{
					for(int i=0; i<OpScores.length; i++)
						OpScores[i] = scores[i];
				}
				
				// was scores
				System.out.println("The global best score: ");
				for (int i = 0; i < OpScores.length ; i ++){
					System.out.print( " level " + (i+1) + ": " + OpScores[i]);
				}
				System.out.println();
				
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
				slingflag = false;
				t = 0;
			} 
			else if (state == GameState.LOST){
				failedCounter++;
				
				if(pass > 1){
					currentLevel = (byte)getNextLevel();
					
					byte cload = ar.loadLevel(currentLevel);
					if(cload == -1){
						System.out.println("Run - loadLevel - Lost mode....");
						storeData();
					}
				}
				else{
					if(failedCounter > 3){
						failedCounter = 0;
						currentLevel = (byte)getNextLevel(); 
						
						byte cload = ar.loadLevel(currentLevel);
						if(cload == -1){
							System.out.println("Run - loadLevel - Lost mode....");
							storeData();
						}
					}
					else{		
						System.out.println("restart");
						t = 0;
						byte cres = ar.restartLevel();
						if(cres == -1){
							System.out.println("Run - restartlevel()...");
							storeData();
						}
					}
				}
				
			} 
			else if (state == GameState.LEVEL_SELECTION) {
				System.out.println("unexpected level selection page, go to the last current level : " + currentLevel);
				byte cres = ar.loadLevel(currentLevel);
				if(cres == -1){
					System.out.println("Run - loadlevel()...");
					storeData();
				}
			}
			else if (state == GameState.MAIN_MENU) {
				System.out.println("unexpected main menu page, reload the level : " + currentLevel);
			
				byte cres = ar.loadLevel(currentLevel);
				if(cres == -1){
					System.out.println("Run - loadlevel()...");
					storeData();
				}
			}
			else if (state == GameState.EPISODE_MENU) {
				System.out.println("unexpected episode menu page, reload the level: " + currentLevel);
				
				byte cres = ar.loadLevel(currentLevel);
				if(cres == -1){
					System.out.println("Run - loadlevel()...");
					storeData();
				}
			}
		}
	}

	/*
	 * function to load score from Qualification Round 1
	 */
	private void loadScoresR1(){
		try{
			System.out.println("Load scores from qualification Round 1...");
			String file = new String("myScore.txt");
			
			BufferedReader inn = new BufferedReader(new FileReader(file));
			
			int i=0;
			String tmp = inn.readLine();
			while( tmp != null){
				
				int score  = Integer.parseInt(tmp);
				MyScore[i] = score;
				System.out.println("level " + (i+1) + " : " + MyScore[i] );
				i++;
				tmp = inn.readLine();
			}
	
			inn.close();
		}
		catch (IOException e){
			System.out.println("Error while loading Scores of qualification R1....");
			System.out.println("Exception: " +e);
		}
	}
	
	/*
	 *  when server turns to "about terminate mode"
	 *  the agent must store all data needed and exit 
	 *  available time: 2 minutes 
	 */ 
	private void storeData(){
		System.out.println("In about termination mode....");
		System.out.println("Agent stores data....");
		
		int count = 0;
		while(count < 20){
			System.out.print(".");
			count++;
		}
		
		// write parameters of Bayesian Regression in Files
		settings.writeAllParametersFiles(regressors);
		System.out.println("Parameters stored....");
		
		count = 0;
		while(count < 20){
			System.out.print(".");
			count++;
		}
		
		// write MyScore to file
		writeMyScore();
		
		count = 0;
		while(count < 20){
			System.out.print(".");
			count++;
		}
		
		// generally, data to store.....
		System.out.println("Data stored...Exit....");
		System.exit(0);
	}
	
	private void writeMyScore(){
		File myScoreFile = new File("Competition_Score.txt");
		
		try{
			if (!myScoreFile.exists()){
				myScoreFile.createNewFile();
			}
			
			PrintWriter inFile = new PrintWriter(new FileWriter(myScoreFile, false));
			
			for(int i=0; i<MyScore.length; i++){
				inFile.println(MyScore[i]);
			}
			inFile.close();
			System.out.println("MyScore stored .....");
			
		}catch(IOException e){
			System.out.println("Error while writing MyScore to file....");
		}
	}
	
	/* 
	* Solve a particular level by shooting birds directly to pigs
	* @return GameState: the game state after shots.
    */
	public GameState solve(){
		// help variable for score
		int returnScore = 0;
			
		// boolean variable to show if shot is made
		boolean shotFlag = false;
				
		// capture Image
		BufferedImage screenshot = ar.doScreenShot();
		if(screenshot == null){
			System.out.println("Solve - doScreenShot()....");
			storeData();	
		}

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
		
		GameState ThisState = ar.checkState();
		if(ThisState == null){
			System.out.println("Solve - checkState().....");
			storeData();
		}
				
		// confirm the right capture of slingshot
		while (sling == null && ThisState == GameState.PLAYING) {
			// check state
			ThisState = ar.checkState();
			if(ThisState == null){
				System.out.println("Solve - checkState....");
				storeData();
			}
			
			System.out.println("No slingshot detected. Please remove pop up or zoom out...");
			///////It has been updated
			ar.fullyZoomOut();
			byte res = ar.fullyZoomOut();
			if( res == -1){
				System.out.println("Solve - fullyZoomOut....");
				storeData();
			}

			screenshot = ar.doScreenShot();
			if(screenshot == null){
				System.out.println("Solve - doScreenShot()....");
				storeData();
			}
			
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
		/*end of Scene detection*/
		///////////////////////////////////////////////////////////////////////////////////////////
				
		GameState state = ar.checkState();
		if(state == null){
			System.out.println("Solve - checkState....");
			storeData();
		}
				
		if (sling != null){
			if (!pigs.isEmpty() && !PigsObjects.isEmpty()) {
									
				// allocate our room to find states of game
				if(firstShot){
					room = ABUtil.findOurRoom(pigs, objects);
					room = new Rectangle(room.x-8, room.y-8, room.width+16, room.height+16);
					scaleCT = 0;
				}		
				
				// bird type on sling detection 
				ABType bird =  ar.getBirdTypeOnSling(sling);
				if(bird == null){
					System.out.println("Solve() - Just after getBirdTypeOnSling().....");
					storeData();
				}
				int birdtype = bird.ordinal();
						
				boolean whitebird = bird.equals(ABType.WhiteBird);
				double limit = setLimit(birdtype);
						
				//Tree Construction
				Tree tree = new Tree(tp);
				tree.TreeConstruction(room, PigsObjects, room.x, room.y, room.width, pigs, tnts, sling, bird, limit); 
						
				//Check feasibility of each node
				Feasibility feasibility = new Feasibility(room, tp);
				feasibility.CheckFeasibility(tree, bird, sling, hills, mostDistantObj, limit);
				
				tree.SetFeatures(room, pigs, tnts, sling, bird, limit);
				// set Phi Matrix for each feasible Node of our Tree 
				features.setPhiX(tree);
						
				//Compute hit value
				tree.ComputeHitvalue(regressors);
						
				// print the constructed Tree
				tree.Print(settings.get_type_of_features());
						 
				if (firstShot){
					t = 0;
				}
						
				// select the Tree's Node that will be hit in that shot
				Node target = tree.NodeSelection(bird);
						
				// to check case that a "null" target Node is returned
				boolean nullflag = false;
				if(target == null){
					target = tree.NullTargetNode(sling);
					nullflag = true;
				}
					
				// preparation for shoot according to target Node
				Point releasePoint = null, refPoint  = null;
				int dx,dy, tapTime = 0;
				{
					boolean typeOFbirdANDmaterial = ((bird.equals(ABType.BlueBird) || bird.equals(ABType.YellowBird) || bird.equals(ABType.RedBird) || bird.equals(ABType.Unknown)) 
							&& (target.type.equals("Pig") || target.type.equals("TNT")));
					TargetNode tpNode;
							
					// give the target according to previous conditions
					if(!whitebird){
						tpNode = target.targetPointSelection();
					}
					else{
						// White Bird case
						System.out.println("White bird....");
						double point = 0;
						if(nullflag){
							System.out.println("Get point 0....");
							point = 0;
						}
						else{
							point = 0.50*(target.targetNode.size()-1);	// 0: the highest point of trajectory	
						}
						System.out.println("Point: " + point + " points: "+ (target.targetNode.size()-1));
						System.out.println("index: " + point + " -> " + "[" + target.targetNode.get((int)point).getTargetPoint().x +
											"," + target.targetNode.get((int)point).getTargetPoint().y + "]");
						tpNode = target.targetNode.get((int)point); 
					}
							
					System.out.println("Hit side: " + tpNode.getLabel());
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
					refPoint = tp.getReferencePoint(sling);
			
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,releasePoint);
						System.out.println("Release Angle: "+ Math.toDegrees(releaseAngle) + "\n");
								
						// function to select tap time according to bird and trajectory
						// if tapFlag, then tapInterval is set
						if(!tapFlag)
							tapInterval = ABUtil.tapTimeSelection(bird, tpNode, releaseAngle);
						
						int gap = 0;
								
						// special treatment for taptime when a WhiteBird is on sling
						// positive and negative gradient
						if(whitebird){		
							Point tmp =  tpNode.getTargetPoint();
							List<Point> trajP = new ArrayList<Point>();
							trajP = tp.predictTrajectory(sling,releasePoint);
							
							gap = ABUtil.WhitefindGap(trajP, tmp);
							tmp.x = tmp.x - gap;
							tapTime =  tp.getTimeByDistance(sling, releasePoint, tmp);
						}
						else{
							tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						}
							
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						System.out.println("Target Node is shot by the agent ...!!!!!");
						shotFlag = true;
					}
					else{
						System.err.println("No Release Point Found");
						// Garbage Collector
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
					byte cres = ar.fullyZoomOut();
					if( cres == -1){
						System.out.println("Solve - fullyZoomOut....");
						storeData();
					}
					
					screenshot = ar.doScreenShot();
					if(screenshot == null){
						System.out.println("Solve - doScreenShot()....");
						storeData();
					}
					
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
								byte[] check = null;
								
								if(safeModeShot){
									System.out.println("Shot will be executed in safe mode...");
									check = ar.shoot(refPoint.x, refPoint.y, dx, dy, 0, tapTime, false);	
								}
								else{
									// shoot in fast mode
									System.out.println("Shot will be executed in fast mode...");
									check = ar.shootFast(ClientActionRobot.intToByteArray(refPoint.x), ClientActionRobot.intToByteArray(refPoint.y), 
											ClientActionRobot.intToByteArray(dx), ClientActionRobot.intToByteArray(dy), ClientActionRobot.intToByteArray(0),
											ClientActionRobot.intToByteArray(tapTime), false);
								}
								
								if(check[0] == -1){
									System.out.println("Solve - ar.Shoot()....");
									storeData();
								}
								
								int tmp_score = 0;
								boolean fscore = false;
										
								state = ar.checkState();
								if(state == null){
									System.out.println("Solve - checkState....");
									storeData();
								}
								
								//if(state == GameState.PLAYING)
									//mySleep(1000);
								
								if(state == GameState.LOST){
									fscore = true;
									tmp_score = getCurrentScore();
								}
										
								if(!fscore)
									tmp_score = getCurrentScore();		
								
								scoreflag = false; 
								if(tmp_score >= 0){
									scoreflag = true;
									// function to get the right score
									returnScore = getRightScore(target, initialpigs, tmp_score);
								}
										
								state = ar.checkState();
								if(state == null){
									System.out.println("Solve - checkState....");
									storeData();
								}
								
								if (state == GameState.PLAYING){
									screenshot = ar.doScreenShot();
									if(screenshot == null){
										System.out.println("getCurrentScore - doScreenShot()....");
										storeData();
									}
									
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
							
				if(!bird.equals(ABType.Unknown)){				// if bird is known, to avoid crash
					if(target.PhiX != null){					// if Phi Matrix exists, to avoid crash
						if (scoreflag){
							System.out.println("REAL REWARD for update:  " + returnScore);
							System.out.println("PREDICTION FOR EXPECTED REWARD: " + target.hitvalue);
							System.out.println("DIFFERENCE: " + (returnScore-target.hitvalue));
							System.out.println("---------------------------------------------------------------------------------");
								
							regressors.get(target.NodeType).BayesianRegression(target, returnScore);	
								
							System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
							System.out.println("Write modified parameters in Files....");
								
							settings.writeWSNFile(regressors, target.NodeType);
								
							System.out.println("Parameters updated......");
							System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
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
				System.out.println("Shot completed ....");
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
			return(half_heights[2]);		// unknown means blue
		default:
			System.out.println("Error in RightBird, called by getNodeType...");
			return(-1);
		}
	}
	
	private int getCurrentScore(){
    	
    	GameStateExtractor gameStateExtractor = new GameStateExtractor();
    	
    	GameState state = ar.checkState();
    	if(state == null){
    		System.out.println("getCurrentScore() - checkState().....");
    		storeData();
    	}
    
    	BufferedImage screenshot = ar.doScreenShot();
    	if(screenshot == null){
			System.out.println("getCurrentScore - doScreenShot()....");
			storeData();
		}
    		
    	if (state == GameState.PLAYING)
    		return gameStateExtractor.getScoreInGame(screenshot);
    	else 
    		if(state == GameState.WON)
    			return gameStateExtractor.getScoreEndGame(screenshot);
		
    	int score = -1;
    	if(gameStateExtractor.getScoreEndGame(screenshot) == -1){
    		score = -1;
    		System.out.println(" Game score is unavailable "); 	
    	}
    	
    	return score;			
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
		GameState state = ar.checkState();
		if(state == null){
			System.out.println("Solve - checkState....");
			storeData();
		}
		
		while((state == GameState.PLAYING) && (ct < 2)){
			state = ar.checkState();
			if(state == null){
				System.out.println("Solve - checkState....");
				storeData();
			}
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
		if(pigsafter < initialpigs){
			bonus = 10000;
			returnScore = returnScore + bonus;
			System.out.println("---------------------------------------------------------------------");
			System.out.println("Initial pigs: " + initialpigs + " after shoot: " + pigsafter);
			System.out.println("bonus added: REAL REWARD UPDATED: " + returnScore);
			System.out.println("---------------------------------------------------------------------");
		}
						
		state = ar.checkState();
		if(state == null){
			System.out.println("Solve - checkState....");
			storeData();
		}
		
		if(state == GameState.PLAYING){
			// scene non-static: some objects fall 
			int tmp_score1 = getCurrentScore();						
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
	 * function to detect how many pigs 
	 * are left after a shot
	 */
	private int PigsAfterShoot(){

		BufferedImage screenshot = ar.doScreenShot();
		if(screenshot == null){
			System.out.println("PigsAfterShoot - doScreenShot()....");
			storeData();
		}
		
		Vision vision = new Vision(screenshot);
		List<ABObject> pigstmp = vision.findPigsMBR();
	
		if(pigstmp.size() == 0){
			//System.out.println("There are no other pigs...!!!");
			GameState state = ar.checkState();
			
			if(state == null){
				System.out.println("Solve - checkState....");
				storeData();
			}
			
			int ct = 0;
			while(state != GameState.WON && pigstmp.size() == 0){
				state = ar.checkState();
				if(state == null){
					System.out.println("Solve - checkState....");
					storeData();
				}
				if(ct == 0) {
					System.out.println("Try to turn to state: WON");
				}
				ct++;
				
				screenshot = ar.doScreenShot();
				if(screenshot == null){
					System.out.println("Solve - doScreenShot()....");
					storeData();
				}
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

		ClientAgent na;
		if(args.length > 0){
			na = new ClientAgent(args[0]);
		}
		else{
			na = new ClientAgent();
		}		
		na.run();
		
	}
}
