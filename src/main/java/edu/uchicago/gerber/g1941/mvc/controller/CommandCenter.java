package edu.uchicago.gerber.g1941.mvc.controller;

import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.airplane.AircraftFriend;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//The CommandCenter is a singleton that manages the state of the game.
//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private  int numFalcons;
	private  int level;
	private  long score;
	private  boolean paused;
	private  boolean muted;
	// Count the center of the middle Aircraft of Aircraft Group
	// Used in method generateAircraftEnemyAGroup() in Class Game
	private int AircraftEnemyAGroupCenterX;
    private boolean bossDead; // Count whether the final BOSS dead (Class AircraftEnemyE)
	private boolean bossOccur; // Count whether the final BOSS occurred (Class AircraftEnemyE)
	private boolean showMenu = false; // Menu is the map selection menu: user can select which map to play
	private boolean passed = false; // Whether passed the current map
	private boolean failed = false; // Whether Failed
	private boolean gameStart = false; // Count whether game has been init
	private int curMap = 1; // Count current MAP played
	private final int totalMap = 3; // Count how many MAP in this game
	private Map<Integer, Boolean> mapLocked = new HashMap<>(); // Count the MAP status, locked or unlocked
	private long frame;
	private final AircraftFriend aircraftFriend = new AircraftFriend();
	private final LinkedList<Movable> movDebris = new LinkedList<>();
	private final LinkedList<Movable> movFriends = new LinkedList<>();
	private final LinkedList<Movable> movFoes = new LinkedList<>();
	private final LinkedList<Movable> movFloaters = new LinkedList<>();
	private final GameOpsQueue opsQueue = new GameOpsQueue();
	private static CommandCenter instance = null; 	//singleton


	// If you find the game is hard to play, you can simply increase the HP of your Aircraft
	private final int AIRCRAFT_FRIEND_HP = 100;


	// Constructor made private
	private CommandCenter() {}


    //this class maintains game state - make this a singleton.
	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
			instance.mapLocked.put(1, false); // At the beginning, players can only play MAP 1
			instance.mapLocked.put(2, true); // Unlock until pass the previous one
			instance.mapLocked.put(3, true); // Unlock until pass the previous one
		}
		return instance;
	}


	public void initGame(){
		clearAll();
		setLevel(0);
		setScore(0);
		setPaused(false);
		setBossDead(false);
		setBossOccur(false);
		setPassed(false);
		setFailed(false);
		setShowMenu(false);
		setGameStart(true);
		aircraftFriend.setHp(AIRCRAFT_FRIEND_HP);
		aircraftFriend.setTotalHp(AIRCRAFT_FRIEND_HP);
		aircraftFriend.setBulletLevel(1);
		aircraftFriend.setMaxBulletLevel(1);
		aircraftFriend.setRemainNumMissile(0);
		aircraftFriend.decrementFalconNumAndSpawn();
		opsQueue.enqueue(aircraftFriend, GameOp.Action.ADD); //add the falcon to the movFriends list
	}


	public void incrementFrame(){
		frame = frame < Long.MAX_VALUE ? frame + 1 : 0;
	}


	private void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
	}


	// Check whether game is over
	// Update variables
	public boolean isGameOver() {
		if (aircraftFriend.getHp() <= 0) {
			if (gameStart) {
				setFailed(true);
			}
			setGameStart(false);
			return true;
		}
		if (bossOccur && bossDead) {
			aircraftFriend.setHp(0);
			setPassed(true);
			if (getCurMap() < getTotalMap()) {
				mapLocked.put(getCurMap() + 1, false);
			}
			setGameStart(false);
			return true;
		}
		return false;
	}



}
