package edu.uchicago.gerber.sinistar.mvc.model;



import edu.uchicago.gerber.sinistar.mvc.controller.Game;

import java.awt.*;
import java.awt.image.BufferedImage;


public class Falcon extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================
	
	private static final double THRUST = .65;
    private static final double BRAKE = .9;
    private static final double MAX_SPEED = 25;
    private static final int FALCON_SIZE = 15;
    private static final double LIMITING_BOX = .510;
	private static final int MAX_HEALTH = 1;

	final int DEGREE_STEP = 10;
	private boolean bProtected; //for fade in and out
	private boolean bThrusting = false;
	private boolean bTurningRight = false;
	private boolean bTurningLeft = false;
    private int numSinibombs;
	private int health;
	
	// ==============================================================
	// CONSTRUCTOR 
	// ==============================================================
	
	public Falcon() {
		super();
		setTeam(Team.FRIEND);
		setColor(Color.WHITE);
		setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
		setOrientation(Game.R.nextInt(360));
		setRadius(FALCON_SIZE);
		setHealth(MAX_HEALTH);
		setProtected(true);
		setFadeValue(0);
	}
	
	
	// ==============================================================
	// METHODS 
	// ==============================================================
	@Override
	public void move() {
		super.move();


		if (bThrusting) {
			double dAdjustX = Math.cos(Math.toRadians(getOrientation()))
					* THRUST;
			double dAdjustY = Math.sin(Math.toRadians(getOrientation()))
					* THRUST;
            double potDeltaX = getDeltaX() + dAdjustX;
            double potDeltaY = getDeltaY() + dAdjustY;
            if (potDeltaX < 0) {
                setDeltaX(Math.max(potDeltaX, -MAX_SPEED));
            }
            else {
                setDeltaX(Math.min(potDeltaX, MAX_SPEED));
            }

            if (potDeltaY < 0) {
                setDeltaY(Math.max(potDeltaY, -MAX_SPEED));
            }
            else {
                setDeltaY(Math.min(potDeltaY, MAX_SPEED));
            }
        }
        else {
            setDeltaX(getDeltaX() * BRAKE);
            setDeltaY(getDeltaY() * BRAKE);
        }

		if (bTurningLeft) {

			if (getOrientation() <= 0 && bTurningLeft) {
				setOrientation(360);
			}
			setOrientation(getOrientation() - DEGREE_STEP);
		} 
		if (bTurningRight) {
			if (getOrientation() >= 360 && bTurningRight) {
				setOrientation(0);
			}
			setOrientation(getOrientation() + DEGREE_STEP);
		}

		Point pnt = getCenter();
		if (pnt.x > getDim().width * LIMITING_BOX) {
			setCenter(new Point((int) (getDim().width * LIMITING_BOX), pnt.y));
		} else if (pnt.x < getDim().width * (1 - LIMITING_BOX)) {
			setCenter(new Point((int) (getDim().width * (1 - LIMITING_BOX)), pnt.y));
		}
		pnt = getCenter();
		if (pnt.y > getDim().height * LIMITING_BOX) {
			setCenter(new Point(pnt.x, (int) (getDim().height * LIMITING_BOX)));
		} else if (pnt.y < getDim().height * (1 - LIMITING_BOX)) {
			setCenter(new Point(pnt.x, (int) (getDim().height * (1 - LIMITING_BOX))));
		}

		if (getProtected()) {
			setFadeValue(getFadeValue() + 3);
		}
		if (getFadeValue() >= 255) {
			setProtected(false);
		}

	} //end move

	public void rotateLeft() {
		bTurningLeft = true;
	}

	public void rotateRight() {
		bTurningRight = true;
	}

	public void stopRotating() {
		bTurningRight = false;
		bTurningLeft = false;
	}

	public void thrustOn() {
		bThrusting = true;
	}

	public void thrustOff() {
		bThrusting = false;
	}

	@Override
	public void draw(Graphics g) {
		BufferedImage img = CommandCenter.getInstance().getFalconImg();
		drawImage(img, g);
	}

	public void setProtected(boolean bParam) {
		if (bParam) {
			setFadeValue(0);
		}
		bProtected = bParam;
	}

	public boolean getProtected() {return bProtected;}

    public int getNumSinibombs() {
        return numSinibombs;
    }

    public void setNumSinibombs(int numSinibombs) {
        this.numSinibombs = numSinibombs;
    }

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}
} //end class
