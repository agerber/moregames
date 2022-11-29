package edu.uchicago.gerber.raster_asteroids.model;




import edu.uchicago.gerber.raster_asteroids.controller.Game;
import edu.uchicago.gerber.raster_asteroids.controller.Sound;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private  int numFalcons;
	private  int level;
	private  long score;
	private  boolean paused;
	private  boolean muted;

	//this value is used count the number of frames (full animation cycles) in the game
	private long frame;

	//the falcon is located in the movFriends list, but since we use this reference a lot, we keep track of it in a
	//separate reference. Use final to ensure that the falcon ref always points to the single falcon object on heap.
	//Lombok will not provide setter methods on final members
	private final Falcon falcon  = new Falcon();

	//lists containing our movables
	private final List<Movable> movDebris = new LinkedList<>();
	private final List<Movable> movFriends = new LinkedList<>();
	private final List<Movable> movFoes = new LinkedList<>();
	private final List<Movable> movFloaters = new LinkedList<>();

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
		//add the falcon to the movFriends list
		opsQueue.enqueue(falcon, GameOp.Action.ADD);

		//doesn't add much functionality to the game, but shows how to add walls or rectangular elements one
		//brick at a time
		createWall();

	}

	private void createWall(){

		final int BRICK_SIZE = Game.DIM.width / 30, ROWS = 20, COLS = 2,  X_OFFSET = BRICK_SIZE * 5, Y_OFFSET =
				Game.DIM.height - 200;

		for (int nRow = 0; nRow < ROWS; nRow++)
			for (int nCol = 0; nCol < COLS; nCol++)
				opsQueue.enqueue(new Brick(new Point(nRow * BRICK_SIZE + X_OFFSET, nCol * BRICK_SIZE+ Y_OFFSET), BRICK_SIZE),
						GameOp.Action.ADD);


	}


	public void initFalconAndDecrementFalconNum(){
		setNumFalcons(getNumFalcons() - 1);
		if (isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		falcon.setSpawn(Falcon.INITIAL_SPAWN_TIME);
		//put falcon in the middle of the game-space
		falcon.setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
		falcon.setOrientation(Game.R.nextInt(360));
		falcon.setDeltaX(0);
		falcon.setDeltaY(0);
	}

	public void incrementFrame(){
		if (frame == Long.MAX_VALUE){
			frame = 0;
		} else {
			frame++;
		}
	}

	private void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
	}

	public boolean isGameOver() {		//if the number of falcons is zero, then game over
		return numFalcons <= 0;
	}



	////////////////////////////////////////////////////////////////////
	//Utility methods for transforming cartesian2Polar, pointsListToArray, etc.
	////////////////////////////////////////////////////////////////////
	public static List<PolarPoint> cartesianToPolar(List<Point> pntCartesians) {

		BiFunction<Point, Double, PolarPoint> cartToPolarTransform = (pnt, hyp) -> new PolarPoint(
				//this is r from PolarPoint(r,theta).
				hypotFunction(pnt.x, pnt.y) / hyp, //r is relative to the largestHypotenuse
				//this is theta from PolarPoint(r,theta)
				Math.toDegrees(Math.atan2(pnt.y, pnt.x)) * Math.PI / 180
		);


		//determine the largest hypotenuse
		double largestHypotenuse = 0;
		for (Point pnt : pntCartesians)
			if (hypotFunction(pnt.x, pnt.y) > largestHypotenuse)
				largestHypotenuse = hypotFunction(pnt.x, pnt.y);


		//we must make hypotenuse final to pass into a stream.
		final double hyp = largestHypotenuse;


		return pntCartesians.stream()
				.map(pnt -> cartToPolarTransform.apply(pnt, hyp))
				.collect(Collectors.toList());

	}

	public static Point[] pointsListToArray(List<Point> listPoints) {
		return listPoints.stream()
				.toArray(Point[]::new);

	}

	//private helper method
	private static double hypotFunction(double sideX, double sideY) {
		return Math.sqrt(Math.pow(sideX, 2) + Math.pow(sideY, 2));
	}





}
