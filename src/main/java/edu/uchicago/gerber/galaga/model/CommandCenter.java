package edu.uchicago.gerber.galaga.model;




import edu.uchicago.gerber.galaga.controller.Game;
import edu.uchicago.gerber.galaga.controller.Sound;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private  int numFalcons;
	private  int level;
	private  long score;
	private  boolean paused;
	private  boolean muted;

	//the falcon is located in the movFriends list, but since we use this reference a lot, we keep track of it in a
	//separate reference. Use final to ensure that the falcon ref always points to the single falcon object on heap
	//Lombok will not provide setter methods on final members
	private final Falcon falcon  = new Falcon();

	//lists containing our movables
	private final List<Movable> movDebris = new LinkedList<>();
	private final List<Movable> movFriends = new LinkedList<>();
	private final List<Movable> movFoes = new LinkedList<>();
	private final List<Movable> movFloaters = new LinkedList<>();

	private final GameOpsQueue opsQueue = new GameOpsQueue();

	//singleton
	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {}

    //this class maintains game state - make this a singleton.
	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}


	public void initGame(){
		clearAll();
		setLevel(1);
		setScore(0);
		setPaused(false);
		//set to one greater than number of falcons lives in your game as initFalconAndDecrementNum() also decrements
		setNumFalcons(4);
		initFalconAndDecrementFalconNum();
		opsQueue.enqueue(falcon, GameOp.Action.ADD);

	}


	public void initFalconAndDecrementFalconNum(){
		setNumFalcons(getNumFalcons() - 1);
		if (isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		falcon.setFade(Falcon.FADE_INITIAL_VALUE);
		//put falcon in the middle of the game-space
		falcon.setCenter(new Point(Game.DIM.width / 2,  Game.DIM.height - 100));
		falcon.setOrientation(270);
		falcon.setDeltaX(0);
		falcon.setDeltaY(0);
	}

	private void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
	}

	public boolean isGameOver() {		//if the number of falcons is zero, then game over
		return getNumFalcons() <= 0;
	}



}
