package edu.uchicago.gerber.sinistar.mvc.controller;



import edu.uchicago.gerber.sinistar.mvc.model.*;
import edu.uchicago.gerber.sinistar.mvc.view.GamePanel;


import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.awt.Toolkit;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

	// ===============================================
	// FIELDS
	// ===============================================

	public static final Dimension DIM = new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height - 20); //the dimension of the game.
    public static final int DIMENSION_SCALAR = 5;
    public static final int FRAMES_BETWEEN_BULLETS = 1;
	private GamePanel gmpPanel;
	public static Random R = new Random();
	public final static int ANI_DELAY = 45; // milliseconds between screen
											// updates (animation)
    private final static int BUMP_NUMBER = 15;
	private Thread thrAnim;
	private int nTick = 0;
    private int framesSinceBullet = 0;

    private boolean fireBullets = false;

	private final int PAUSE = 80, // p key
			QUIT = 81, // q key
			LEFT = 37, // rotate left; left arrow
			RIGHT = 39, // rotate right; right arrow
			UP = 38, // thrust; up arrow
			START = 83, // s key
			FIRE = 32, // space key
	 		SPECIAL = 70; // fire special weapon;  F key

	private Clip clpThrust;

	// ===============================================
	// ==CONSTRUCTOR
	// ===============================================

	public Game() {

		gmpPanel = new GamePanel(DIM);
		gmpPanel.addKeyListener(this);
		clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
	}

	// ===============================================
	// ==METHODS
	// ===============================================

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() { // uses the Event dispatch thread from Java 5 (refactored)
					public void run() {
						try {
							Game game = new Game(); // construct itself
							game.fireUpAnimThread();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void fireUpAnimThread() { // called initially
		if (thrAnim == null) {
			thrAnim = new Thread(this); // pass the thread a runnable object (this)
			thrAnim.start();
		}
	}

	// implements runnable - must have run method
	public void run() {

		// lower this thread's priority; let the "main" aka 'Event Dispatch'
		// thread do what it needs to do first
		thrAnim.setPriority(Thread.MIN_PRIORITY);

		// and get the current time
		long lStartTime = System.currentTimeMillis();
		// this thread animates the scene
		while (Thread.currentThread() == thrAnim) {
			tick();
			gmpPanel.update(gmpPanel.getGraphics()); // update takes the graphics context we must
														// surround the sleep() in a try/catch block
														// this simply controls delay time between 
														// the frames of the animation

			//this might be a good place to check for collisions
			checkCollisions();
            spawnAsteroids();
            spawnWorkers();
            spawnTurrets();
			fireTurrets();
			fireFalcon();

			try {
				// The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update) 
				// between frames takes longer than ANI_DELAY, then the difference between lStartTime - 
				// System.currentTimeMillis() will be negative, then zero will be the sleep time
				lStartTime += ANI_DELAY;
				Thread.sleep(Math.max(0,
						lStartTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				// just skip this frame -- no big deal
				continue;
			}
		} // end while
	} // end run

	private void checkCollisions() {
		Point pntMovOneCenter, pntMovTwoCenter;
		for (Movable movOne : CommandCenter.getInstance().getMov()) {
			for (Movable movTwo : CommandCenter.getInstance().getMov()) {
				if (movOne instanceof Sprite && movTwo instanceof Sprite) {
                    if (movOne.getTeam() != Movable.Team.DEBRIS && movTwo.getTeam() != Movable.Team.DEBRIS){
                        Sprite spriteOne = (Sprite) movOne;
                        Sprite spriteTwo = (Sprite) movTwo;
                        pntMovOneCenter = spriteOne.getCenter();
                        pntMovTwoCenter = spriteTwo.getCenter();
                        if (pntMovOneCenter.distance(pntMovTwoCenter) < (movOne.getRadius() + movTwo.getRadius())) {
                            checkBump(spriteOne, spriteTwo);
                            checkWorkerSinistar(spriteOne, spriteTwo);
                            checkCollisionCrystal(spriteOne, spriteTwo);
                            checkFalconSinistar(spriteOne, spriteTwo);
                            checkBulletBomb(spriteOne, spriteTwo);
                        }
                    }
				}
			}
		}

		//we are dequeuing the opsList and performing operations in serial to avoid mutating the movable arraylists while iterating them above
		while(!CommandCenter.getInstance().getOpsList().isEmpty()){
			CollisionOp cop =  CommandCenter.getInstance().getOpsList().dequeue();
			Movable mov = cop.getMovable();
			CollisionOp.Operation operation = cop.getOperation();

			switch (mov.getTeam()){
				case FOE:
					if (operation == CollisionOp.Operation.ADD){
						CommandCenter.getInstance().getMovFoes().add(mov);
					} else {
						CommandCenter.getInstance().getMovFoes().remove(mov);
					}

					break;
				case FRIEND:
					if (operation == CollisionOp.Operation.ADD){
						CommandCenter.getInstance().getMovFriends().add(mov);
					} else {
						CommandCenter.getInstance().getMovFriends().remove(mov);
					}
					break;

				case FLOATER:
					if (operation == CollisionOp.Operation.ADD){
						CommandCenter.getInstance().getMovFloaters().add(mov);
					} else {
						CommandCenter.getInstance().getMovFloaters().remove(mov);
					}
					break;

				case DEBRIS:
					if (operation == CollisionOp.Operation.ADD){
						CommandCenter.getInstance().getMovDebris().add(mov);
					} else {
						CommandCenter.getInstance().getMovDebris().remove(mov);
					}
					break;
			}
		}
		//a request to the JVM is made every frame to garbage collect, however, the JVM will choose when and how to do this
		System.gc();
	}

	private void checkBump(Sprite movOne, Sprite movTwo) {
		if (!(movOne instanceof Bullet) && !(movOne instanceof Crystal) && !(movOne instanceof Sinistar)) {
			if (!(movTwo instanceof Bullet) && !(movTwo instanceof Crystal) && !(movTwo instanceof Sinistar)) {
				adjustBump(movOne, movTwo);
			}
		}
	}

	private void adjustBump(Sprite movOne, Sprite movTwo){
		Point center1 = movOne.getCenter();
		Point center2 = movTwo.getCenter();

		if (center1.getX() < center2.getX()) {
			center1.setLocation(center1.getX() - BUMP_NUMBER, center1.getY());
			center2.setLocation(center2.getX() + BUMP_NUMBER, center2.getY());
		}
		else {
			center1.setLocation(center1.getX() + BUMP_NUMBER, center1.getY());
			center2.setLocation(center2.getX() - BUMP_NUMBER, center2.getY());
		}

		if (center1.getY() < center2.getY()) {
			center1.setLocation(center1.getX(), center1.getY() - BUMP_NUMBER);
			center2.setLocation(center2.getX(), center2.getY() + BUMP_NUMBER);
		}
		else {
			center1.setLocation(center1.getX(), center1.getY() + BUMP_NUMBER);
			center2.setLocation(center2.getX(), center2.getY() - BUMP_NUMBER);
		}
	}

	private void checkWorkerSinistar(Sprite movOne, Sprite movTwo) {
		if (movOne instanceof WorkerShip && movTwo instanceof Sinistar) {
			if (((WorkerShip) movOne).hasCrystal() && ((Sinistar) movTwo).getNumPieces() < Sinistar.MAX_PIECES) {
				((WorkerShip) movOne).setCrystal(false);
				((Sinistar) movTwo).setNumPieces(((Sinistar) movTwo).getNumPieces() + 1);
				Sound.playSound("piece.wav");
			}
		}
	}

	private void checkCollisionCrystal(Sprite movOne, Sprite movTwo) {
		if (movOne instanceof Crystal) {
			if (movTwo instanceof WorkerShip) {
				if (!((WorkerShip) movTwo).hasCrystal()){
					CommandCenter.getInstance().getOpsList().enqueue(movOne, CollisionOp.Operation.REMOVE);
					((WorkerShip) movTwo).setCrystal(true);
				}
			}
			else if (movTwo instanceof Falcon) {
				CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + movOne.getSpriteScore());
				CommandCenter.getInstance().getOpsList().enqueue(movOne, CollisionOp.Operation.REMOVE);
				((Falcon) movTwo).setNumSinibombs(((Falcon) movTwo).getNumSinibombs() + 1);
				Sound.playSound("crystal.wav");
			}
		}
	}

	private void checkFalconSinistar(Sprite movOne, Sprite movTwo) {
		if (movOne instanceof Falcon && movTwo instanceof Sinistar) {
			if (((Sinistar) movTwo).isAlive()){
				CommandCenter.getInstance().getOpsList().enqueue(movOne, CollisionOp.Operation.REMOVE);
				movTwo.setCenter(new Point(Game.R.nextInt(Game.DIMENSION_SCALAR * Game.DIM.width),
						Game.R.nextInt(Game.DIMENSION_SCALAR * Game.DIM.height)));
				CommandCenter.getInstance().spawnFalcon(false);
			}
		}
	}

	private void checkBulletBomb(Sprite movOne, Sprite movTwo) {
		if ((movOne instanceof Bullet || movOne instanceof Bomb) && !(movTwo instanceof Crystal || movTwo instanceof Bullet)){
			adjustBulletBomb(movOne, movTwo);
		}
	}

	private void adjustBulletBomb(Sprite projectile, Sprite target) {
		if (target instanceof Asteroid || (target instanceof DestroyableFoe && projectile.getTeam() == Movable.Team.FRIEND)) {
			CommandCenter.getInstance().getOpsList().enqueue(projectile, CollisionOp.Operation.REMOVE);
			if (((DestroyableFoe) target).mineCrystal()) {
				CommandCenter.getInstance().getOpsList().enqueue(new Crystal(target.getCenter()), CollisionOp.Operation.ADD);
			}
			if (projectile instanceof Bullet){
				((DestroyableFoe) target).setHealth(((DestroyableFoe) target).getHealth() - 1);
			}
			else if (projectile instanceof Bomb){
				((DestroyableFoe) target).setHealth(0);
			}

			if (((DestroyableFoe) target).getHealth() <= 0){
				CommandCenter.getInstance().getOpsList().enqueue(new Explosion(target), CollisionOp.Operation.ADD);
				if (target instanceof TurretShip){
					if (((TurretShip) target).getLockon_point() != null){
						TurretShip.setLockedOnTurret(false);
					}
				}
				killFoe(target);
				if (projectile.getTeam() == Movable.Team.FRIEND) {
					CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + target.getSpriteScore());
					Sound.playSound("kapow.wav");
				}
			}
		}

		else if (target instanceof Sinistar && projectile instanceof Bomb){
			CommandCenter.getInstance().getOpsList().enqueue(projectile, CollisionOp.Operation.REMOVE);
			((Sinistar) target).setNumPieces(Math.max(((Sinistar) target).getNumPieces() - 1, 0));
			CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + target.getSpriteScore());
			CommandCenter.getInstance().getOpsList().enqueue(new Explosion(projectile), CollisionOp.Operation.ADD);
			Sound.playSound("kapow.wav");
		}

		else if (target instanceof Falcon && projectile.getTeam() == Movable.Team.FOE) {
			CommandCenter.getInstance().getOpsList().enqueue(projectile, CollisionOp.Operation.REMOVE);
			if (!((Falcon) target).getProtected()){
				((Falcon) target).setHealth(((Falcon) target).getHealth() - 1);
				if (((Falcon) target).getHealth() <= 0) {
					CommandCenter.getInstance().getOpsList().enqueue(new Explosion(target), CollisionOp.Operation.ADD);
					Sound.playSound("kapow.wav");
					CommandCenter.getInstance().getOpsList().enqueue(target, CollisionOp.Operation.REMOVE);
					CommandCenter.getInstance().spawnFalcon(false);
				}
			}
		}
	}

    private void fireTurrets() {
		for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
			if (movFoe instanceof TurretShip) {
				if (((TurretShip) movFoe).isFiring()
						&& ((TurretShip) movFoe).getFramesSinceLastBullet()
						>= ((TurretShip) movFoe).getFramesBetweenBullets())
				{
					CommandCenter.getInstance().getOpsList().enqueue(new Bullet(
							(TurretShip) movFoe), CollisionOp.Operation.ADD);
					((TurretShip) movFoe).setFramesSinceLastBullet(0);
				}
				else {
					((TurretShip) movFoe).setFramesSinceLastBullet(
							((TurretShip) movFoe).getFramesSinceLastBullet() + 1);
				}
			}
		}
	}

	private void fireFalcon(){
		if (fireBullets && framesSinceBullet >= FRAMES_BETWEEN_BULLETS && CommandCenter.getInstance().isPlaying()) {
			CommandCenter.getInstance().getOpsList().enqueue(new Bullet(
					CommandCenter.getInstance().getFalcon()), CollisionOp.Operation.ADD);
			Sound.playSound("laser.wav");
			framesSinceBullet = 0;
		}
		else {
			framesSinceBullet += 1;
		}
	}

	private void killFoe(Sprite movFoe) {
		
		if (movFoe instanceof Asteroid){
            CommandCenter.getInstance().setNumAsteroids(CommandCenter.getInstance().getNumAsteroids() - 1);
		}
		else if (movFoe instanceof WorkerShip){
            CommandCenter.getInstance().setNumWorkers(CommandCenter.getInstance().getNumWorkers() - 1);
        }

        else if (movFoe instanceof TurretShip){
            CommandCenter.getInstance().setNumTurrets(CommandCenter.getInstance().getNumTurrets() - 1);
        }

		//remove the original DestroyableFoe
		CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);
	}

	//some methods for timing events in the game,
	//such as the appearance of UFOs, floaters (power-ups), etc. 
	public void tick() {
		if (nTick == Integer.MAX_VALUE)
			nTick = 0;
		else
			nTick++;
	}

	public int getTick() {
		return nTick;
	}

	// Called when user presses 's'
	private void startGame() {
		CommandCenter.getInstance().clearAll();
		CommandCenter.getInstance().initGame();
		CommandCenter.getInstance().setPlaying(true);
		CommandCenter.getInstance().setPaused(false);
	}

	//this method spawns new asteroids
	private void spawnAsteroids() {
        CommandCenter.getInstance().spawnAsteroids();
	}
	private void spawnTurrets() {CommandCenter.getInstance().spawnTurrets();}
    private void spawnWorkers() {CommandCenter.getInstance().spawnWorkers();}

	private void dropSinibomb() {
        Falcon falcon = CommandCenter.getInstance().getFalcon();
        if (falcon.getNumSinibombs() >= 1) {
            CommandCenter.getInstance().getOpsList().enqueue(new Bomb(falcon), CollisionOp.Operation.ADD);
            falcon.setNumSinibombs(falcon.getNumSinibombs() - 1);
            Sound.playSound("laser.wav");
        }
    }

	// Varargs for stopping looping-music-clips
	private static void stopLoopingSounds(Clip... clpClips) {
		for (Clip clp : clpClips) {
			clp.stop();
		}
	}

	public boolean isFireBullets() {
		return fireBullets;
	}

	public void setFireBullets(boolean fireBullets) {
		this.fireBullets = fireBullets;
	}

	// ===============================================
	// KEYLISTENER METHODS
	// ===============================================

	@Override
	public void keyPressed(KeyEvent e) {
		Falcon fal = CommandCenter.getInstance().getFalcon();
		int nKey = e.getKeyCode();
		//System.out.println(nKey);

		if (nKey == START && !CommandCenter.getInstance().isPlaying())
			startGame();

		if (CommandCenter.getInstance().isPlaying()) {

			switch (nKey) {
			case PAUSE:
				CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
				break;
			case QUIT:
				System.exit(0);
				break;
			case UP:
				fal.thrustOn();
				if (!CommandCenter.getInstance().isPaused())
					clpThrust.loop(Clip.LOOP_CONTINUOUSLY);
				break;
			case LEFT:
				fal.rotateLeft();
				break;
			case RIGHT:
				fal.rotateRight();
				break;

            case FIRE:
                fireBullets = true;
                break;

			default:
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Falcon fal = CommandCenter.getInstance().getFalcon();
		int nKey = e.getKeyCode();

		if (CommandCenter.getInstance().isPlaying()) {
			switch (nKey) {
			case FIRE:
                fireBullets = false;
                framesSinceBullet = 0;
                CommandCenter.getInstance().getOpsList().enqueue(new Bullet(fal), CollisionOp.Operation.ADD);
                Sound.playSound("laser.wav");
				break;
				
			case SPECIAL:
			    dropSinibomb();
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
				
			default:
				break;
			}
		}
	}

	@Override
	// Just need it b/c of KeyListener implementation
	public void keyTyped(KeyEvent e) {
	}

}