package edu.uchicago.gerber.raster_asteroids.controller;

import edu.uchicago.gerber.raster_asteroids.model.*;
import edu.uchicago.gerber.raster_asteroids.view.GamePanel;

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
    private final GamePanel panel;
    //this is used throughout many classes.
    public static final Random R = new Random();

    public final static int ANIMATION_DELAY = 40; // milliseconds between screen
    // updates (animation)

    public final static int FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY;

    private final Thread animationThread;


    private final int PAUSE = 80, // p key
            QUIT = 81, // q key
            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow
            UP = 38, // thrust; up arrow
            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77, // m-key mute

    // for possible future use
    // HYPER = 68, 					// D key
    ALIEN = 65;                // A key
    // SPECIAL = 70; 					// fire special weapon;  F key

    private final Clip soundThrust;
    private final Clip soundBackground;

    //spawn every 30 seconds
    private static final int SPAWN_NEW_WALL_FLOATER = FRAMES_PER_SECOND * 40;
    private static final int SPAWN_SHIELD_FLOATER = FRAMES_PER_SECOND * 25;




    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {

        panel = new GamePanel(DIM);
        panel.addKeyListener(this); //Game object implements KeyListener
        soundThrust = Sound.clipForLoopFactory("whitenoise.wav");
        soundBackground = Sound.clipForLoopFactory("music-background.wav");

        //fire up the animation thread
        animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
        animationThread.start();


    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String[] args) {
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
        long startTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == animationThread) {


            panel.update(panel.getGraphics()); // see GamePanel class
            checkCollisions();
            checkNewLevel();
            checkFloaters();
            CommandCenter.getInstance().incrementFrame();

            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update)
                // between frames takes longer than ANI_DELAY, then the difference between startTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                startTime += ANIMATION_DELAY;

                Thread.sleep(Math.max(0,
                        startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run

    private void checkFloaters() {
        spawnNewWallFloater();
        spawnShieldFloater();
    }


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
                    if (!movFriend.isProtected()) {
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                    }
                    //remove the foe
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);

                    if (movFoe instanceof  Brick){
                        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 1000);
                        Sound.playSound("rock.wav");
                    } else {
                        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10);
                        Sound.playSound("kapow.wav");
                    }
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

                Class<? extends Movable> clazz = movFloater.getClass();
                switch (clazz.getSimpleName()){
                    case "ShieldFloater":
                        Sound.playSound("shieldup.wav");
                        CommandCenter.getInstance().getFalcon().setSpawn(Falcon.MAX_SHIELD);
                     break;
                    case "NewWallFloater":
                        Sound.playSound("wall.wav");
                        buildWall();
                        break;
                }
                CommandCenter.getInstance().getOpsQueue().enqueue(movFloater, GameOp.Action.REMOVE);


            }//end if
        }//end for

        processGameOpsQueue();

    }//end meth




    private void processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {
            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();
            Movable mov = gameOp.getMovable();
            GameOp.Action action = gameOp.getAction();

            switch (mov.getTeam()) {
                case FOE:
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovFoes().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovFoes().remove(mov);
                        if (mov instanceof Asteroid)
                            spawnSmallerAsteroidsOrDebris((Asteroid) mov);
                    }

                    break;
                case FRIEND:
                    if (action == GameOp.Action.ADD) {
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
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovFloaters().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovFloaters().remove(mov);
                    }
                    break;

                case DEBRIS:
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovDebris().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovDebris().remove(mov);
                    }
                    break;


            }

        }
    }
    private boolean hasNoBricks() {
        //if there are no more Bricks on the screen
        boolean bricksFree = true;
        for (Movable movFriend : CommandCenter.getInstance().getMovFoes()) {
            if (movFriend instanceof Brick) {
                bricksFree = false;
                break;
            }
        }
        return bricksFree;
    }

    //shows how to add walls or rectangular elements one
    //brick at a time
    private void buildWall() {
        final int BRICK_SIZE = Game.DIM.width / 30, ROWS = 20, COLS = 2, X_OFFSET = BRICK_SIZE * 5, Y_OFFSET = 50;

        for (int nRow = 0; nRow < ROWS; nRow++) {
            for (int nCol = 0; nCol < COLS; nCol++) {
                CommandCenter.getInstance().getOpsQueue().enqueue(
                        new Brick(
                                new Point(nRow * BRICK_SIZE + X_OFFSET, nCol * BRICK_SIZE + Y_OFFSET),
                                BRICK_SIZE),
                        GameOp.Action.ADD);

            }
        }
    }


    private void spawnNewWallFloater() {

        if (CommandCenter.getInstance().getFrame() % SPAWN_NEW_WALL_FLOATER == 0 && hasNoBricks()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new NewWallFloater(), GameOp.Action.ADD);
        }
    }

    private void spawnShieldFloater() {

        if (CommandCenter.getInstance().getFrame() % SPAWN_SHIELD_FLOATER == 0 ) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new ShieldFloater(), GameOp.Action.ADD);
        }
    }


    //this method spawns new Large (0) Asteroids
    private void spawnBigAsteroids(int nNum) {
        while (nNum-- > 0) {
            //Asteroids with size of zero are big
            CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(0), GameOp.Action.ADD);

        }
    }

    private void spawnSmallerAsteroidsOrDebris(Asteroid originalAsteroid) {

        int nSize = originalAsteroid.getSize();
        //small asteroids
        if (nSize > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalAsteroid), GameOp.Action.ADD);
        }
        //med and large
        else {
            //for large (0) and medium (1) sized Asteroids only, spawn 2 or 3 smaller asteroids respectively
            nSize += 2;
            while (nSize-- > 0) {
                CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(originalAsteroid), GameOp.Action.ADD);
            }
        }

    }


    private boolean isLevelClear() {
        //if there are no more Asteroids on the screen
        boolean asteroidFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Asteroid) {
                asteroidFree = false;
                break;
            }
        }
        return asteroidFree;
    }

    private void checkNewLevel() {

        if (isLevelClear()) {
            //more asteroids at each level to increase difficulty
            CommandCenter.getInstance().setLevel(CommandCenter.getInstance().getLevel() + 1);
            spawnBigAsteroids(CommandCenter.getInstance().getLevel());
            //setFade e.g. protect the falcon so that player has time to avoid newly spawned asteroids.
            CommandCenter.getInstance().getFalcon().setSpawn(Falcon.INITIAL_SPAWN_TIME);

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
                        stopLoopingSounds(soundBackground, soundThrust);

                    break;
                case QUIT:
                    System.exit(0);
                    break;
                case UP:
                    fal.setThrusting(true);
                    if (!CommandCenter.getInstance().isPaused() && !CommandCenter.getInstance().isGameOver())
                        soundThrust.loop(Clip.LOOP_CONTINUOUSLY);
                    break;
                case LEFT:
                    fal.setTurnState(Falcon.TurnState.LEFT);
                    break;
                case RIGHT:
                    fal.setTurnState(Falcon.TurnState.RIGHT);
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
                case RIGHT:
                    fal.setTurnState(Falcon.TurnState.IDLE);
                    break;
                case UP:
                    fal.setThrusting(false);
                    soundThrust.stop();
                    break;

                case MUTE:
                    CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());

                    if (!CommandCenter.getInstance().isMuted()) {
                        stopLoopingSounds(soundBackground);
                    } else {
                        soundBackground.loop(Clip.LOOP_CONTINUOUSLY);
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


