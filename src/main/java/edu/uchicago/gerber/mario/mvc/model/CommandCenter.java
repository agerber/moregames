package mario.mvc.model;

import mario.mvc.controller.Game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class CommandCenter {

	private  int nNumMario;
	private  int nLevel;
	private  long lScore;
	private  int nCoins;
	private  Mario mario;
    private  Flag flag;
	private  boolean bPlaying;
	private  boolean bPaused;
    private int nSecondsLeft;
    private long lSysTimeSeconds;
    private boolean bInitPosFlag = false;
    private boolean bLevelClear = false;
    private Point pntFlagCenterTracker;

    private Ground groundFirst, groundLast;
	
	// These ArrayLists with capacities set
	private List<Movable> movPlatform = new ArrayList<Movable>(300);
	private List<Movable> movFriends = new ArrayList<Movable>(100);
	private List<Movable> movFoes = new ArrayList<>(200);
	private List<Movable> movBackground = new ArrayList<>(50);

	private GameOpsList opsList = new GameOpsList();

	private static CommandCenter instance = null;

    private int nMoveCountX = 0;
    private int nDeltaX = 0;

	// Constructor made private - static Utility class only
	private CommandCenter() {}

	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}

	public  void initGame(){
		setLevel(1);
		setScore(0);
        setCoins(0);
		setNumMarios(5);
        nSecondsLeft = 300;
        lSysTimeSeconds = System.currentTimeMillis()/1000;
	}

    public void setTimeLeft(int nSecondsLeft) {
        this.nSecondsLeft = nSecondsLeft;
    }

	
	// The parameter is true if this is for the beginning of the game, otherwise false
	public  void spawnMario(boolean bFirst) {
		if (getNumMarios() != 0) {
			mario = new Mario(220,680);
			opsList.enqueue(mario, CollisionOp.Operation.ADD);
			if (!bFirst) {
                setInitPosFlag(true);
                setNumMarios(getNumMarios() - 1);
            }

		}
	}

	public void spawnGoombas()  {
        switch (nLevel) {
            case 1:
                opsList.enqueue(new Goomba(1040,680), CollisionOp.Operation.ADD);
                break;
            case 2:
                opsList.enqueue(new Goomba(1040,680), CollisionOp.Operation.ADD);
                break;
        }
    }

    public void spawnKoopas()  {
        switch (nLevel) {
            case 1:
                opsList.enqueue(new Koopa(1040,664), CollisionOp.Operation.ADD);
                break;
        }
    }

    public void spawnParatroopas()  {
        switch (nLevel) {
            case 2:
                opsList.enqueue(new Paratroopa(1040,664), CollisionOp.Operation.ADD);
                break;
        }
    }

	public GameOpsList getOpsList() {
		return opsList;
	}

	public void setOpsList(GameOpsList opsList) {
		this.opsList = opsList;
	}

	public  void clearAll(){
		movPlatform.clear();
		movFriends.clear();
		movFoes.clear();
		movBackground.clear();
	}

	public  boolean isPlaying() {
		return bPlaying;
	}

	public  void setPlaying(boolean bPlaying) {
		this.bPlaying = bPlaying;
	}

	public  boolean isPaused() {
		return bPaused;
	}

	public  void setPaused(boolean bPaused) {
		this.bPaused = bPaused;
	}
	
	public  boolean isGameOver() {		//if the number of Marios is zero or seconds left is zero, then game over
		if ((getNumMarios() == 0 || nSecondsLeft == 0 || nLevel > Game.GAME_MAX_LEVEL) && nLevel != 0) {
			return true;
		}
		return false;
	}

	public  int getLevel() {
		return nLevel;
	}

    public void setNextLevel() {
        nLevel++;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public Flag getFlag() {
        return flag;
    }

    public boolean isLevelClear() {
        return bLevelClear;
    }

    public void setLevelClear(boolean bLevelClearStatus) {
        bLevelClear = bLevelClearStatus;
    }


	public   long getScore() {
		return lScore;
	}

    public void setCoins(int nCoins) {
        this.nCoins = nCoins;
    }

    public int getCoins() {
        return nCoins;
    }

    public void incrCoinScore() {
        nCoins++;
    }

	public  void setScore(long lParam) {
		lScore = lParam;
	}

    public  void addScore(long lParam) {
        lScore += lParam;
    }

	public  void setLevel(int n) {
		nLevel = n;
	}

	public  int getNumMarios() {
		return nNumMario;
	}

	public  void setNumMarios(int nParam) {
		nNumMario = nParam;
	}
	
	public  Mario getMario(){
		return mario;
	}
	
	public  void setMario(Mario marioParam){
		mario = marioParam;
	}

	public  List<Movable> getMovFriends() {
		return movFriends;
	}

	public  List<Movable> getMovFoes() {
		return movFoes;
	}

	public  List<Movable> getMovBackground() {
		return movBackground;
	}

	public  List<Movable> getMovPlatform() {
		return movPlatform;
	}

    public int getMoveCountX() {
        return nMoveCountX;
    }

    public void setMoveCountX(int nMoveCount) {
        this.nMoveCountX = nMoveCount;
    }

    public void decrMoveCountX() {
        nMoveCountX--;
    }

    public int getDeltaX() {
        return nDeltaX;
    }

    public void setDeltaX(int nDeltaX) {
        this.nDeltaX = nDeltaX;
    }

    public void setGroundFirst(Ground ground) {
        groundFirst = ground;
    }

    public void setGroundLast (Ground ground) {
        groundLast = ground;
    }

    public Ground getGroundFirst() {
        return groundFirst;
    }

    public Ground getGroundLast() {
        return groundLast;
    }

    public int getGameTimeLeft() {
        return nSecondsLeft;
    }

    public void updateTimeLeft() {
        if (lSysTimeSeconds != System.currentTimeMillis()/1000) {
            nSecondsLeft--;
            lSysTimeSeconds = System.currentTimeMillis()/1000;
        }
    }

    public boolean getInitPosFlag() {
        return bInitPosFlag;
    }

    public void setInitPosFlag(boolean bInitPosFlag) {
        this.bInitPosFlag = bInitPosFlag;
    }

    public void setPntFlagCenterTracker(int nCenterX, int nCenterY) {
        pntFlagCenterTracker = new Point(nCenterX,nCenterY);
    }

    public Point getPntFlagCenterTracker() {
        return pntFlagCenterTracker;
    }


}
