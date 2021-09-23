package mario.mvc.controller;

import mario.mvc.model.*;
import mario.mvc.view.GamePanel;
import mario.sounds.Sound;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import static java.awt.event.KeyEvent.VK_N;

/**
 *
 *  Date        Author      Description
 *  ----        ------      -----------
 *  06/05/16    Moison      Extended code to implement first two levels of Mario
 */
public class Game implements Runnable, KeyListener {

	// FIELDS
	public static final Dimension DIM = new Dimension(1020, 780); //the dimension of the game.
	private GamePanel gmpPanel;
	public static Random R = new Random();
	public final static int ANI_DELAY = 45; // milliseconds between screen
											// updates (animation)

    public static final int GAME_MAX_LEVEL = 2; // Define max number of levels to determine if game is over.
	private Thread thrAnim;
	private int nTick = 0;
    private final int FOE_LEVEL_MULTIPLIER = 4; //Number of foes active based on level.
    private final int GOOMBA_INTRO_INTERVAL = 4500; // Milliseconds
    private final int KOOPA_INTRO_INTERVAL = 12000; // Milliseconds
    private final int PARATROOPA_INTRO_INTERVAL = 12000; // Milliseconds

    private long lStartTime = System.currentTimeMillis();

	private boolean bMuted = false;
	private final int PAUSE = 80, // p key
			QUIT = 81, // q key
			LEFT = 37, // rotate left; left arrow
			RIGHT = 39, // rotate right; right arrow
			UP = 38, // thrust; up arrow
			START = 83, // s key
			FIRE = 32, // space key
			MUTE = 77; // m-key mute

	private Clip clpMusicBackground;

	public Game() {

		gmpPanel = new GamePanel(DIM);
		gmpPanel.addKeyListener(this);
		clpMusicBackground = Sound.clipForLoopFactory("Mario_background.wav");
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							Game game = new Game();
							game.fireUpAnimThread();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void fireUpAnimThread() {
		if (thrAnim == null) {
			thrAnim = new Thread(this);
			thrAnim.start();
		}
	}


	public void run() {

		thrAnim.setPriority(Thread.MIN_PRIORITY);

		while (Thread.currentThread() == thrAnim) {

			tick();

            // Check items in order of priority
            checkGameOver();
            checkLevelClear();
            checkHorizontalCollision();
            checkExpiringCoins();
            checkGravity();
			gmpPanel.update(gmpPanel.getGraphics());
            checkCollision();
            checkClouds();
            checkFoes();
            addFoes();
            processQueue();
			try {
				lStartTime += ANI_DELAY;
				Thread.sleep(Math.max(0,lStartTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				continue;
			}
		} // end while
	} // end run

    // Check if level is clear after the completion of fireworks
    private void checkLevelClear() {
        if (CommandCenter.getInstance().isLevelClear()) {
            CommandCenter.getInstance().getOpsList().enqueue(new Firework(Game.R.nextInt(Game.DIM.width),Game.R.nextInt(Game.DIM.height) - 300),
                                                            CollisionOp.Operation.ADD);
            for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
                if (movFriend instanceof Firework) {
                    if (((Firework) movFriend).getExpiryCounter() > 0) {
                        ((Firework) movFriend).decrExpiryCounter();
                    } else {
                        CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                    }
                }
            }
        } else  {
            // Remove any left over fireworks from previous level
            for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
                if (movFriend instanceof Firework) {
                    CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                }
            }
        }
        if (CommandCenter.getInstance().isLevelClear() && CommandCenter.getInstance().getFlag().getCenter().y > 650) {
            setNextLevel();
        }
    }

    // Check if Game is over and stop background music
    private void checkGameOver() {
        if (CommandCenter.getInstance().isGameOver()) {
            stopLoopingSounds(clpMusicBackground);
        }
    }

    // Method to draw background based on current level of game.
	private void drawBackGround() {

        Ground ground;

        switch (CommandCenter.getInstance().getLevel()) {
            case 1:
                // Add ground blocks
                ground = new Ground(0);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);
                CommandCenter.getInstance().setGroundFirst(ground);

                ground = new Ground(510);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);

                ground = new Ground(1020);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);

                ground = new Ground(1530);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);

                ground = new Ground(2040);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);

                CommandCenter.getInstance().setGroundLast(ground);

                // Add pipe for entry
                CommandCenter.getInstance().getOpsList().enqueue(new Pipe(200, 648), CollisionOp.Operation.ADD);

                // Add bushes
                CommandCenter.getInstance().getOpsList().enqueue(new Bush2(300, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush1(650, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush1(800, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush1(1050, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush2(1220, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush1(1600, 680), CollisionOp.Operation.ADD);


                // Add hills
                CommandCenter.getInstance().getOpsList().enqueue(new Hill1(50, 674), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Hill2(750, 642), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Hill1(1380, 674), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Hill2(1750, 642), CollisionOp.Operation.ADD);

                // Introduce cloud1 from right but from 20 pixels outside the screen
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud1(DIM.width + 20,200), CollisionOp.Operation.ADD);

                // Introduce cloud2 from 1/3 pixels within the frame
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud2(Game.DIM.width/3, 150), CollisionOp.Operation.ADD);

                // Introduce cloud from the middle of the frame
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud3(Game.DIM.width/2, 100), CollisionOp.Operation.ADD);

                break;
            case 2:
                // Add ground blocks
                ground = new Ground(0);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);
                CommandCenter.getInstance().setGroundFirst(ground);

                ground = new Ground(510);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);

                ground = new Ground(1020);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);

                ground = new Ground(1530);
                CommandCenter.getInstance().getOpsList().enqueue(ground, CollisionOp.Operation.ADD);
                CommandCenter.getInstance().setGroundLast(ground);


                // Add pipe for entry
                CommandCenter.getInstance().getOpsList().enqueue(new Pipe(200, 648), CollisionOp.Operation.ADD);

                // Add bushes
                CommandCenter.getInstance().getOpsList().enqueue(new Bush1(650, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush2(400, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush1(1600, 680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Bush2(1420, 680), CollisionOp.Operation.ADD);

                // Add hills
                CommandCenter.getInstance().getOpsList().enqueue(new Hill1(50, 674), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Hill2(850, 642), CollisionOp.Operation.ADD);

                // Introduce cloud1 from right but from 20 pixels outside the screen
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud1(DIM.width + 20,200), CollisionOp.Operation.ADD);

                // Introduce cloud2 from 1/3 pixels within the frame
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud2(Game.DIM.width/3, 150), CollisionOp.Operation.ADD);

                // Introduce cloud from the middle of the frame
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud3(Game.DIM.width/2, 100), CollisionOp.Operation.ADD);

                break;
        }

	}


    // Method to draw the game level components
    private void drawLevelGame() {
        Flag flag;
        switch (CommandCenter.getInstance().getLevel()) {
            case 1:
                // 1st set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(428,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(460,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(492,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(524,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(556,570), CollisionOp.Operation.ADD);


                // 1.5 set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(630,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(662,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(694,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(726,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(746,470), CollisionOp.Operation.ADD);


                // 2nd set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(828,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(860,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(892,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(924,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(956,570), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(436,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(468,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(500,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(532,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(564,532), CollisionOp.Operation.ADD);

                // Add Question block
                CommandCenter.getInstance().getOpsList().enqueue(new QuestionBlock(694,300), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(836,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(868,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(900,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(932,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(964,532), CollisionOp.Operation.ADD);


                // Add Independent coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1100,500), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1100,540), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1100,580), CollisionOp.Operation.ADD);


                // 3rd set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1396,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1428,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1460,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1492,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1524,570), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1432,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1468,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1500,532), CollisionOp.Operation.ADD);

                // 4th set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1608,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1640,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1672,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1704,470), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1736,470), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1648,432), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1680,432), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1712,432), CollisionOp.Operation.ADD);

                // 5th set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1808,370), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1840,370), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1872,370), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1904,370), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1936,370), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(1968,370), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1880,338), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(1912,338), CollisionOp.Operation.ADD);

                // Add flag pole components
                flag = new Flag(2080,280);
                CommandCenter.getInstance().getOpsList().enqueue(flag, CollisionOp.Operation.ADD);
                CommandCenter.getInstance().setFlag(flag);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(2100,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new FlagPole(2116,280), CollisionOp.Operation.ADD);


                break;

            case 2:
                // 1st set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(328,320), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(360,320), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(392,320), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(424,320), CollisionOp.Operation.ADD);

                // 2nd set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(678,320), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(710,320), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(742,320), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(774,320), CollisionOp.Operation.ADD);

                // 3rd set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(504,450), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(536,450), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(568,450), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(600,450), CollisionOp.Operation.ADD);

                // Add Piranha plant
                CommandCenter.getInstance().getOpsList().enqueue(new PiranhaPlant(552,404), CollisionOp.Operation.ADD);

                // 4th set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(328,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(360,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(392,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(424,570), CollisionOp.Operation.ADD);

                // 5th set of bricks
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(678,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(710,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(742,570), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Brick(774,570), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(336,258), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(368,258), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(400,258), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(432,258), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(686,258), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(718,258), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(750,258), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(782,258), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(336,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(368,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(400,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(432,532), CollisionOp.Operation.ADD);

                // Add coins
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(686,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(718,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(750,532), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Coin(782,532), CollisionOp.Operation.ADD);

                //Add platform to climb to flag
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1372,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1404,648), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1404,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1436,616), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1436,648), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1436,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1468,584), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1468,616), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1468,648), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1468,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1500,552), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1500,584), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1500,616), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1500,648), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1500,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1532,520), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1532,552), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1532,584), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1532,616), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1532,648), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1532,680), CollisionOp.Operation.ADD);

                // Add flag pole components
                flag = new Flag(1575,280);
                CommandCenter.getInstance().getOpsList().enqueue(flag, CollisionOp.Operation.ADD);
                CommandCenter.getInstance().setFlag(flag);
                CommandCenter.getInstance().getOpsList().enqueue(new Block(1595,680), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new FlagPole(1611,280), CollisionOp.Operation.ADD);


                break;
        }
    }

    // Method to check gravity of Mario
	private void checkGravity() {
        if (CommandCenter.getInstance().getMario() != null){
            Mario mario = CommandCenter.getInstance().getMario();

            if (mario.isDead()) {
                if (mario.getMarioDeadTimeLeft() <=0) {
                    CommandCenter.getInstance().getOpsList().enqueue(mario, CollisionOp.Operation.REMOVE);
                    CommandCenter.getInstance().spawnMario(false);
                    if (!CommandCenter.getInstance().isGameOver()) {
                        clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                }
            } else {
                int nMarioHeight = mario.getHeight();
                int nMarioWidth = mario.getWidth();
                int nMarioCenterX = mario.getCenter().x;
                int nMarioCenterY = mario.getCenter().y;
                int nPlatformHeight, nPlatformWidth, nMarioDeltaYNew;
                int nPlatformCenterX = 0, nPlatformCenterY = 0;
                boolean bMarioOnPlatform = false;
                boolean bMarioWithinPlatformRange = false;


                // Iterate over platform array list to check if Mario landed on a platform
                for (Movable movPlatform : CommandCenter.getInstance().getMovPlatform()) {
                    nPlatformHeight = movPlatform.getHeight();
                    nPlatformWidth = movPlatform.getWidth();
                    nPlatformCenterX = movPlatform.getCenter().x;
                    nPlatformCenterY = movPlatform.getCenter().y;

                    // Call method to check if Mario is within the range of this platform
                    bMarioWithinPlatformRange = checkWithinRange(nMarioCenterX, nMarioWidth, nPlatformCenterX, nPlatformWidth);

                    // Logic to stop Mario's jump if he hits a platform above him & also limit his vertical ascent to the threshold set for him.
                    if (mario.getMarioAscend()
                            && (nPlatformCenterY + nPlatformHeight) >= (nMarioCenterY + mario.getDeltaY())
                            && nPlatformCenterY < nMarioCenterY
                            && bMarioWithinPlatformRange
                            || nMarioCenterY < Mario.VERTICAL_LIMIT) {
                        mario.stopMarioAscend();

                        // Check if collision was with Question block and dispend expiring coins.
                        if (movPlatform instanceof QuestionBlock && ((QuestionBlock)movPlatform).getCoinsHeld() > 0
                                && bMarioWithinPlatformRange) {
                            Sound.playSound("Mario_Coin.wav");
                            CommandCenter.getInstance().getOpsList().enqueue(
                                    new ExpiringCoin(nPlatformCenterX + 8,nPlatformCenterY - 38), CollisionOp.Operation.ADD);
                            CommandCenter.getInstance().addScore(QuestionBlock.COIN_WORTH);
                            CommandCenter.getInstance().incrCoinScore();
                            ((QuestionBlock)movPlatform).decrCoins();
                        }
                    }
                    // Logic to check if Mario's edges are within platform and foot is above platform.
                    else if ((nMarioCenterY + nMarioHeight) == nPlatformCenterY
                            && nMarioCenterY < nPlatformCenterY && bMarioWithinPlatformRange) {
                             bMarioOnPlatform = true;
                             break;
                        // Adjust descend rate as Mario nears platform to ensure alignment with platform
                    } else if ((nMarioCenterY + nMarioHeight + mario.getDeltaJumpDownY()) > nPlatformCenterY
                            && bMarioWithinPlatformRange && mario.getMarioDescending()) {
                        nMarioDeltaYNew = nPlatformCenterY - (nMarioCenterY + nMarioHeight);
                        if (nMarioDeltaYNew > 0) { //Check to avoid negative delta when Mario is between two platforms
                            mario.setDeltaJumpDownY(nMarioDeltaYNew);
                        }
                    }
                }

                if (!bMarioOnPlatform && !CommandCenter.getInstance().getMario().getMarioAscend()) {
                    mario.setMarioDescend();
                } else {
                    mario.stopMarioDescend();
                }
            }
        }
        System.gc();
	}//end gravity check


    // Method to check collision with friends and foes
    private void checkCollision() {

        if (CommandCenter.getInstance().getMario() != null && !CommandCenter.getInstance().isLevelClear()) {
            Mario mario = CommandCenter.getInstance().getMario();

            int nMarioHeight = mario.getHeight();
            int nMarioWidth =  mario.getWidth();
            int nMarioCenterX = mario.getCenter().x;
            int nMarioCenterY = mario.getCenter().y;
            int nFriendHeight, nFriendWidth, nFriendCenterX, nFriendCenterY;
            for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
                if (!(movFriend instanceof Mario)) {
                    nFriendHeight = movFriend.getHeight();
                    nFriendWidth = movFriend.getWidth();
                    nFriendCenterX = movFriend.getCenter().x;
                    nFriendCenterY = movFriend.getCenter().y;
                    if (checkWithinRange(nMarioCenterX,nMarioWidth,nFriendCenterX,nFriendWidth)
                            && checkWithinRange(nMarioCenterY,nMarioHeight,nFriendCenterY,nFriendHeight)) {
                        if (movFriend instanceof Coin) {
                            CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                            Sound.playSound("Mario_Coin.wav");
                            CommandCenter.getInstance().incrCoinScore();
                            CommandCenter.getInstance().addScore(Coin.COIN_WORTH);
                        } else if (movFriend instanceof Flag) {
                            stopLoopingSounds(clpMusicBackground);
                            Sound.playSound("Mario_stage_clear.wav");
                            CommandCenter.getInstance().addScore(Flag.FLAG_WORTH);
                            CommandCenter.getInstance().setLevelClear(true);
                        }

                    }
                }
            }

            int nFoeHeight, nFoeWidth, nFoeCenterX, nFoeCenterY;
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
                nFoeHeight = movFoe.getHeight();
                nFoeWidth = movFoe.getWidth();
                nFoeCenterX = movFoe.getCenter().x;
                nFoeCenterY = movFoe.getCenter().y;
                if (checkWithinRange(nMarioCenterX,nMarioWidth,nFoeCenterX,nFoeWidth)
                    && checkWithinRange(nMarioCenterY,nMarioHeight + (Mario.DEFAULT_DESCEND_SPEED - nMarioHeight),nFoeCenterY,nFoeHeight)
                    && !movFoe.isDead()) {
                    if (!mario.getMarioDescending() && !mario.isDead() || movFoe instanceof PiranhaPlant && !mario.isDead()) {
                        stopLoopingSounds(clpMusicBackground);
                        Sound.playSound("Mario_die.wav");
                        mario.setDead();
                    } else if (!movFoe.isDead() && !mario.isDead()) {
                        Sound.playSound("Mario_touch_Foe.wav");
                        movFoe.setDead();
                        CommandCenter.getInstance().addScore(movFoe.getWorth());
                    }

                }
                if (movFoe.isDead() && movFoe.getDeadTimeLeft() == 0) {
                    CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);
                }
            }

        }

    }

    // Check for auto-expiring coins from the question block
    private void checkExpiringCoins() {
        if (CommandCenter.getInstance().getMario() != null) {
            for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
                if (movFriend instanceof ExpiringCoin) {
                    if (((ExpiringCoin) movFriend).getExpiryCounter() > 0) {
                        ((ExpiringCoin) movFriend).decrExpiryCounter();
                    } else {
                        CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                    }
                }
            }
        }
    }

    // Check for horizontal collision with pipe or block
    private void checkHorizontalCollision() {

        if (CommandCenter.getInstance().getMario() != null) {
            Mario mario = CommandCenter.getInstance().getMario();

            int nMarioWidth = mario.getWidth();
            int nMarioCenterX = mario.getCenter().x;
            int nMarioCenterY = mario.getCenter().y;

            for (Movable movPlatform : CommandCenter.getInstance().getMovPlatform()) {
                if (((movPlatform instanceof Pipe || movPlatform instanceof Block)
                        && nMarioCenterY > movPlatform.getCenter().y
                        && ((movPlatform.getCenter().x + movPlatform.getWidth() < nMarioCenterX
                        && movPlatform.getCenter().x + movPlatform.getWidth() > nMarioCenterX + mario.getDeltaMoveLeftX()
                        && mario.getMoveLeftCount() > 0 )
                        || (nMarioCenterX + nMarioWidth < movPlatform.getCenter().x
                        && nMarioCenterX + nMarioWidth + mario.getDeltaMoveRightX() > movPlatform.getCenter().x
                        && mario.getMoveRightCount() > 0))
                        && mario.checkMarioMoving())) {
                    mario.stopMarioHorizontalMove();
                }
            }
        }
    }

    // Method to check if cloud is out of screen, then remove and add a replacement for the same type
    private void checkClouds() {
        for (Movable movBackground : CommandCenter.getInstance().getMovBackground()) {
            if (movBackground instanceof Cloud1 && (movBackground.getCenter().x + movBackground.getWidth() + 100 < 0)) {
                CommandCenter.getInstance().getOpsList().enqueue(movBackground, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud1(DIM.width + 20,200), CollisionOp.Operation.ADD);
            }
            if (movBackground instanceof Cloud2 && (movBackground.getCenter().x + movBackground.getWidth() + 100 < 0)) {
                CommandCenter.getInstance().getOpsList().enqueue(movBackground, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud2(DIM.width + 20,150), CollisionOp.Operation.ADD);
            }
            if (movBackground instanceof Cloud3 && (movBackground.getCenter().x + movBackground.getWidth() + 200 < 0)) {
                CommandCenter.getInstance().getOpsList().enqueue(movBackground, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().getOpsList().enqueue(new Cloud3(DIM.width + 20,100), CollisionOp.Operation.ADD);
            }
        }
    }

    // Method to keep Foes within Frame
    private void checkFoes() {
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            for (Movable movPlatform : CommandCenter.getInstance().getMovPlatform()) {
                if (movPlatform instanceof Pipe && (movPlatform.getCenter().x + movPlatform.getWidth()) >= movFoe.getCenter().x
                        && movPlatform.getCenter().x < movFoe.getCenter().x) {
                    movFoe.setRightDirection();
                } else if (movFoe.getCenter().x + movFoe.getWidth() + 40 >= DIM.width
                        || movPlatform instanceof Pipe
                        && movPlatform.getCenter().x  <= movFoe.getCenter().x +  movFoe.getWidth()
                        && movPlatform.getCenter().x > movFoe.getCenter().x) {
                    movFoe.setLeftDirection();
                }
            }
        }
    }

    // Spawn a foes based on their intro interval but limit based on level multiplier
    // Ensure number of foes are proportionate to game level
    private void addFoes() {
        if (CommandCenter.getInstance().getLevel() != 0) {
            if (getTick() % (GOOMBA_INTRO_INTERVAL /ANI_DELAY/CommandCenter.getInstance().getLevel()) == 0
                    && CommandCenter.getInstance().getMovFoes().size() < CommandCenter.getInstance().getLevel() * FOE_LEVEL_MULTIPLIER) {
                CommandCenter.getInstance().spawnGoombas();
            }

            if (getTick() % (KOOPA_INTRO_INTERVAL /ANI_DELAY/CommandCenter.getInstance().getLevel()) == 0
                    && CommandCenter.getInstance().getMovFoes().size() < CommandCenter.getInstance().getLevel() * FOE_LEVEL_MULTIPLIER){
                CommandCenter.getInstance().spawnKoopas();
            }

            if (getTick() % (PARATROOPA_INTRO_INTERVAL /ANI_DELAY/CommandCenter.getInstance().getLevel()) == 0
                    && CommandCenter.getInstance().getMovFoes().size() < CommandCenter.getInstance().getLevel() * FOE_LEVEL_MULTIPLIER){
                CommandCenter.getInstance().spawnParatroopas();
            }
        }
    }

    private void processQueue() {

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

                case BACKGROUND:
                    if (operation == CollisionOp.Operation.ADD){
                        CommandCenter.getInstance().getMovBackground().add(mov);
                    } else {
                        CommandCenter.getInstance().getMovBackground().remove(mov);
                    }
                    break;
                case PLATFORM:
                    if (operation == CollisionOp.Operation.ADD){
                        CommandCenter.getInstance().getMovPlatform().add(mov);
                    } else {
                        CommandCenter.getInstance().getMovPlatform().remove(mov);
                    }
                    break;
            }
        }
        System.gc();
    }//end meth

	//some methods for timing events in the game, such as the appearance of UFOs, floaters (power-ups), etc.
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
		clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
		CommandCenter.getInstance().clearAll();
		CommandCenter.getInstance().initGame();
		CommandCenter.getInstance().setPlaying(true);
		CommandCenter.getInstance().setPaused(false);
		drawBackGround();
        drawLevelGame();
        CommandCenter.getInstance().spawnMario(true);
	}


    // Varargs for stopping looping-music-clips
	private static void stopLoopingSounds(Clip... clpClips) {
		for (Clip clp : clpClips) {
			clp.stop();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Mario mario = CommandCenter.getInstance().getMario();
		int nKey = e.getKeyCode();

		if (nKey == START && !CommandCenter.getInstance().isPlaying())
			startGame();

		if (mario != null) {
			switch (nKey) {
			case PAUSE:
				CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
				if (CommandCenter.getInstance().isPaused())
					stopLoopingSounds(clpMusicBackground);
				else
					clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
				break;
			case QUIT:
				System.exit(0);
				break;
            case UP:
                CommandCenter.getInstance().getMario().jump();
                break;
            case RIGHT:
                if (!CommandCenter.getInstance().getMario().isDead()) {
                    moveRight();
                }
                break;
            case LEFT:
                if (!CommandCenter.getInstance().getMario().isDead()) {
                    moveLeft();
                }
                break;
            // Cheat key to jump to next level
            case VK_N:
                stopLoopingSounds(clpMusicBackground);
                CommandCenter.getInstance().getMario().setCenter(new Point(
                        CommandCenter.getInstance().getFlag().getCenter().x -26,
                        CommandCenter.getInstance().getFlag().getCenter().y
                ));
                break;
			default:
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Mario mario = CommandCenter.getInstance().getMario();
		int nKey = e.getKeyCode();

		if (mario != null) {
			switch (nKey) {

			case MUTE:
				if (!bMuted){
					stopLoopingSounds(clpMusicBackground);
					bMuted = !bMuted;
				} 
				else {
					clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
					bMuted = !bMuted;
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

    public boolean checkWithinRange(int nObj1Center, int nObj1Length, int nObj2Center, int nObj2Length) {
        if ((nObj1Center + nObj1Length >= nObj2Center && nObj1Center <= nObj2Center)
                || (nObj2Center + nObj2Length >= nObj1Center && nObj2Center <= nObj1Center)) {
            return true;
        } else {
            return false;
        }
    }

    // Method to move right when the right arrow key is pressed. If Mario is beyond his screen limit, move everything else to left
    private void moveRight() {
        if (CommandCenter.getInstance().getMario().getCenter().getX() > Mario.SCREEN_RIGHT_LIMIT) {
            moveEverythingLeft();
        } else {
            CommandCenter.getInstance().getMario().moveRight();
        }
    }

    // Similar to moveRight method but this is for moving left.
    private void moveLeft() {
        if (CommandCenter.getInstance().getMario().getCenter().getX() < Mario.SCREEN_LEFT_LIMIT) {
            moveEverythingRight();
        } else {
            CommandCenter.getInstance().getMario().moveLeft();
        }
    }


    private void moveEverythingLeft() {
        CommandCenter.getInstance().setMoveCountX(Mario.DEFAULT_HORIZONTAL_STEPS);
        CommandCenter.getInstance().setDeltaX(-CommandCenter.getInstance().getMario().getDeltaMoveRightX());
    }

    private void moveEverythingRight() {
        CommandCenter.getInstance().setMoveCountX(Mario.DEFAULT_HORIZONTAL_STEPS);
        CommandCenter.getInstance().setDeltaX(-CommandCenter.getInstance().getMario().getDeltaMoveLeftX());
    }


    // Method to increment to next level of the game.
    private void setNextLevel() {
            CommandCenter.getInstance().setLevelClear(false);
            CommandCenter.getInstance().setNextLevel();
            try {
                Thread.sleep(100);
            } catch (java.lang.InterruptedException e) {
                e.printStackTrace();
            }
            switch (CommandCenter.getInstance().getLevel()) {
                case 2:
                    clpMusicBackground = Sound.clipForLoopFactory("Mario_Birabuto_Kingdom.wav");
                    break;
                default:
                    clpMusicBackground = Sound.clipForLoopFactory("Mario_background.wav");
                    break;
            }
            clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
            CommandCenter.getInstance().clearAll();
            CommandCenter.getInstance().setNumMarios(5);
            CommandCenter.getInstance().setCoins(0);
            drawBackGround();
            drawLevelGame();
            CommandCenter.getInstance().spawnMario(true);
            CommandCenter.getInstance().setTimeLeft(300);

        }

}

