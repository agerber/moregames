package edu.uchicago.gerber.joust.mvc.controller;



import edu.uchicago.gerber.joust.mvc.model.*;
import edu.uchicago.gerber.joust.mvc.view.GamePanel;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Gameplay reference: https://youtu.be/4JMY2dMnK_E
// Pixilart made on https://www.pixilart.com/draw# (see /images/pixilArt files)

public class Game implements Runnable, KeyListener {

    public static final Dimension DIM = new Dimension(700, 650);
    private GamePanel gmpPanel;
    public final static int ANI_DELAY = 45;

    // framerate tickers
    private Thread thrAnim;
    private int tick = 0;
    private int gameStateTicker;      // use for timed events
    private int respawnTicker;        // use for delaying bKid respawn

    // game metrics
    public static final int GAME_WON_LEVEL = 9;
    public static final int BONUS_LEVEL = 4;
    public static final int BONUS_BALLOON_COUNT = 22;
    public static final boolean DRAW_COLLISION_POINTS = false;     // show bFighter collision points in green

    // key code constants
    private final int START = 32; // space key
    private final int RESET = 82; // r-key
    private final int RIGHT = 39;
    private final int LEFT = 37;
    private final int UP = 38;

    public static final String SCORE_FILENAME = System.getProperty("user.dir") + File.separator + "src"
            + File.separator + "_08final" + File.separator + "highScore" + File.separator + "highScore.txt";

    private Clip music_bonusLevel;

    public Game() {
        gmpPanel = new GamePanel(DIM);
        gmpPanel.addKeyListener(this);
        music_bonusLevel = Sound.clipForLoopFactory("levelPlay.wav");
    }

    public static void main(String[] args) {
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

        // load high score from file
        try {
            Scanner scoreFile = new Scanner(new File(SCORE_FILENAME));
            int highScore = 0;
            if (scoreFile.hasNextInt()) { highScore = scoreFile.nextInt(); }
            CommandCenter.getInstance().setHighScore(highScore);
        }
        catch (FileNotFoundException | NumberFormatException e) { CommandCenter.getInstance().setHighScore(0); }
    }

    public void fireUpAnimThread() {
        if (thrAnim == null) {
            thrAnim = new Thread(this); // pass the thread a runnable object (this)
            thrAnim.start();
        }
    }

    @Override
    public void run() {

        thrAnim.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long lStartTime = System.currentTimeMillis();

        while (Thread.currentThread() == thrAnim) {
            tick();
            gmpPanel.update(gmpPanel.getGraphics());
            checkGameState();

            // charge clouds and spawn bubbles randomly when game is being played
            if (!CommandCenter.getInstance().isPaused() && !CommandCenter.getInstance().isGameOver()
                    && !CommandCenter.getInstance().isGameWon()) {
                if (CommandCenter.getInstance().isPlaying()) {
                    checkCollisions();
                    if (Math.random() >= 0.99 && Math.random() >= 0.20) spawnNewBubble();
                    if (Math.random() >= 0.97) chargeClouds();
                }
                // remove bKid if he's died and fallen below the destruction line
                if (CommandCenter.getInstance().bKidDead()&& CommandCenter.getInstance().getBKid().getY() > DIM.height + 20) {
                    if (respawnTicker == 0) {
                        Splash splash = new Splash(CommandCenter.getInstance().getBKid());
                        Sound.playSound("splash.wav");
                        CommandCenter.getInstance().getOpsList().enqueue(splash, CollisionOp.Operation.ADD);
                    }
                    respawnTicker += 1;

                    // delay respawn by 50 frames
                    if (respawnTicker >= 50) {
                        CommandCenter.getInstance().respawnBKid(false);
                        respawnTicker = 0;
                    }
                }
            }

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
        }
    }

    private void checkCollisions() {

        if (CommandCenter.getInstance().getBKid() != null && !CommandCenter.getInstance().bKidDead()) {
            BalloonKid bKid = CommandCenter.getInstance().getBKid();

            // check bKid-platform collisions
            int airborneCount = 0;
            for (Movable mov : CommandCenter.getInstance().getPlatforms()) {
                Platform plat = (Platform)(mov);
                plat.checkBalloonBump(bKid);
                plat.checkSideBump(bKid);
                if (plat.checkLand(bKid)) { airborneCount++; }
            }
            if (airborneCount > 0) bKid.setAirborne(false);
            else bKid.setAirborne(true);

            // check bKid-bFox collisions
            for (Movable mov : CommandCenter.getInstance().getbFoxes()) {
                if (mov instanceof BalloonFox) {
                    BalloonFox bFox = (BalloonFox)(mov);
                    if (!bFox.isDead()) bFox.checkClash(bKid);
                    if (bFox.getBalloons() <= 0 && !bFox.isParachuting() && bFox.isAirborne()) {
                        bFox.setParachuting(true);
                    }

                    // if a bFox has fallen below the destruction line , remove it
                    if (bFox.getPos().y > DIM.height + 20) {
                        Splash splash = new Splash(bFox);
                        CommandCenter.getInstance().getOpsList().enqueue(splash, CollisionOp.Operation.ADD);
                        CommandCenter.getInstance().getOpsList().enqueue(bFox, CollisionOp.Operation.REMOVE);
                        Sound.playSound("splash.wav");
                        if (CommandCenter.getInstance().getbFoxes().size() == 2) {
                            Sound.playSound("oneBFoxLeft.wav");
                        }
                    }
                    // if bFox falls into the lake, have the fish eat it
                    if (bFox.isParachuting() && !bFox.isDead() && !bFox.isEaten() && !bFox.isAboutToBeEaten()
                            && bFox.getLeftFootPos().y > DIM.height - 70) {
                        bFox.setAboutToBeEaten(true);
                        ReaperFish fish = new ReaperFish(bFox);
                        CommandCenter.getInstance().getOpsList().enqueue(fish, CollisionOp.Operation.ADD);
                        if (CommandCenter.getInstance().getbFoxes().size() > 1) Sound.playSound("fishFood.wav");
                    }
                }
            }

            // check bKid-bubble collisions
            for (Movable mov : CommandCenter.getInstance().getBubbles()) {
                Bubble bubble = (Bubble)(mov);
                if (!bubble.isBurst()) { bubble.checkCollision(bKid); }
                else if (bubble.getBurstTimer() <= 1) {
                    Sound.playSound("pop.wav");
                }
                else if (bubble.getBurstTimer() >= 8) {
                    CommandCenter.getInstance().getOpsList().enqueue(bubble, CollisionOp.Operation.REMOVE);
                }
            }

            // check bKid-bonus balloon collisions
            for (Movable mov : CommandCenter.getInstance().getBonusBalloons()) {
                BonusBalloon balloon = (BonusBalloon) (mov);
                if (!balloon.isBurst()) { balloon.checkCollision(bKid); }
                else if (balloon.getBurstTimer() <= 1) {
                    Sound.playSound("pop.wav");
                }
                else if (balloon.getBurstTimer() >= 8) {
                    CommandCenter.getInstance().getOpsList().enqueue(balloon, CollisionOp.Operation.REMOVE);
                    CommandCenter.getInstance().incBonusBalloonCount();
                }
            }

            // check bKid-bolt collisions
            for (Movable mov : CommandCenter.getInstance().getBolts()) {
                Bolt bolt = (Bolt)(mov);
                bolt.checkCollision(bKid);
                // check bolt-platform collisions
                for (Movable mo_v : CommandCenter.getInstance().getPlatforms()) {
                    Platform plat = (Platform)(mo_v);
                    plat.checkBoltBump(bolt);
                }
            }
            // check for bKid popped death
            if (bKid.getBalloons() < 1) {
                bKid.setPopped(true);
                bKid.setDead(true);
            }
            // check for bKid fish death
            if (!bKid.isDead() && !bKid.isEaten() && !bKid.isAboutToBeEaten()
                    && bKid.getLeftFootPos().y > DIM.height - 60 && CommandCenter.getInstance().getLevel() != BONUS_LEVEL
                    && !CommandCenter.getInstance().isTransition()) {
                bKid.setAboutToBeEaten(true);
                ReaperFish fish = new ReaperFish(bKid);
                CommandCenter.getInstance().getOpsList().enqueue(fish, CollisionOp.Operation.ADD);
                Sound.playSound("fishFood.wav");
            }
        }
        // check bFox-collisions with platforms
        for (Movable mov : CommandCenter.getInstance().getbFoxes()) {
            if (mov instanceof BalloonFox) {
                BalloonFox bFox = (BalloonFox)(mov);
                if(!bFox.isDead()) {
                    int airborneCount = 0;
                    for (Movable movPlat : CommandCenter.getInstance().getPlatforms()) {
                        Platform plat = (Platform)(movPlat);
                        plat.checkBalloonBump(bFox);
                        plat.checkSideBump(bFox);
                        if (plat.checkLand(bFox)) airborneCount++;
                    }
                    if (bFox.isAirborne()) {
                        if (airborneCount > 0 && bFox.isParachuting()) {
                            bFox.setAirborne(false);
                            bFox.setParachuting(false);
                            bFox.setParachuteUnfolding(false);
                        }
                        else if (airborneCount > 0 && !bFox.isParachuting()) bFox.jump();
                    }
                }
            }
        }

        // carry out queued operations
        while(!CommandCenter.getInstance().getOpsList().isEmpty()) {
            CollisionOp cop =  CommandCenter.getInstance().getOpsList().dequeue();
            Movable mov = cop.getMovable();
            CollisionOp.Operation operation = cop.getOperation();

            switch (mov.getTeam()) {
                case BKID:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getbKids().add(mov);
                    } else {
                        CommandCenter.getInstance().getbKids().remove(mov);
                    }
                    break;

                case BFOX:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getbFoxes().add(mov);
                    } else {
                        CommandCenter.getInstance().getbFoxes().remove(mov);
                    }
                    break;

                case FISH:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getFishes().add(mov);
                    } else {
                        CommandCenter.getInstance().getFishes().remove(mov);
                    }
                    break;

                case PLATFORM:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getPlatforms().add(mov);
                    } else {
                        CommandCenter.getInstance().getPlatforms().remove(mov);
                    }
                    break;

                case CLOUD:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getClouds().add(mov);
                    } else {
                        CommandCenter.getInstance().getClouds().remove(mov);
                    }
                    break;

                case BOLT:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getBolts().add(mov);
                    } else {
                        CommandCenter.getInstance().getBolts().remove(mov);
                    }
                    break;

                case BUBBLE:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getBubbles().add(mov);
                    } else {
                        CommandCenter.getInstance().getBubbles().remove(mov);
                    }
                    break;

                case BONUSBALLOON:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getBonusBalloons().add(mov);
                    } else {
                        CommandCenter.getInstance().getBonusBalloons().remove(mov);
                    }
                    break;

                case SCOREPOP:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getScorePops().add(mov);
                    } else {
                        CommandCenter.getInstance().getScorePops().remove(mov);
                    }
                    break;

                case DEBRIS:
                    if (operation == CollisionOp.Operation.ADD) {
                        CommandCenter.getInstance().getDebris().add(mov);
                    } else {
                        CommandCenter.getInstance().getDebris().remove(mov);
                    }
                    break;
            }
        }
    }

    /*
     Monitor and move between game states (playing, game over, game won, etc.)
      */
    private void checkGameState() {
        // check for game over
        if (CommandCenter.getInstance().isPlaying() && CommandCenter.getInstance().getLives() <= 0 && !CommandCenter.getInstance().isGameOver()) {
            CommandCenter.getInstance().setGameOver(true);
            gameStateTicker = tick;
            Sound.playSound("gameOver.wav");
        }

        // check for level clear
        if (CommandCenter.getInstance().isPlaying() && !CommandCenter.getInstance().isGameOver() && !CommandCenter.getInstance().isGameWon()) {
            if (CommandCenter.getInstance().getLevel() != BONUS_LEVEL) {
                // regular levels are clear when all bFoxes are dead
                if (CommandCenter.getInstance().getbFoxes().size() == 0 && !CommandCenter.getInstance().isTransition()
                        && !CommandCenter.getInstance().isCleared()) {
                    Sound.playSound("levelClear.wav");
                    gameStateTicker = tick;
                    CommandCenter.getInstance().setCleared(true);
                    CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 2000);
                }
            }
            // bonus level is clear when all bonus balloons are gone
            else if (CommandCenter.getInstance().getBonusBalloons().size() == 0 && !CommandCenter.getInstance().isTransition()
                    && !CommandCenter.getInstance().isCleared()) {
                gameStateTicker = tick;
                CommandCenter.getInstance().setCleared(true);
            }

            // allow levelClear.wav to play for 50 frames before moving to level transition screen
            if (tick - gameStateTicker == 50 && CommandCenter.getInstance().isCleared()) {
                CommandCenter.getInstance().setCleared(false);
                if(CommandCenter.getInstance().getLevel() + 1 != GAME_WON_LEVEL) {
                    if (CommandCenter.getInstance().getLevel() == BONUS_LEVEL) { stopAndResetLoopingSounds(music_bonusLevel); }
                    CommandCenter.getInstance().setTransition(true);
                    CommandCenter.getInstance().setLevel(CommandCenter.getInstance().getLevel() + 1);
                }
                else {
                    CommandCenter.getInstance().setGameWon(true);
                    Sound.playSound("gameWon2.wav");
                }
                gameStateTicker = tick;
            }

            // stay on level transition screen for 40 frames
            if (tick - gameStateTicker == 40 && CommandCenter.getInstance().isTransition()) {
                CommandCenter.getInstance().setTransition(false);
                buildLevel();
            }
        }
        else if ((tick - gameStateTicker == 200 && (CommandCenter.getInstance().isGameOver()) ||
                (tick - gameStateTicker == 340 && CommandCenter.getInstance().isGameWon()))) {
            CommandCenter.getInstance().resetGame();
        }
    }

    /*
    Clear movables from the CommandCenter and add new movables for the new game level.
     */
    private void buildLevel() {

        CommandCenter.getInstance().clearMovables();
        int level = CommandCenter.getInstance().getLevel();
        int blockHeight = 16;

        if (level == 1 || level == 2 || level == 3 || level == 5 || level == 6 || level == 7 || level == 8) {
            // make earth
            DeepEarthBlock deepEarthBlockR = new DeepEarthBlock(DeepEarthBlock.Direction.RIGHT);
            CommandCenter.getInstance().getOpsList().enqueue(deepEarthBlockR, CollisionOp.Operation.ADD);

            DeepEarthBlock deepEarthBlockL = new DeepEarthBlock(DeepEarthBlock.Direction.LEFT);
            CommandCenter.getInstance().getOpsList().enqueue(deepEarthBlockL, CollisionOp.Operation.ADD);
        }

        if (level == 1) {
            // make earth
            for (int i = 0; i <= 18; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 200, 400));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            BalloonFox bFox1 = new BalloonFox(new Point( 220, 360 ), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox2 = new BalloonFox(new Point(320, 360), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox3 = new BalloonFox(new Point(420, 360), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox3, CollisionOp.Operation.ADD);

            // make cloud
            Cloud cloud = new Cloud(new Point(370, 200));
            CommandCenter.getInstance().getOpsList().enqueue(cloud, CollisionOp.Operation.ADD);
        }
        else if (level == 2) {
            // make earth
            for (int i = 0; i <= 9; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+100, 250));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 18; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4,new Point(i*blockHeight+200, 400));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 9; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+500, 250));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            BalloonFox bFox1 = new BalloonFox(new Point( 220, 360 ), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox2 = new BalloonFox(new Point(320, 360), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox3 = new BalloonFox(new Point(420, 360), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox4 = new BalloonFox(new Point(120, 210), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox5 = new BalloonFox(new Point(520, 210), BalloonFox.Behavior.SEEKER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox5, CollisionOp.Operation.ADD);

            // make clouds
            Cloud cloud1 = new Cloud(new Point(120, 350));
            Cloud cloud2 = new Cloud(new Point(600, 300));
            CommandCenter.getInstance().getOpsList().enqueue(cloud1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud2, CollisionOp.Operation.ADD);
        }
        else if (level == 3) {
            // make earth
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+250, 200));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+150, 300));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+400, 400));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+500, 250));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i%4, new Point(i*blockHeight+300, 500));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            BalloonFox bFox1 = new BalloonFox(new Point( 270, 160 ), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox2 = new BalloonFox(new Point(170, 260), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox3 = new BalloonFox(new Point(420, 360), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox4 = new BalloonFox(new Point(520, 210), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox5 = new BalloonFox(new Point(320, 460), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox5, CollisionOp.Operation.ADD);

            // make clouds
            Cloud cloud1 = new Cloud(new Point(120, 150));
            Cloud cloud2 = new Cloud(new Point(600, 400));
            CommandCenter.getInstance().getOpsList().enqueue(cloud1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud2, CollisionOp.Operation.ADD);
        }
        else if (level == BONUS_LEVEL) {
            // make pipes
            Pipe shortPipe1 = new Pipe(Pipe.Length.SHORT, new Point(150, DIM.height - 70));
            Pipe longPipe1 = new Pipe(Pipe.Length.LONG, new Point(250, DIM.height - 91));
            Pipe shortPipe2 = new Pipe(Pipe.Length.SHORT, new Point(450, DIM.height - 70));
            Pipe longPipe2 = new Pipe(Pipe.Length.LONG, new Point(550, DIM.height - 91));
            CommandCenter.getInstance().getOpsList().enqueue(shortPipe1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(longPipe1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(shortPipe2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(longPipe2, CollisionOp.Operation.ADD);

            // make earth
            for (int i = 0; i <= 44; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight - 15, Game.DIM.height - 27));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 1; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 31, Game.DIM.height - 88));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bonus balloons
            BonusBalloon b1 = new BonusBalloon(shortPipe1, DIM.height + 80);
            BonusBalloon b2 = new BonusBalloon(shortPipe2, DIM.height + 160);
            BonusBalloon b3 = new BonusBalloon(shortPipe1, DIM.height + 200);
            BonusBalloon b4 = new BonusBalloon(longPipe1, DIM.height + 300);
            BonusBalloon b5 = new BonusBalloon(longPipe2, DIM.height + 350);
            BonusBalloon b6 = new BonusBalloon(longPipe1, DIM.height + 400);
            BonusBalloon b7 = new BonusBalloon(shortPipe2, DIM.height + 450);
            BonusBalloon b8 = new BonusBalloon(longPipe2, DIM.height + 600);
            BonusBalloon b9 = new BonusBalloon(longPipe2, DIM.height + 640);
            BonusBalloon b10 = new BonusBalloon(shortPipe2, DIM.height + 750);
            BonusBalloon b11 = new BonusBalloon(shortPipe2, DIM.height + 790);
            BonusBalloon b12 = new BonusBalloon(longPipe2, DIM.height + 800);
            BonusBalloon b13 = new BonusBalloon(shortPipe2, DIM.height + 830);
            BonusBalloon b14 = new BonusBalloon(shortPipe2, DIM.height + 870);
            BonusBalloon b15 = new BonusBalloon(longPipe1, DIM.height + 900);
            BonusBalloon b16 = new BonusBalloon(longPipe1, DIM.height + 1000);
            BonusBalloon b17 = new BonusBalloon(shortPipe1, DIM.height + 1080);
            BonusBalloon b18 = new BonusBalloon(longPipe2, DIM.height + 1150);
            BonusBalloon b19 = new BonusBalloon(shortPipe1, DIM.height + 1200);
            BonusBalloon b20 = new BonusBalloon(shortPipe2, DIM.height + 1230);
            BonusBalloon b21 = new BonusBalloon(longPipe1, DIM.height + 1280);
            BonusBalloon b22 = new BonusBalloon(longPipe2, DIM.height + 1310);
            CommandCenter.getInstance().getOpsList().enqueue(b1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b5, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b6, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b7, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b8, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b9, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b10, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b11, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b12, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b13, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b14, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b15, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b16, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b17, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b18, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b19, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b20, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b21, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(b22, CollisionOp.Operation.ADD);

            // start bonus music
            stopAndResetLoopingSounds(music_bonusLevel);
            music_bonusLevel.loop(2);
        }
        else if (level == 5) {
            // make earth
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 360, 250));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 140, 350));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 260, 390));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 500, 380));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 3; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 380, 490));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            BalloonFox bFox1 = new BalloonFox(new Point( 380, 210 ), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox2 = new BalloonFox(new Point(160, 310), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox3 = new BalloonFox(new Point(280, 350), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox4 = new BalloonFox(new Point(520, 340), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox5 = new BalloonFox(new Point(400, 450), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox5, CollisionOp.Operation.ADD);

            // make clouds
            Cloud cloud1 = new Cloud(new Point(180, 150));
            Cloud cloud2 = new Cloud(new Point(480, 200));
            CommandCenter.getInstance().getOpsList().enqueue(cloud1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud2, CollisionOp.Operation.ADD);
        }
        else if (level == 6) {
            // make earth
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 80, 150));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 100, 430));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 180, 380));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 260, 330));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 360, 280));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 440, 230));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 2; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 520, 180));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 4; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 320, 500));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            BalloonFox bFox1 = new BalloonFox(new Point( 90, 110 ), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox2 = new BalloonFox(new Point(190, 340), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox3 = new BalloonFox(new Point(270, 290), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox4 = new BalloonFox(new Point(370, 240), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox5 = new BalloonFox(new Point(450, 190), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox6 = new BalloonFox(new Point(530, 140), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox5, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox6, CollisionOp.Operation.ADD);

            // make clouds
            Cloud cloud1 = new Cloud(new Point(180, 150));
            Cloud cloud2 = new Cloud(new Point(440, 400));
            CommandCenter.getInstance().getOpsList().enqueue(cloud1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud2, CollisionOp.Operation.ADD);
        }
        else if (level == 7) {
            // make earth
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 100, 150));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 5; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 300, 150));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 480, 150));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 100, 300));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 5; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 300, 300));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 480, 300));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 100, 450));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 480, 450));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            BalloonFox bFox1 = new BalloonFox(new Point(90, 110), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox2 = new BalloonFox(new Point(320, 110), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox3 = new BalloonFox(new Point(500, 110), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox4 = new BalloonFox(new Point(295, 260), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox5 = new BalloonFox(new Point(325, 260), BalloonFox.Behavior.DRIFTER);
            BalloonFox bFox6 = new BalloonFox(new Point(355, 260), BalloonFox.Behavior.SEEKER);
            BalloonFox bFox7 = new BalloonFox(new Point(120, 410), BalloonFox.Behavior.SEEKER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox5, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox6, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(bFox7, CollisionOp.Operation.ADD);

            // make clouds
            Cloud cloud1 = new Cloud(new Point(80, 200));
            Cloud cloud2 = new Cloud(new Point(540, 200));
            Cloud cloud3 = new Cloud(new Point(80, 350));
            Cloud cloud4 = new Cloud(new Point(540, 350));
            Cloud cloud5 = new Cloud(new Point(320, 30));
            CommandCenter.getInstance().getOpsList().enqueue(cloud1, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud2, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud3, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud4, CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(cloud5, CollisionOp.Operation.ADD);
        }
        else if (level == 8) {
            // make earth
            // left perch
            for (int i = 0; i <= 5; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 60, 370));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // right perch
            for (int i = 0; i <= 4; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 580, 300));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // castle top
            for (int i = 0; i <= 24; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 150, 150));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // castle bottom
            for (int i = 0; i <= 24; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 150, 480));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // lair top
            for (int i = 0; i <= 10; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 260, 230));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // lair bottom
            for (int i = 0; i <= 10; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 260, 398));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // lair entryway
            for (int i = 0; i <= 6; i++) {
                EarthBlock earthBlock = new EarthBlock(i % 4, new Point(i * blockHeight + 150, 300));
                CommandCenter.getInstance().getOpsList().enqueue(earthBlock, CollisionOp.Operation.ADD);
            }
            // left upper castle wall
            for (int i = 0; i <= 8; i++) {
                EarthWallBlock earthWallBlock = new EarthWallBlock(i % 4, new Point( 150,i * blockHeight + 160), Movable.Direction.LEFT);
                CommandCenter.getInstance().getOpsList().enqueue(earthWallBlock, CollisionOp.Operation.ADD);
            }
            // left lower castle wall
            for (int i = 0; i <= 6; i++) {
                EarthWallBlock earthWallBlock = new EarthWallBlock(i % 4, new Point( 150,i * blockHeight + 370), Movable.Direction.LEFT);
                CommandCenter.getInstance().getOpsList().enqueue(earthWallBlock, CollisionOp.Operation.ADD);
            }
            // lair left wall
            for (int i = 0; i <= 5; i++) {
                EarthWallBlock earthWallBlock = new EarthWallBlock(i % 4, new Point( 260,i * blockHeight + 302), Movable.Direction.LEFT);
                CommandCenter.getInstance().getOpsList().enqueue(earthWallBlock, CollisionOp.Operation.ADD);
            }
            // lair right wall
            for (int i = 0; i <= 10; i++) {
                EarthWallBlock earthWallBlock = new EarthWallBlock(i % 4, new Point( 420,i * blockHeight + 232), Movable.Direction.RIGHT);
                CommandCenter.getInstance().getOpsList().enqueue(earthWallBlock, CollisionOp.Operation.ADD);
            }
            // castle right wall
            for (int i = 0; i <= 19; i++) {
                EarthWallBlock earthWallBlock = new EarthWallBlock(i % 4, new Point( 533,i * blockHeight + 163), Movable.Direction.RIGHT);
                CommandCenter.getInstance().getOpsList().enqueue(earthWallBlock, CollisionOp.Operation.ADD);
            }

            // make bFoxes
            // left perch
            BalloonFox bFox1 = new BalloonFox(new Point(60, 330), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox1, CollisionOp.Operation.ADD);
            // right perch
            BalloonFox bFox2 = new BalloonFox(new Point(600, 260), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox2, CollisionOp.Operation.ADD);
            // castle guard
            BalloonFox bFox4 = new BalloonFox(new Point(500, 440), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox4, CollisionOp.Operation.ADD);
            // lair guard
            BalloonFox bFox5 = new BalloonFox(new Point(190, 260), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox5, CollisionOp.Operation.ADD);
            // lair fox 1
            BalloonFox bFox6 = new BalloonFox(new Point(290, 358), BalloonFox.Behavior.DRIFTER);
            CommandCenter.getInstance().getOpsList().enqueue(bFox6, CollisionOp.Operation.ADD);

            // make clouds
            // overhead
            for (int i = 0; i<= 3; i++) {
                Cloud cloud = new Cloud(new Point(200*i + 20, 35));
                CommandCenter.getInstance().getOpsList().enqueue(cloud, CollisionOp.Operation.ADD);
            }
            // lair cloud
            Cloud cloud3 = new Cloud(new Point(320, 280));
            CommandCenter.getInstance().getOpsList().enqueue(cloud3, CollisionOp.Operation.ADD);
        }
    }

    public int getTick() { return tick; }

    public void tick() {
        if (tick == Integer.MAX_VALUE) { tick = 0; }
        else { tick++; }

        if (CommandCenter.getInstance().getBumpSoundTicker() == Integer.MAX_VALUE) { CommandCenter.getInstance().setBumpSoundTicker(0); }
        else { CommandCenter.getInstance().setBumpSoundTicker(CommandCenter.getInstance().getBumpSoundTicker() + 1); }

        if (CommandCenter.getInstance().getShockSoundTicker() == Integer.MAX_VALUE) { CommandCenter.getInstance().setShockSoundTicker(0); }
        else { CommandCenter.getInstance().setShockSoundTicker(CommandCenter.getInstance().getShockSoundTicker() + 1); }

        if (CommandCenter.getInstance().getbFoxJumpSoundTicker() == Integer.MAX_VALUE) { CommandCenter.getInstance().setbFoxJumpSoundTicker(0); }
        else { CommandCenter.getInstance().setbFoxJumpSoundTicker(CommandCenter.getInstance().getbFoxJumpSoundTicker() + 1); }

        if (CommandCenter.getInstance().getBubbleBirthSoundTicker() == Integer.MAX_VALUE) { CommandCenter.getInstance().setBubbleBirthSoundTicker(0); }
        else { CommandCenter.getInstance().setBubbleBirthSoundTicker(CommandCenter.getInstance().getBubbleBirthSoundTicker() + 1); }

        if (CommandCenter.getInstance().getLightningSoundTicker() == Integer.MAX_VALUE) { CommandCenter.getInstance().setLightningSoundTicker(0); }
        else { CommandCenter.getInstance().setLightningSoundTicker(CommandCenter.getInstance().getLightningSoundTicker() + 1); }
    }

    private void spawnNewBubble() {
        if (CommandCenter.getInstance().getLevel() != BONUS_LEVEL) {
            CommandCenter.getInstance().getOpsList().enqueue(new Bubble(), CollisionOp.Operation.ADD);
            if (CommandCenter.getInstance().getBubbleBirthSoundTicker() > 3) {
                Sound.playSound("newBubbleQuiet.wav");
                CommandCenter.getInstance().setBubbleBirthSoundTicker(0);
            }
        }
    }

    private void chargeClouds() {
        for (Movable mov : CommandCenter.getInstance().getClouds()) {
            Cloud cloud = (Cloud) (mov);
            int chargeUp = (int) (30 * Math.random());
            cloud.setCharge(cloud.getCharge() + chargeUp);

            // when charge gets to 100%, create a lightning bolt
            if (cloud.getCharge() >= 100) {
                if (CommandCenter.getInstance().getBolts().size() < 4) {
                    cloud.setStriking(true);
                    cloud.setCharge(0);
                }
            }
        }
    }

    private static void stopAndResetLoopingSounds(Clip... clpClips) {
        for (Clip clp : clpClips) {
            clp.setFramePosition(0);
            clp.stop();
        }
    }

    private static void pauseLoopingSounds(Clip... clpClips) {
        for (Clip clp : clpClips) {
            clp.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int nKey = e.getKeyCode();

        if(nKey == START) {
            // start the game from the title screen
            if (!CommandCenter.getInstance().isPlaying()) {
                CommandCenter.getInstance().initGame();
                gameStateTicker = tick;
                CommandCenter.getInstance().setTransition(true);
            }
            // pause the game during play
            else if(!CommandCenter.getInstance().isGameOver() && !CommandCenter.getInstance().isGameWon()
                        && !CommandCenter.getInstance().isTransition() && !CommandCenter.getInstance().isCleared()) {
                CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                if (CommandCenter.getInstance().isPaused()) {
                    Sound.playSound("pause.wav");
                    if (CommandCenter.getInstance().getLevel() == BONUS_LEVEL) { pauseLoopingSounds(music_bonusLevel); }
                }
                else if (CommandCenter.getInstance().getLevel() == BONUS_LEVEL) { music_bonusLevel.start(); }
            }
        }

        if(nKey == RESET) {
            CommandCenter.getInstance().resetGame();
            stopAndResetLoopingSounds(music_bonusLevel);
        }

        BalloonKid bKid = CommandCenter.getInstance().getBKid();

        // guard against null pointers and don't react to inputs when paused
        if (bKid != null && !CommandCenter.getInstance().isPaused() && CommandCenter.getInstance().isPlaying()) {
            switch (nKey) {
                case LEFT:
                    bKid.setDirection(Movable.Direction.LEFT);
                    bKid.setMoving(true);
                    break;

                case RIGHT:
                    bKid.setDirection(Movable.Direction.RIGHT);
                    bKid.setMoving(true);
                    break;

                case UP:
                    if (!bKid.isDead() && !CommandCenter.getInstance().isTransition()) bKid.jump();
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        BalloonKid bKid = CommandCenter.getInstance().getBKid();

        // guard against null pointers and don't react to inputs when paused
        if (bKid != null && !CommandCenter.getInstance().isPaused() && CommandCenter.getInstance().isPlaying()) {
            int nKey = e.getKeyCode();
            switch (nKey) {
                case LEFT:
                    if (bKid.getDirection() == Movable.Direction.LEFT)
                        bKid.setMoving(false);
                    break;

                case RIGHT:
                    if (bKid.getDirection() == Movable.Direction.RIGHT)
                        bKid.setMoving(false);
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }
}