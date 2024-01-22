package edu.uchicago.gerber.fox.mvc.controller;



import edu.uchicago.gerber.fox.mvc.model.*;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//The CommandCenter is a singleton that manages the state of the game.
//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private int numFoxes;
	private  int level;
	private int numJumps;
	private  long score;
	private  boolean paused;
	private  boolean muted;

	//this value is used to count the number of frames (full animation cycles) in the game
	private long frame;

	//the fox is located in the movFriends list, but since we use this reference a lot, we keep track of it in a
	//separate reference. Use final to ensure that the fox ref always points to the single fox object on heap.
	//Lombok will not provide setter methods on final members

	private final Fox fox = new Fox();

	private BackGround bgImage1 = new BackGround(new Point(899, 450));

	//lists containing our movables subdivided by team
	private final List<Movable> movFriends = new LinkedList<>();
	private final List<Movable> movFoes = new LinkedList<>();
	private final List<Movable> movCoins = new LinkedList<>();

	private final GameOpsQueue opsQueue = new GameOpsQueue();

	//for sound playing. Limit the number of threads to 5 at a time.
	private final ThreadPoolExecutor soundExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

	//singleton
	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {}

    //this class maintains game state - make this a singleton.
	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
			System.out.println("command Center init");
		}
		return instance;
	}


	public void initGame(){
		clearAll();
		setLevel(0);
		setScore(0);
		setPaused(false);
		//set to one greater than number of fox lives in your game as initFoxAndDecrementNum() also decrements
		setNumJumps(5);
		setNumFoxes(4);
		initFoxAndDecrementNumb();
		opsQueue.enqueue(bgImage1, GameOp.Action.ADD);
		//add the fox to the movFriends list
		opsQueue.enqueue(fox, GameOp.Action.ADD);
	}


	public void initFoxAndDecrementNumb(){
		numFoxes--;
		if(isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		fox.setInvisible(Fox.INITIAL_SPAWN_TIME/4);
		fox.setDeltaX(0);
		fox.setDeltaY(0);
		movFoes.clear();
		movCoins.clear();
		bgImage1.clearPits();
	}


	public void incrementFrame(){
		//use of ternary expression to simplify the logic to one line
		frame = frame < Long.MAX_VALUE ? frame + 1 : 0;
	}

	private void clearAll(){
		movFriends.clear();
		movFoes.clear();
		movCoins.clear();
	}

	public boolean isGameOver() {		//if the number of foxes is zero, then game over
		return numFoxes < 1;
	}

}
