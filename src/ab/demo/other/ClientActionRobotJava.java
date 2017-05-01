/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/

package ab.demo.other;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.GameStateExtractor.GameState;
import external.ClientMessageEncoder;

//Java interface of ClientActionRobot
public class ClientActionRobotJava extends ClientActionRobot {

	public ClientActionRobotJava(String ip) {
		super(ip);
	}
   
	//return game state as enum format
	public GameState checkState() {
		byte result = super.getState();
		
		// in about termination mode
		if(result == -1)
			return null;
		
		GameState state = GameState.values()[result];
		return state;
	}
	
	//return an array of best scores. the nth slot stores the score of (n+1)-th level
	public int[] checkScore(){
		
		byte[] scores = super.getBestScores();

		int[] _scores = new int[scores.length/4];
		for (int i = 0 ; i < _scores.length ; i++){
			_scores[i] = super.bytesToInt(scores[i * 4], scores[i*4 + 1], scores[i*4 + 2], scores[i*4 + 3]);
		}
		return _scores;
	}
	
	//send a shot message using int values as input
	public byte[] shoot(int fx, int fy, int dx, int dy, int t1, int t2, boolean polar) {
		return super.shoot(intToByteArray(fx), intToByteArray(fy),intToByteArray(dx), intToByteArray(dy), intToByteArray(t1), intToByteArray(t2), polar);
	}

	//send a shot sequence message using int arrays as input
	//one array per shot
	public byte[] cshootSequence(int[]... shots) {
		byte[][] byteShots = new byte[shots.length][24] ;
		int shotCount = 0;
		for (int[] shot : shots) {
			byteShots[shotCount] = ClientMessageEncoder.mergeArray(intToByteArray(shot[0])/*fx*/,
									intToByteArray(shot[1])/*fy*/, intToByteArray(shot[2])/*dx*/,
									intToByteArray(shot[3])/*dy*/, intToByteArray(shot[4])/*t1*/,
									intToByteArray(shot[5])/*t2*/);
			
			shotCount++;
		}
		return super.cshootSequence(byteShots);
	}
	
	public ABType getBirdTypeOnSling(Rectangle sling){
	
		if(sling == null){
			System.out.println("getBirdTypeOnSling() returns ABType.Unknown due to non-detected slingshot....");
			return ABType.Unknown;
		}
		else{
			int birdindex = 0;
			
			BufferedImage screenshot = doScreenShot();
			if(screenshot == null){
				System.out.println("getBirdTypeOnSling - doScreenShot()....");
				return null;
			}
			
			Vision vision = new Vision(screenshot);
		
			// find birds before zoom out	
			List<ABObject> _birds = vision.findBirdsRealShape();
			
			if(_birds == null) {
				_birds = vision.findBirdsMBR();
			}
		
			if(_birds == null){
				return ABType.Unknown;
			}
			else{
				if(_birds.isEmpty()){ 
					return ABType.Unknown;
				} 
				else {
					int birdsminy = 480;
					boolean flagfindbird = false;
				
					for(int i=0; i<_birds.size(); i++){
						if((Math.abs(_birds.get(i).x - sling.x) < 50) && (_birds.get(i).y < birdsminy)){
							birdsminy = _birds.get(i).y;
							birdindex = i;
							flagfindbird = true;
						}
					}
			
					if(flagfindbird) {
						return _birds.get(birdindex).getType();
					}
				}
			}
		}
		return ABType.Unknown;
	}
	
	// returns the achieved Score
	public int[] checkMyScore() {
		byte[] scores = super.getMyScore();
		
		int[] _scores = new int[scores.length/4];
		for (int i = 0 ; i < _scores.length ; i++){
			_scores[i] = super.bytesToInt(scores[i * 4], scores[i*4 + 1], scores[i*4 + 2], scores[i*4 + 3]);
		}
		return _scores;
	}
}
