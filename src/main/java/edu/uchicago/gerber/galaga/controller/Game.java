package edu.uchicago.gerber.galaga.controller;



import edu.uchicago.gerber.galaga.model.*;
import edu.uchicago.gerber.galaga.view.GamePanel;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

	// ===============================================
	// FIELDS
	// ===============================================

	public static final Dimension DIM = new Dimension(1100, 900); //the dimension of the game.
	private final GamePanel gmpPanel;
	//this is used throughout many classes.
	public static final Random R = new Random();

	public final static int ANI_DELAY = 40; // milliseconds between screen
	// updates (animation)

	public final static int FRAMES_PER_SECOND = 1000 / ANI_DELAY;

	private final Thread animationThread;


	private final int PAUSE = 80, // p key
			QUIT = 81, // q key
			LEFT = 37, // rotate left; left arrow
			RIGHT = 39, // rotate right; right arrow
			UP = 38, // thrust; up arrow
			DOWN = 40, // break, down arrow
			START = 83, // s key
			FIRE = 32, // space key
			MUTE = 77; // m-key mute

	// for possible future use
	// HYPER = 68, 					// D key
	// SHIELD = 65, 				// A key
	// SPECIAL = 70; 					// fire special weapon;  F key

	private final Clip clpThrust;
	private final Clip clpMusicBackground;

	//spawn every 30 seconds
	private static final int SPAWN_NEW_SHIP_FLOATER = FRAMES_PER_SECOND * 30;



	// ===============================================
	// ==CONSTRUCTOR
	// ===============================================

	public Game() {

		gmpPanel = new GamePanel(DIM);
		gmpPanel.addKeyListener(this); //Game object implements KeyListener
		clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
		clpMusicBackground = Sound.clipForLoopFactory("music-background.wav");

		//fire up the animation thread
		animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
		animationThread.start();


	}

	// ===============================================
	// ==METHODS
	// ===============================================

	public static void main(String args[]) {
		//typical Swing application start; we pass EventQueue a Runnable object.
		EventQueue.invokeLater(Game::new);
	}

	// Game implements runnable, and must have run method
	@Override
	public void run() {

		// lower animation thread's priority, thereby yielding to the "main" aka 'Event Dispatch'
		// thread which listens to keystrokes
		animationThread.setPriority(Thread.MIN_PRIORITY);

		// and get the current time
		long lStartTime = System.currentTimeMillis();

		// this thread animates the scene
		while (Thread.currentThread() == animationThread) {

			gmpPanel.update(gmpPanel.getGraphics()); // see GamePanel class
			checkCollisions();
			checkNewLevel();
			spawnNewShipFloater();

			// surround the sleep() in a try/catch block
			// this simply controls delay time between
			// the frames of the animation
			try {
				// The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update)
				// between frames takes longer than ANI_DELAY, then the difference between lStartTime -
				// System.currentTimeMillis() will be negative, then zero will be the sleep time
				lStartTime += ANI_DELAY;

				Thread.sleep(Math.max(0,
						lStartTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				// do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
			}
		} // end while
	} // end run

	private void checkCollisions() {

		Point pntFriendCenter, pntFoeCenter;
		int radFriend, radFoe;

		//This has order-of-growth of O(n^2), there is no way around this.
		for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
			for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {

				pntFriendCenter = movFriend.getCenter();
				pntFoeCenter = movFoe.getCenter();
				radFriend = movFriend.getRadius();
				radFoe = movFoe.getRadius();

				//detect collision
				if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {
					//remove the friend (so long as he is not protected)
					if (!movFriend.isProtected()){
						CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
					}
					//remove the foe
					CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
					Sound.playSound("kapow.wav");
				}

			}//end inner for
		}//end outer for

		//check for collisions between falcon and floaters. Order of growth of O(n) where n is number of floaters
		Point pntFalCenter = CommandCenter.getInstance().getFalcon().getCenter();
		int radFalcon = CommandCenter.getInstance().getFalcon().getRadius();

		Point pntFloaterCenter;
		int radFloater;
		for (Movable movFloater : CommandCenter.getInstance().getMovFloaters()) {
			pntFloaterCenter = movFloater.getCenter();
			radFloater = movFloater.getRadius();

			//detect collision
			if (pntFalCenter.distance(pntFloaterCenter) < (radFalcon + radFloater)) {

				CommandCenter.getInstance().getOpsQueue().enqueue(movFloater, GameOp.Action.REMOVE);
				Sound.playSound("pacman_eatghost.wav");

			}//end if
		}//end for

		processGameOpsQueue();

	}//end meth

	private void processGameOpsQueue() {

		//deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
		// mutating the movable linkedlists while iterating them above
		while(!CommandCenter.getInstance().getOpsQueue().isEmpty()){
			GameOp gameOp =  CommandCenter.getInstance().getOpsQueue().dequeue();
			Movable mov = gameOp.getMovable();
			GameOp.Action action = gameOp.getAction();

			switch (mov.getTeam()){
				case FOE:
					if (action == GameOp.Action.ADD){
						CommandCenter.getInstance().getMovFoes().add(mov);
					} else { //GameOp.Operation.REMOVE
						CommandCenter.getInstance().getMovFoes().remove(mov);
						if (mov instanceof Asteroid)
							spawnSmallerAsteroids((Asteroid) mov);
					}

					break;
				case FRIEND:
					if (action == GameOp.Action.ADD){
						CommandCenter.getInstance().getMovFriends().add(mov);
					} else { //GameOp.Operation.REMOVE
						if (mov instanceof Falcon) {
							CommandCenter.getInstance().initFalconAndDecrementFalconNum();
						} else {
							CommandCenter.getInstance().getMovFriends().remove(mov);
						}
					}
					break;

				case FLOATER:
					if (action == GameOp.Action.ADD){
						CommandCenter.getInstance().getMovFloaters().add(mov);
					} else { //GameOp.Operation.REMOVE
						CommandCenter.getInstance().getMovFloaters().remove(mov);
					}
					break;

				case DEBRIS:
					if (action == GameOp.Action.ADD){
						CommandCenter.getInstance().getMovDebris().add(mov);
					} else { //GameOp.Operation.REMOVE
						CommandCenter.getInstance().getMovDebris().remove(mov);
					}
					break;


			}

		}
	}


	private void spawnNewShipFloater() {

		//appears more often as your level increases.
		if ((System.currentTimeMillis() / ANI_DELAY) % (SPAWN_NEW_SHIP_FLOATER - CommandCenter.getInstance().getLevel() * 7L) == 0) {
			CommandCenter.getInstance().getOpsQueue().enqueue(new NewShipFloater(), GameOp.Action.ADD);
		}
	}


	//this method spawns new Large (0) Asteroids
	private void spawnBigAsteroids(int nNum) {
		while(nNum-- > 0) {
			//Asteroids with size of zero are big
			CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(0), GameOp.Action.ADD);

		}
	}

	private void spawnSmallerAsteroids(Asteroid originalAsteroid) {

		int nSize = originalAsteroid.getSize();
		if (nSize > 1) return; //return if Small (2) Asteroid

		//for large (0) and medium (1) sized Asteroids only, spawn 2 or 3 smaller asteroids respectively
		nSize += 2;
		while (nSize-- > 0) {
			CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(originalAsteroid), GameOp.Action.ADD);
		}

	}




	private boolean isLevelClear(){
		//if there are no more Asteroids on the screen
		boolean asteroidFree = true;
		for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
			if (movFoe instanceof Asteroid){
				asteroidFree = false;
				break;
			}
		}
		return asteroidFree;
	}

	private void checkNewLevel(){

		if (isLevelClear()) {
			//more asteroids at each level to increase difficulty
			CommandCenter.getInstance().setLevel(CommandCenter.getInstance().getLevel() + 1);
			spawnBigAsteroids(CommandCenter.getInstance().getLevel());
			//setFade e.g. protect the falcon so that player has time to avoid newly spawned asteroids.
			CommandCenter.getInstance().getFalcon().setFade(Falcon.FADE_INITIAL_VALUE);

		}
	}




	// Varargs for stopping looping-music-clips
	private static void stopLoopingSounds(Clip... clpClips) {
		for (Clip clp : clpClips) {
			clp.stop();
		}
	}

	// ===============================================
	// KEYLISTENER METHODS
	// ===============================================

	@Override
	public void keyPressed(KeyEvent e) {
		Falcon fal = CommandCenter.getInstance().getFalcon();
		int nKey = e.getKeyCode();

		if (nKey == START && CommandCenter.getInstance().isGameOver())
			CommandCenter.getInstance().initGame();

		if (fal != null) {

			switch (nKey) {
				case PAUSE:
					CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
					if (CommandCenter.getInstance().isPaused())
						stopLoopingSounds(clpMusicBackground, clpThrust);

					break;
				case QUIT:
					System.exit(0);
					break;
				case UP:
					fal.thrustOn();
					if (!CommandCenter.getInstance().isPaused() && !CommandCenter.getInstance().isGameOver())
						clpThrust.loop(Clip.LOOP_CONTINUOUSLY);
					break;

				case DOWN:
					fal.breakOn();
					break;
				case LEFT:
					fal.rotateLeft();
					break;
				case RIGHT:
					fal.rotateRight();
					break;

				// possible future use
				// case KILL:
				// case SHIELD:
				// case NUM_ENTER:

				default:
					break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Falcon fal = CommandCenter.getInstance().getFalcon();
		int nKey = e.getKeyCode();
		//show the key-code in the console
		System.out.println(nKey);

		if (fal != null) {
			switch (nKey) {
				case FIRE:
					CommandCenter.getInstance().getOpsQueue().enqueue(new Bullet(fal), GameOp.Action.ADD);
					Sound.playSound("laser.wav");
					break;


				case LEFT:
					fal.stopRotating();
					break;
				case RIGHT:
					fal.stopRotating();
					break;
				case UP:
					fal.thrustOff();
					clpThrust.stop();
					break;
				case DOWN:
					fal.breakOff();
					break;

				case MUTE:
					CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());

					if (!CommandCenter.getInstance().isMuted()){
						stopLoopingSounds(clpMusicBackground);
					}
					else {
						clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
					}
					break;

				default:
					break;
			}
		}
	}

	@Override
	// does nothing, but we need it b/c of KeyListener contract
	public void keyTyped(KeyEvent e) {
	}

}


