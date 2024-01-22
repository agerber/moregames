package edu.uchicago.gerber.g1941.mvc.controller;

import edu.uchicago.gerber.g1941.mvc.model.*;
import edu.uchicago.gerber.g1941.mvc.model.airplane.*;
import edu.uchicago.gerber.g1941.mvc.model.buff.FloaterBullet;
import edu.uchicago.gerber.g1941.mvc.model.buff.FloaterHp;
import edu.uchicago.gerber.g1941.mvc.model.buff.FloaterMissile;
import edu.uchicago.gerber.g1941.mvc.model.weapon.*;
import edu.uchicago.gerber.g1941.mvc.model.weapon.Bullet;
import edu.uchicago.gerber.g1941.mvc.view.GamePanel;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;


// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    public static final Dimension DIM = new Dimension(500, 900); //the dimension of the game.
    private final GamePanel gamePanel;
    public static final Random R = new Random(); //this is used throughout many classes.
    public final static int ANIMATION_DELAY = 40; // milliseconds between frames
    private final Thread animationThread;

    //key-codes
    private static final int
            PAUSE = 80,
            QUIT = 81,
            LEFT = 37,
            RIGHT = 39,
            UP = 38,
            DOWN = 40,
            START = 83,
            MUTE = 77,
            MAP1 = 49,
            MAP2 = 50,
            MAP3 = 51,
            BACK_TO_MAP = 66,
            LUNCH_MISSILE = 70;

    private final Clip soundThrust;
    private final Clip soundBackground;


    public Game() {
        gamePanel = new GamePanel(DIM);
        gamePanel.addKeyListener(this); //Game object implements KeyListener
        soundThrust = Sound.clipForLoopFactory("whitenoise.wav");
        soundBackground = Sound.clipForLoopFactory("music-background.wav");
        //fire up the animation thread
        animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
        animationThread.setDaemon(true);
        animationThread.start();
    }


    public static void main(String[] args) {
        //typical Swing application start; we pass EventQueue a Runnable object.
        EventQueue.invokeLater(Game::new);
    }


    // Game implements runnable, and must have run method
    // Manage the occurrence of different Aircrafts in different MAPs and different levels
    @Override
    public void run() {
        // lower animation thread's priority, thereby yielding to the 'Event Dispatch Thread' or EDT
        // thread which listens to keystrokes
        animationThread.setPriority(Thread.MIN_PRIORITY);
        // and get the current time
        long startTime = System.currentTimeMillis();
        // this thread animates the scene
        while (Thread.currentThread() == animationThread) {
            //this call will cause all movables to move() and draw() themselves every ~40ms
            // see GamePanel class for details
            gamePanel.update(gamePanel.getGraphics());
            checkCollisions();
            // W.
            if (checkShouldUpdateLevel()) {
                updateLevel();
            }
            // W.
            int level = CommandCenter.getInstance().getLevel();
            switch (CommandCenter.getInstance().getCurMap()) {
                case 1:
                    CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(2);
                    switch (level) {
                        case 1:
                            generateAircraftEnemyA(100); // generate enemy aircraft A per 100 frames once, i.e. per 4 sec
                            generateAircraftEnemyB(180);
                            break;
                        case 2:
                            generateAircraftEnemyAGroup(100);
                            generateAircraftEnemyB(180);
                            break;
                        case 3:
                            generateAircraftEnemyAGroup(130);
                            generateAircraftEnemyB(180);
                            generateAircraftEnemyC(60);
                            spawnFloaterBullet(130);
                            break;
                        case 4:
                            generateAircraftEnemyAGroup(130);
                            generateAircraftEnemyB(180);
                            generateAircraftEnemyC(200);
                            generateAircraftEnemyD(500);
                            spawnFloaterBullet(300);
                            spawnFloaterHp(300);
                            break;
                        default:
                            if (!CommandCenter.getInstance().isBossOccur() && noMoreAircraftEnemy()) {
                                generateAircraftEnemyE();
                                CommandCenter.getInstance().setBossOccur(true);
                            }
                            break;
                    }
                    break;
                case 2:
                    switch (level) {
                        case 1:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(2);
                            generateAircraftEnemyA(50);
                            generateAircraftEnemyF(120);
                            break;
                        case 2:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(2);
                            generateAircraftEnemyAGroup(70);
                            generateAircraftEnemyB(120);
                            generateAircraftEnemyD(500);
                            spawnFloaterBullet(230);
                            break;
                        case 3:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(3);
                            generateAircraftEnemyAGroup(130);
                            generateAircraftEnemyC(50);
                            generateAircraftEnemyF(220);
                            generateAircraftShip(330);
                            spawnFloaterBullet(230);
                            spawnFloaterHp(200);
                            spawnFloaterMissile(230);
                            break;
                        case 4:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(3);
                            generateAircraftEnemyAGroup(130);
                            generateAircraftEnemyB(50);
                            generateAircraftEnemyC(220);
                            generateAircraftEnemyF(160);
                            generateAircraftShip(330);
                            spawnFloaterBullet(230);
                            spawnFloaterHp(200);
                            spawnFloaterMissile(230);
                            break;
                        default:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(3);
                            if (!CommandCenter.getInstance().isBossOccur() && noMoreAircraftEnemy()) {
                                generateAircraftEnemyE();
                                CommandCenter.getInstance().setBossOccur(true);
                            }
                            break;
                    }
                    break;
                case 3:
                    switch (level) {
                        case 1:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(2);
                            generateAircraftEnemyA(50);
                            generateAircraftEnemyF(120);
                            spawnFloaterMissile(230);
                            generateAircraftShip(330);
                            break;
                        case 2:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(2);
                            generateAircraftEnemyAGroup(70);
                            generateAircraftEnemyB(120);
                            generateAircraftEnemyD(500);
                            spawnFloaterBullet(230);
                            spawnFloaterMissile(230);
                            generateAircraftShip(330);
                            break;
                        case 3:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(3);
                            generateAircraftEnemyAGroup(130);
                            generateAircraftEnemyC(50);
                            generateAircraftEnemyF(220);
                            generateAircraftShip(230);
                            spawnFloaterBullet(230);
                            spawnFloaterHp(200);
                            spawnFloaterMissile(230);
                            break;
                        case 4:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(3);
                            generateAircraftEnemyAGroup(130);
                            generateAircraftEnemyB(50);
                            generateAircraftEnemyC(220);
                            generateAircraftEnemyF(160);
                            generateAircraftShip(230);
                            spawnFloaterBullet(230);
                            spawnFloaterHp(200);
                            spawnFloaterMissile(230);
                            break;
                        default:
                            CommandCenter.getInstance().getAircraftFriend().setMaxBulletLevel(3);
                            if (!CommandCenter.getInstance().isBossOccur() && noMoreAircraftEnemy()) {
                                generateAircraftEnemyE();
                                CommandCenter.getInstance().setBossOccur(true);
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }
            //this method will execute add() and remove() callbacks on Movable objects
            processGameOpsQueue();
            //keep track of the frame for development purposes
            CommandCenter.getInstance().incrementFrame();
            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANIMATION_DELAY long.  If processing (update)
                // between frames takes longer than ANIMATION_DELAY, then the difference between startTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                startTime += ANIMATION_DELAY;
                Thread.sleep(Math.max(0,
                        startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run


    // Check Collisions
    // Do not Check Collisions between bullets (bullet will not destroy each other even though they are in different groups)
    // If bullet hit aircraft, apply method aircraftHitByBullet()
    private void checkCollisions() {
        //This has order-of-growth of O(FOES * FRIENDS)
        Point pntFriendCenter, pntFoeCenter;
        int radFriend, radFoe;
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
                // W.
                // do Not check collisions between bullets from different sides
                if (movFriend instanceof Bullet && movFoe instanceof Bullet) {
                    continue;
                }
                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                radFriend = movFriend.getRadius();
                radFoe = movFoe.getRadius();
                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {
                    // W.
                    if (movFriend instanceof Aircraft && movFoe instanceof Bullet) {
                        aircraftHitByBullet((Aircraft) movFriend, (Bullet) movFoe);
                    } else if (movFriend instanceof Bullet && movFoe instanceof Aircraft){
                        aircraftHitByBullet((Aircraft) movFoe, (Bullet) movFriend);
                    } else {
                        CommandCenter.getInstance().getAircraftFriend().setHp(0);
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                    }
                }
            }//end inner for
        }//end outer for
        //check for collisions between falcon and floaters. Order of growth of O(FLOATERS)
        Point pntFalCenter = CommandCenter.getInstance().getAircraftFriend().getCenter();
        int radFalcon = CommandCenter.getInstance().getAircraftFriend().getRadius();
        Point pntFloaterCenter;
        int radFloater;
        for (Movable movFloater : CommandCenter.getInstance().getMovFloaters()) {
            pntFloaterCenter = movFloater.getCenter();
            radFloater = movFloater.getRadius();
            //detect collision
            if (pntFalCenter.distance(pntFloaterCenter) < (radFalcon + radFloater)) {
                //enqueue the floater
                CommandCenter.getInstance().getOpsQueue().enqueue(movFloater, GameOp.Action.REMOVE);
                // W.
                // enhance bullet
                if (movFloater instanceof FloaterBullet) {
                    bulletLevelUp();
                } else if (movFloater instanceof FloaterMissile) {
                    // Don't forget to change 3 to a variable from FloaterMissile
                    CommandCenter.getInstance().getAircraftFriend().addMissile(2);
                } else if (movFloater instanceof FloaterHp) {
                    CommandCenter.getInstance().getAircraftFriend().addHp(FloaterHp.HP_INCREMENT);
                }
                // W.
                // increment number of missile
            }//end if
        }//end for
    }//end meth


    // Increase the bullet level of player's Aircraft
    private void bulletLevelUp() {
        int bulletLevel = CommandCenter.getInstance().getAircraftFriend().getBulletLevel();
        bulletLevel += 1;
        CommandCenter.getInstance().getAircraftFriend().setBulletLevel(bulletLevel);
    }


    // Call method hit() in Class Aircraft
    // Aircraft hit by bullet
    private void aircraftHitByBullet(Aircraft aircraft, Bullet bullet) {
        int fireForce = bullet.getFireForce();
        CommandCenter.getInstance().getOpsQueue().enqueue(bullet, GameOp.Action.REMOVE);
        boolean alive = aircraft.hit(fireForce);
        if (!alive) {
            CommandCenter.getInstance().getOpsQueue().enqueue(aircraft, GameOp.Action.REMOVE);
        }
    }


    //This method adds and removes movables to/from their respective linked-lists.
    private void processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {

            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();

            //given team, determine which linked-list this object will be added-to or removed-from
            LinkedList<Movable> list;
            Movable mov = gameOp.getMovable();
            switch (mov.getTeam()) {
                case FOE:
                    list = CommandCenter.getInstance().getMovFoes();
                    break;
                case FRIEND:
                    list = CommandCenter.getInstance().getMovFriends();
                    break;
                case FLOATER:
                    list = CommandCenter.getInstance().getMovFloaters();
                    break;
                case DEBRIS:
                default:
                    list = CommandCenter.getInstance().getMovDebris();
            }

            //pass the appropriate linked-list from above
            //this block will execute the add() or remove() callbacks in the Movable models.
            GameOp.Action action = gameOp.getAction();
            if (action == GameOp.Action.ADD)
                mov.add(list);
            else //REMOVE
                mov.remove(list);

        }//end while
    }


    // Spawn bullet Floater
    // Which will enhance the bullet level of player's Aircraft
    // Only spawn bullet floater if the current bullet level is less than the maximum bullet level
    private void spawnFloaterBullet(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                CommandCenter.getInstance().getAircraftFriend().getBulletLevel() <
                        CommandCenter.getInstance().getAircraftFriend().getMaxBulletLevel() &&
                !CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new FloaterBullet(), GameOp.Action.ADD);
        }
    }


    // Spawn Missile Floater
    // Add more Missile for player's Aircraft
    // Only spawn missile floater if the current remaining amount of missile if less than the max amount of missiles
    private void spawnFloaterMissile(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                CommandCenter.getInstance().getAircraftFriend().getRemainNumMissile() < AircraftFriend.MAX_NUM_MISSILE &&
                !CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new FloaterMissile(), GameOp.Action.ADD);
        }
    }


    // Spawn HP Floater
    // Add more HP for player's Aircraft
    // Only spawn missile floater if the current remaining amount of missile if less than the max amount of missiles
    private void spawnFloaterHp(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                CommandCenter.getInstance().getAircraftFriend().getHp() <
                        CommandCenter.getInstance().getAircraftFriend().getTotalHp()&&
                !CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new FloaterHp(), GameOp.Action.ADD);
        }
    }


    // Check whether there are any enemy aircrafts on the screen
    // Do NOT check ship
    // This method make sure more enemy aircrafts on the screen before final BOSS occur
    private boolean noMoreAircraftEnemy() {
        boolean aircraftEnemyFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Aircraft) {
                aircraftEnemyFree = false;
                break;
            }
        }
        return aircraftEnemyFree;
    }


    // Update the level within MAP
    private void updateLevel() {
        int level = CommandCenter.getInstance().getLevel();
        level ++;
        CommandCenter.getInstance().setLevel(level);
    }


    // Check whether update level
    public boolean checkShouldUpdateLevel() {
        return CommandCenter.getInstance().getScore() >= CommandCenter.getInstance().getLevel() *
                CommandCenter.getInstance().getLevel() * 800L;
    }


    // Generate enemy Aircraft A
    public void generateAircraftEnemyA(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            int centerX = randomPosValue(Game.DIM.width - AircraftEnemyA.MIN_RADIUS * 2) + AircraftEnemyA.MIN_RADIUS;
            Point center = new Point(centerX, 0);
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyA(center), GameOp.Action.ADD);
        }
    }


    // Generate enemy Aircraft B
    public void generateAircraftEnemyB(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyB(), GameOp.Action.ADD);
        }
    }


    // Generate enemy Aircraft C
    public void generateAircraftEnemyC(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyC(), GameOp.Action.ADD);
        }
    }


    // Generate enemy Aircraft D
    public void generateAircraftEnemyD(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyD(), GameOp.Action.ADD);
        }
    }


    // Generate enemy Aircraft E
    // The final BOSS
    public void generateAircraftEnemyE() {
        if (!CommandCenter.getInstance().isPaused()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyE(), GameOp.Action.ADD);
        }
    }


    // Generate Ship
    public void generateAircraftShip(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            AircraftShip ship = new AircraftShip();
            AircraftShipGun gun = new AircraftShipGun(ship);
            CommandCenter.getInstance().getOpsQueue().enqueue(ship, GameOp.Action.ADD);
            CommandCenter.getInstance().getOpsQueue().enqueue(gun, GameOp.Action.ADD);
        }
    }


    // Generate enemy Aircraft F
    public void generateAircraftEnemyF(int frequency) {
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            Point centerLeft = new Point(Game.DIM.width / 5, 0);
            Point centerRight = new Point(Game.DIM.width / 5 * 4, 0);
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyF(centerLeft), GameOp.Action.ADD);
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyF(centerRight), GameOp.Action.ADD);
        }
    }


    // Generate enemy Aircraft A group
    public void generateAircraftEnemyAGroup(int frequency) {
        int gap = 5;
        int distance = AircraftEnemyA.MIN_RADIUS * 3 + gap;
        if (CommandCenter.getInstance().getFrame() % frequency == 0 &&
                !CommandCenter.getInstance().isPaused()) {
            int centerMiddleX = randomPosValue(Game.DIM.width - distance * 2) + distance;
            CommandCenter.getInstance().setAircraftEnemyAGroupCenterX(centerMiddleX);
            Point centerLeft =
                    new Point(
                            CommandCenter.getInstance().getAircraftEnemyAGroupCenterX() - AircraftEnemyA.MIN_RADIUS * 2 - gap,
                            0);
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyA(centerLeft), GameOp.Action.ADD);
        }
        if (CommandCenter.getInstance().getFrame() % frequency == 3) {
            Point centerMiddle =
                    new Point(
                            CommandCenter.getInstance().getAircraftEnemyAGroupCenterX(),
                            0);
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyA(centerMiddle), GameOp.Action.ADD);
        }
        if (CommandCenter.getInstance().getFrame() % frequency == 6) {
            Point centerRight =
                    new Point(
                            CommandCenter.getInstance().getAircraftEnemyAGroupCenterX() + AircraftEnemyA.MIN_RADIUS * 2 + gap,
                            0);
            CommandCenter.getInstance().getOpsQueue().enqueue(new AircraftEnemyA(centerRight), GameOp.Action.ADD);
        }
    }


    public int randomPosValue(int seed) {
        return Game.R.nextInt(seed);
    }


    // Varargs for stopping looping-music-clips
    private static void stopLoopingSounds(Clip... clpClips) {
        Arrays.stream(clpClips).forEach(clip -> clip.stop());
    }


    // Only check some keys at special condition
    // Only check MAP1 (key 1), MAP2, MAP3 if the player is on the Menu Screen (Select which MAP to play)
    // Only check key B if the player failed or passed a MAP
    @Override
    public void keyPressed(KeyEvent e) {
        AircraftFriend aircraftFriend = CommandCenter.getInstance().getAircraftFriend();
        int keyCode = e.getKeyCode();
        if (keyCode == START &&
                CommandCenter.getInstance().isGameOver() &&
                !CommandCenter.getInstance().isShowMenu() &&
                !CommandCenter.getInstance().isPassed() &&
                !CommandCenter.getInstance().isFailed()) {
            CommandCenter.getInstance().setShowMenu(true);
            return;
        }
        if (CommandCenter.getInstance().isGameOver() &&
                CommandCenter.getInstance().isShowMenu() &&
                !CommandCenter.getInstance().isPassed() &&
                !CommandCenter.getInstance().isFailed()) {
            if (keyCode == MAP1) {
                CommandCenter.getInstance().setCurMap(1);
                CommandCenter.getInstance().initGame();
                return;
            } else if (keyCode == MAP2 && !CommandCenter.getInstance().getMapLocked().get(2)) {
                CommandCenter.getInstance().setCurMap(2);
                CommandCenter.getInstance().initGame();
                return;
            } else if (keyCode == MAP3 && !CommandCenter.getInstance().getMapLocked().get(3)) {
                CommandCenter.getInstance().setCurMap(3);
                CommandCenter.getInstance().initGame();
                return;
            }
        }
        if (keyCode == BACK_TO_MAP &&
                CommandCenter.getInstance().isGameOver() &&
                !CommandCenter.getInstance().isShowMenu() &&
                (CommandCenter.getInstance().isPassed() || CommandCenter.getInstance().isFailed())) {
            CommandCenter.getInstance().setShowMenu(true);
            CommandCenter.getInstance().setPassed(false);
            CommandCenter.getInstance().setFailed(false);
            CommandCenter.getInstance().setBossOccur(false);
            CommandCenter.getInstance().setBossDead(false);
            return;
        }
        switch (keyCode) {
            case PAUSE:
                CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                if (CommandCenter.getInstance().isPaused()) stopLoopingSounds(soundBackground, soundThrust);
                break;
            case QUIT:
                System.exit(0);
                break;
            case UP:
                aircraftFriend.setTurnState(AircraftFriend.TurnState.UP);
                break;
            case LEFT:
                aircraftFriend.setTurnState(AircraftFriend.TurnState.LEFT);
                break;
            case RIGHT:
                aircraftFriend.setTurnState(AircraftFriend.TurnState.RIGHT);
                break;
            case DOWN:
                aircraftFriend.setTurnState(AircraftFriend.TurnState.DOWN);
                break;
            default:
                break;
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
        AircraftFriend aircraftFriend = CommandCenter.getInstance().getAircraftFriend();
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            //releasing either the LEFT or RIGHT arrow key will set the TurnState to IDLE
            case LEFT:
            case RIGHT:
            case DOWN:
            case UP:
                aircraftFriend.setTurnState(AircraftFriend.TurnState.IDLE);
                break;
            case MUTE:
                CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());
                if (!CommandCenter.getInstance().isMuted()) {
                    stopLoopingSounds(soundBackground);
                } else {
                    soundBackground.loop(Clip.LOOP_CONTINUOUSLY);
                }
                break;
            case LUNCH_MISSILE:
                if (CommandCenter.getInstance().getAircraftFriend().getRemainNumMissile() == 0 ||
                        CommandCenter.getInstance().isPaused()) {
                    break;
                }
                boolean hasEnemy = false;
                for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
                    if (movFoe instanceof Aircraft) {
                        hasEnemy = true;
                        break;
                    }
                }
                if (hasEnemy) {
                    CommandCenter.getInstance().getOpsQueue().enqueue(new BulletMissile(), GameOp.Action.ADD);
                    CommandCenter.getInstance().getAircraftFriend().setRemainNumMissile(
                            CommandCenter.getInstance().getAircraftFriend().getRemainNumMissile() - 1);
                }
                break;
            default:
                break;
        }

    }


    @Override
    // does nothing, but we need it b/c of KeyListener contract
    public void keyTyped(KeyEvent e) {
    }



}


