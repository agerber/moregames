package edu.uchicago.gerber.sinistar.mvc.model;


import edu.uchicago.gerber.sinistar.mvc.controller.Game;

import java.awt.*;
import java.awt.image.BufferedImage;



public class Asteroid extends DestroyableFoe {

    private static final int RADIUS = 25;
	private static final int SMALL_RADIUS = 20;
	private static final int LARGE_RADIUS = 30;
	private static final int MAX_SPIN = 10;
    private static final int MIN_SPEED = 1;
    private static final int MAX_SPEED = 1;
    public static final int MAX_ASTEROIDS = 300;
    private static final int MAX_HEALTH = 5;
	private static final int ASTEROID_TYPES = 5;
	private static final int ASTEROID_SCORE = 5;
	private static final double PROB_CRYSTAL = .3;

	private int asteroidNum;

    private int nSpin;

	public Asteroid(){
		
		//call Sprite constructor
		super();
		setTeam(Team.FOE);
		setHealth(Game.R.nextInt(MAX_HEALTH));
		setSpriteScore(ASTEROID_SCORE);
		setMaxSpeed(MAX_SPEED);
		setMinSpeed(MIN_SPEED);

		int nSpin = Game.R.nextInt(MAX_SPIN);
		if(Game.R.nextDouble() < .50){
            nSpin = - nSpin;
        }
		setSpin(nSpin);
			
		//random delta-x
		int nDX = Game.R.nextInt(MAX_SPEED) + MIN_SPEED;
		if(Game.R.nextDouble() < .50){
            nDX = -nDX;
        }
		setDeltaX(nDX);
		
		//random delta-y
		int nDY = Game.R.nextInt(MAX_SPEED) + MIN_SPEED;
		if(Game.R.nextDouble() < .50) {
            nDY = -nDY;
        }
		setDeltaY(nDY);
		setAsteroidNum(Game.R.nextInt(ASTEROID_TYPES));

		if (getAsteroidNum() == 2){
			setRadius(SMALL_RADIUS);
		}
		else if (getAsteroidNum() == 4){
			setRadius(LARGE_RADIUS);
		}
		else {
			setRadius(RADIUS);
		}
	}

	@Override
	public boolean mineCrystal(){
		if (Math.random() < PROB_CRYSTAL){
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void move(){
		super.move();
		//an asteroid spins, so you need to adjust the orientation at each move()
		setOrientation(getOrientation() + getSpin());
	}

    public int getSpin() {
		return this.nSpin;
	}
	

	public void setSpin(int nSpin) {
		this.nSpin = nSpin;
	}

	public int getAsteroidNum() {
		return asteroidNum;
	}

	public void setAsteroidNum(int asteroidNum) {
		this.asteroidNum = asteroidNum;
	}

    @Override
    public void draw(Graphics g) {
		BufferedImage img = CommandCenter.getInstance().getAsteroid(asteroidNum);
		drawImage(img, g);
	}
}
