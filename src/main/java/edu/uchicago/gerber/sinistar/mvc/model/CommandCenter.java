package edu.uchicago.gerber.sinistar.mvc.model;



import edu.uchicago.gerber.sinistar.mvc.controller.Sound;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CommandCenter {

	private  int nNumFalcon;
	private int numAsteroids;
    private int numWorkers;
    private int numTurrets;
	private int numStars;
	private  long lScore;
	private Falcon falShip;
    private Sinistar sinShip;
	private Minimap minimap;
	private  boolean bPlaying;
	private  boolean bPaused;

	private BufferedImage workerShip;
	private BufferedImage workerShipCrystal;
	private BufferedImage falcon;
	private BufferedImage turret;
	private BufferedImage bomb1;
	private BufferedImage bomb2;
	private BufferedImage bomb3;
	private BufferedImage explosion1;
	private BufferedImage explosion2;
	private BufferedImage explosion3;
	private BufferedImage explosion4;
	private BufferedImage sinistar1;
	private BufferedImage sinistar2;
	private BufferedImage sinistar3;
	private BufferedImage sinistar4;
	private BufferedImage sinistar5;
	private BufferedImage sinistar6;
	private BufferedImage sinistar7;
	private BufferedImage sinistar8;
	private BufferedImage sinistar9;
	private BufferedImage sinistarAlive1;
	private ArrayList<BufferedImage> asteroids;

	// These ArrayLists with capacities set
	private List<Movable> movDebris = new ArrayList<Movable>(300);
	private List<Movable> movFriends = new ArrayList<Movable>(100);
	private List<Movable> movFoes = new ArrayList<Movable>(200);
	private List<Movable> movFloaters = new ArrayList<Movable>(50);

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


	public void initGame(){
		loadGraphics();
		minimap = new Minimap();
		setScore(0);
		setNumAsteroids(0);
        setNumWorkers(0);
		setNumFalcons(3);
        setNumTurrets(0);
		spawnStars();
		spawnAsteroids();
        spawnWorkers();
        spawnTurrets();
        spawnSinistar();
		spawnFalcon(true);
	}

	private void loadGraphics() {
		asteroids = new ArrayList<>();
		asteroids.add(loadGraphic("asteroid1.png"));
		asteroids.add(loadGraphic("asteroid2.png"));
		asteroids.add(loadGraphic("asteroid3.png"));
		asteroids.add(loadGraphic("asteroid4.png"));
		asteroids.add(loadGraphic("asteroid5.png"));

		workerShip = loadGraphic("WorkerShip.png");
		workerShipCrystal = loadGraphic("WorkerShipCrystal.png");
		falcon = loadGraphic("ship.png");
		turret = loadGraphic("turret.png");
		bomb1 = loadGraphic("bomb1.png");
		bomb2 = loadGraphic("bomb2.png");
		bomb3 = loadGraphic("bomb3.png");
		explosion1 = loadGraphic("explosion1.png");
		explosion2 = loadGraphic("explosion2.png");
		explosion3 = loadGraphic("explosion3.png");
		explosion4 = loadGraphic("explosion4.png");
		sinistar1 = loadGraphic("sinistar1.png");
		sinistar2 = loadGraphic("sinistar2.png");
		sinistar3 = loadGraphic("sinistar3.png");
		sinistar4 = loadGraphic("sinistar4.png");
		sinistar5 = loadGraphic("sinistar5.png");
		sinistar6 = loadGraphic("sinistar6.png");
		sinistar7 = loadGraphic("sinistar7.png");
		sinistar8 = loadGraphic("sinistar8.png");
		sinistar9 = loadGraphic("sinistar9.png");
		sinistarAlive1 = loadGraphic("sinistarAlive1.png");
	}

	private BufferedImage loadGraphic(String imgName) {
		BufferedImage img;
		try {
			img = ImageIO.read(new File("src/sinistar/img/" + imgName));
		}
		catch (IOException e) {
			e.printStackTrace();
			img = null;
		}
		return img;
	}

	public BufferedImage getAsteroid(int asteroidNum) {
		return asteroids.get(asteroidNum);
	}

	public BufferedImage getBomb(int bombNum) {
		switch(bombNum){
			case 1:
				return bomb1;
			case 2:
				return bomb2;
			case 3:
				return bomb3;
			default:
				return null;
		}
	}

	public BufferedImage getExplosion(int explosionNum) {
		switch(explosionNum){
			case 1:
				return explosion1;
			case 2:
				return explosion2;
			case 3:
				return explosion3;
			case 4:
				return explosion4;
			default:
				return null;
		}
	}

	public BufferedImage getSinistarImg(int sinistarNum) {
		switch(sinistarNum){
			case 1:
				return sinistar1;
			case 2:
				return sinistar2;
			case 3:
				return sinistar3;
			case 4:
				return sinistar4;
			case 5:
				return sinistar5;
			case 6:
				return sinistar6;
			case 7:
				return sinistar7;
			case 8:
				return sinistar8;
			case 9:
				return sinistar9;
			case 10:
				return sinistarAlive1;
			default:
				return null;
		}
	}

	public BufferedImage getTurret() {
		return turret;
	}

	public BufferedImage getWorkerShip() {
		return workerShip;
	}

	public BufferedImage getFalconImg(){
		return falcon;
	}



	public BufferedImage getWorkerShipCrystal() { return workerShipCrystal;}



	// The parameter is true if this is for the beginning of the game, otherwise false
	// When you spawn a new falcon, you need to decrement its number

	public  void spawnFalcon(boolean bFirst) {
		if (getNumFalcons() != 0) {
			falShip = new Falcon();

			opsList.enqueue(falShip, CollisionOp.Operation.ADD);
			if (!bFirst)
			    setNumFalcons(getNumFalcons() - 1);
		}
		
		Sound.playSound("shipspawn.wav");
	}



	public void spawnSinistar(){
        sinShip = new Sinistar();
        opsList.enqueue(sinShip, CollisionOp.Operation.ADD);
    }

	public void spawnAsteroids() {
		while (getNumAsteroids() < Asteroid.MAX_ASTEROIDS){
			spawnAsteroid();
		}
	}

	public void spawnAsteroid(){
		opsList.enqueue(new Asteroid(), CollisionOp.Operation.ADD);
		setNumAsteroids(getNumAsteroids() + 1);
	}

	public void spawnStars(){
		while (getNumStars() < Star.NUM_STARS){
			spawnStar();
		}
	}

	public void spawnStar(){
		opsList.enqueue(new Star(), CollisionOp.Operation.ADD);
		setNumStars(getNumStars() + 1);
	}

	public void spawnWorker() {
        opsList.enqueue(new WorkerShip(), CollisionOp.Operation.ADD);
        setNumWorkers(getNumWorkers() + 1);
    }

	public void spawnWorkers() {
        while (getNumWorkers() < WorkerShip.MAX_WORKERS) {
            spawnWorker();
        }
    }

    public void spawnTurret() {
        opsList.enqueue(new TurretShip(), CollisionOp.Operation.ADD);
        setNumTurrets(getNumTurrets() + 1);
    }

    public void spawnTurrets() {
        while (getNumTurrets() < TurretShip.MAX_TURRETS) {
            spawnTurret();
        }
    }

	public GameOpsList getOpsList() {
		return opsList;
	}

	public void setOpsList(GameOpsList opsList) {
		this.opsList = opsList;
	}

	public  void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
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
	
	public  boolean isGameOver() {		//if the number of falcons is zero, then game over
		if (getNumFalcons() == 0) {
			return true;
		}
		return false;
	}

	public   long getScore() {
		return lScore;
	}

	public  void setScore(long lParam) {
		lScore = lParam;
	}

	public  int getNumFalcons() {
		return nNumFalcon;
	}

	public  void setNumFalcons(int nParam) {
		nNumFalcon = nParam;
	}

	public int getNumAsteroids() {
		return numAsteroids;
	}

	public void setNumAsteroids(int numAsteroids) { this.numAsteroids = numAsteroids;}

    public int getNumWorkers() {
        return numWorkers;
    }

    public int getNumTurrets() {
        return numTurrets;
    }

    public void setNumTurrets(int numTurrets) {
        this.numTurrets = numTurrets;
    }

    public void setNumWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
    }

    public  Falcon getFalcon(){
		return falShip;
	}
	
	public  void setFalcon(Falcon falParam){
		falShip = falParam;
	}

	public Minimap getMinimap() {
		return minimap;
	}

	public int getNumStars() {
		return numStars;
	}

	public void setNumStars(int numStars) {
		this.numStars = numStars;
	}

	public Sinistar getSinistar() {
        return sinShip;
    }

    public void setSinistar(Sinistar sinShip) {
        this.sinShip = sinShip;
    }

    public  List<Movable> getMovDebris() {
		return movDebris;
	}

	public  List<Movable> getMovFriends() {
		return movFriends;
	}

	public  List<Movable> getMovFoes() {
		return movFoes;
	}

	public  List<Movable> getMovFloaters() {
		return movFloaters;
	}

	public List<Movable> getMov() {
		List<Movable> movList = new ArrayList<>();
		movList.addAll(movDebris);
		movList.addAll(movFriends);
		movList.addAll(movFoes);
		movList.addAll(movFloaters);
		return movList;
	}
}
