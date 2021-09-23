package joust.mvc.model;

import joust.mvc.controller.Game;
import joust.sounds.Sound;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CommandCenter {

    private int lives;
    private int level;
    private int score;
    private int highScore;
    private int bonusBalloonCount;
    private BalloonKid bKid;
    private boolean bKidDead;
    private boolean playing;
    private boolean paused;
    private boolean cleared;
    private boolean transition;
    private boolean gameOver;
    private boolean gameWon;

    public static final int ONE_UP_POINTS = 22;

    // force delays between bump and shock sound effects
    private int bumpSoundTicker;
    private int shockSoundTicker;
    private int bFoxJumpSoundTicker;
    private int bubbleBirthSoundTicker;
    private int lightningSoundTicker;

    private List<Movable> bKids = new ArrayList<>();
    private List<Movable> bFoxes = new ArrayList<>();
    private List<Movable> fishes = new ArrayList<>();
    private List<Movable> platforms = new ArrayList<>();
    private List<Movable> clouds = new ArrayList<>();
    private List<Movable> bolts = new ArrayList<>();
    private List<Movable> bubbles = new ArrayList<>();
    private List<Movable> bonusBalloons = new ArrayList<>();
    private List<Movable> scorePops = new ArrayList<>();
    private List<Movable> debris = new ArrayList<>();

    private GameOpsList opsList = new GameOpsList();

    private static CommandCenter instance = null;

    // Constructor made private - static Utility class only
    private CommandCenter() {}

    public static CommandCenter getInstance(){
        if (instance == null){
            instance = new CommandCenter();
        }
        return instance;
    }

    public void initGame() {
        setLevel(1);
        setScore(0);
        setLives(3);
        respawnBKid(true);
        setPlaying(true);
        setGameOver(false);
    }

    public void resetGame() {
        setPlaying(false);
        setTransition(false);
        setCleared(false);
        setGameWon(false);
        setGameOver(false);
        setBonusBalloonCount(0);
        clearMovables();
    }

    public void respawnBKid(boolean firstSpawn) {
        if (!firstSpawn) lives -= 1;

        if (bKid == null) {
            bKid = new BalloonKid();
            opsList.enqueue(bKid, CollisionOp.Operation.ADD);
        }
        else if (lives > 0) { bKid.resetBKid(); }

        bKidDead = false;
        bKid.setInvincible(true);
        if (lives > 0) { Sound.playSound("respawn.wav"); }
    }

    public BalloonKid getBKid() { return bKid; }
    public void setbKid(BalloonKid bKid) { this.bKid = bKid; }

    public boolean bKidDead() { return bKidDead; }
    public void setbKidDead(boolean bKidDead) { this.bKidDead = bKidDead; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public GameOpsList getOpsList() { return opsList; }
    public void setOpsList(GameOpsList opsList) { this.opsList = opsList; }

    public void clearMovables() {
        bKid.resetBKid();
        bFoxes.clear();
        fishes.clear();
        platforms.clear();
        clouds.clear();
        bolts.clear();
        bubbles.clear();
        bonusBalloons.clear();
        scorePops.clear();
        debris.clear();
    }
    public List<Movable> getbKids() { return bKids; }
    public List<Movable> getbFoxes() { return bFoxes; }
    public List<Movable> getFishes() { return fishes; }
    public List<Movable> getPlatforms() { return platforms; }
    public List<Movable> getClouds() { return clouds; }
    public List<Movable> getBolts() { return bolts; }
    public List<Movable> getBubbles() { return bubbles; }
    public List<Movable> getBonusBalloons() { return bonusBalloons; }
    public List<Movable> getScorePops() { return scorePops; }
    public List<Movable> getDebris() { return debris; }

    public boolean isPlaying() { return playing; }
    public void setPlaying(boolean playing) { this.playing = playing; }

    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) { checkForHighScore(); }
    }

    public boolean isGameWon() { return gameWon; }
    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
        if (gameWon) { checkForHighScore(); }
    }

    private void checkForHighScore() {
        if (score > highScore) {
            setHighScore(score);
            try {
                PrintWriter scoreFile = new PrintWriter(Game.SCORE_FILENAME);
                try { scoreFile.print(highScore); }
                finally { scoreFile.close(); }
            }
            catch (FileNotFoundException e) {
                System.out.println("High score file could not be written.");
            }
        }
    }

    public boolean isTransition() { return transition; }
    public void setTransition(boolean transition) { this.transition = transition; }

    public boolean isCleared() { return cleared; }
    public void setCleared(boolean cleared) {
        this.cleared = cleared;
        if (level != Game.BONUS_LEVEL) { paused = cleared; }          // pause movement during cleared song
    }

    public void setBonusBalloonCount(int bonusBalloonCount) { this.bonusBalloonCount = bonusBalloonCount; }
    public void incBonusBalloonCount() {
        bonusBalloonCount += 1;
        if (bonusBalloonCount == Game.BONUS_BALLOON_COUNT) {
            ScorePop scorePop = new ScorePop(new Point(bKid.getX() - 20, bKid.getY()), ONE_UP_POINTS);
            CommandCenter.getInstance().getOpsList().enqueue(scorePop, CollisionOp.Operation.ADD);
            Sound.playSound("oneUp.wav");
        }
    }

    public int getBumpSoundTicker() { return bumpSoundTicker; }
    public void setBumpSoundTicker(int bumpSoundTicker) { this.bumpSoundTicker = bumpSoundTicker; }

    public int getShockSoundTicker() { return shockSoundTicker; }
    public void setShockSoundTicker(int shockSoundTicker) { this.shockSoundTicker = shockSoundTicker; }

    public int getbFoxJumpSoundTicker() { return bFoxJumpSoundTicker; }
    public void setbFoxJumpSoundTicker(int bFoxJumpSoundTicker) { this.bFoxJumpSoundTicker = bFoxJumpSoundTicker; }

    public int getBubbleBirthSoundTicker() { return bubbleBirthSoundTicker; }
    public void setBubbleBirthSoundTicker(int bubbleBirthSoundTicker) { this.bubbleBirthSoundTicker = bubbleBirthSoundTicker; }

    public int getLightningSoundTicker() { return lightningSoundTicker; }
    public void setLightningSoundTicker(int lightningSoundTicker) { this.lightningSoundTicker = lightningSoundTicker; }
}
