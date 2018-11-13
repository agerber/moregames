package sinistar.mvc.model;

import sinistar.mvc.controller.Game;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by michaelvasiliou on 11/18/16.
 */
public class TurretShip extends DestroyableFoe {

    private static final int TURRET_SIZE = 15;
    private static final int MAX_HEALTH = 1;
    private static final int MAX_SPEED = 20;
    private static final int MIN_SPEED = 3;
    private static final double PROB_TURN = .25;
    private static final int TURN_THRESH = 20;
    public static final int MAX_TURRETS = 40;
    private static final int FRAMES_BETWEEN_BULLETS = 3;
    private static final double FIRING_PROB = .05;
    private static final int TURRET_SCORE = 500;
    private static final int LOCK_ON_DISTANCE = 300;
    private static final double PROB_LOCKON = .05;
    private static boolean lockedOnTurret = false;
    private int framesSinceLastBullet;
    private int framesSinceTurn;
    private boolean firing;
    private Point lockon_point;

    public TurretShip() {
        super();
        setTeam(Team.FOE);
        setSpriteScore(TURRET_SCORE);
        setHealth(MAX_HEALTH);
        setFramesSinceLastBullet(0);
        setFramesSinceTurn(0);
        setFiring(false);
        setColor(Color.CYAN);
        //with random orientation
        setOrientation(Game.R.nextInt(360));
        //this is the size of the falcon
        setRadius(TURRET_SIZE);
        setMaxSpeed(MAX_SPEED);
        setMinSpeed(MIN_SPEED);
    }


    @Override
    public void move(){
        super.move();
        setFramesSinceTurn(getFramesSinceTurn() + 1);
        if (getCenter().distance(CommandCenter.getInstance().getFalcon().getCenter()) < LOCK_ON_DISTANCE) {
            if (lockon_point == null) {
                if (Game.R.nextDouble() < PROB_LOCKON && !hasLockedOnTurret()){
                    setLockedOnTurret(true);
                    int xLock = Game.R.nextInt(100) + 100;
                    int yLock = Game.R.nextInt(100) + 100;
                    if (getCenter().getX() < CommandCenter.getInstance().getFalcon().getCenter().getX()) {
                        xLock = -xLock;
                    }
                    if (getCenter().getY() < CommandCenter.getInstance().getFalcon().getCenter().getY()){
                        yLock = -yLock;
                    }
                    lockon_point = new Point(CommandCenter.getInstance().getFalcon().getCenter().x + xLock, CommandCenter.getInstance().getFalcon().getCenter().y + yLock);
                }
            }
            else if (lockon_point.distance(getCenter()) > 50){
                lockon(lockon_point, MAX_SPEED);
            }
            else {
                setDeltaX(0);
                setDeltaY(0);
            }
            double falX = CommandCenter.getInstance().getFalcon().getCenter().getX() - getCenter().getX();
            double falY = CommandCenter.getInstance().getFalcon().getCenter().getY() - getCenter().getY();
            double falTheta = Math.atan2(falY, falX);
            setOrientation((int) ((180 / Math.PI) * Math.atan2(MAX_SPEED * Math.sin(falTheta),MAX_SPEED * Math.cos(falTheta))));
        }

        else if (framesSinceTurn >= TURN_THRESH && Game.R.nextDouble() < PROB_TURN) {
            if (lockon_point != null){
                setLockedOnTurret(false);
            }
            lockon_point = null;
            setFramesSinceTurn(0);
            setRandomVelocity();
            setOrientation((int) ((180 / Math.PI) * Math.atan2(getDeltaY(),getDeltaX())));
        }
        else {
            lockon_point = null;
        }

        if (Game.R.nextDouble() < FIRING_PROB) {
            setFiring(true);
        }
        else {
            setFiring(false);
        }
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage img = CommandCenter.getInstance().getTurret();
        drawImage(img, g);
    }

    public int getFramesSinceTurn() {
        return framesSinceTurn;
    }

    public void setFramesSinceTurn(int framesSinceTurn) {
        this.framesSinceTurn = framesSinceTurn;
    }

    public int getFramesSinceLastBullet() {
        return framesSinceLastBullet;
    }

    public int getFramesBetweenBullets() {
        return FRAMES_BETWEEN_BULLETS;
    }

    public void setFramesSinceLastBullet(int framesSinceLastBullet) {
        this.framesSinceLastBullet = framesSinceLastBullet;
    }

    public boolean isFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }

    public static boolean hasLockedOnTurret() {
        return lockedOnTurret;
    }

    public static void setLockedOnTurret(boolean lockedOnTurret) {
        TurretShip.lockedOnTurret = lockedOnTurret;
    }

    public Point getLockon_point() {
        return lockon_point;
    }
}
