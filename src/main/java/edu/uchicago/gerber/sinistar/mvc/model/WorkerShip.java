package edu.uchicago.gerber.sinistar.mvc.model;

import edu.uchicago.gerber.sinistar.mvc.controller.Game;


import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by michaelvasiliou on 11/18/16.
 */
public class WorkerShip extends DestroyableFoe {

    private static final int WORKER_SIZE = 15;
    private static final int MAX_HEALTH = 2;
    private static final int MAX_SPEED = 8;
    private static final int MIN_SPEED = 1;
    private static final double PROB_TURN = .25;
    private static final int TURN_THRESH = 20;
    public static final int MAX_WORKERS = 100;
    private static final int CRYSTAL_TRACK_DIST = 200;
    private static final int WORKER_SCORE = 150;

    private boolean crystal;
    private int framesSinceTurn;

    public WorkerShip() {
        super();
        setTeam(Team.FOE);
        setSpriteScore(WORKER_SCORE);
        setHealth(MAX_HEALTH);
        setFramesSinceTurn(0);
        setMaxSpeed(MAX_SPEED);
        setMinSpeed(MIN_SPEED);
        setRandomVelocity();
        setColor(Color.RED);
        setOrientation(Game.R.nextInt(360));
        setRadius(WORKER_SIZE);
        setCrystal(false);
    }

    @Override
    public boolean mineCrystal(){
        return hasCrystal();
    }

    @Override
    public void move(){
        super.move();
        setFramesSinceTurn(getFramesSinceTurn() + 1);
        if (framesSinceTurn >= TURN_THRESH && Game.R.nextDouble() < PROB_TURN) {
            setFramesSinceTurn(0);
            setRandomVelocity();
        }
        if (hasCrystal() && CommandCenter.getInstance().getSinistar().getNumPieces() < Sinistar.MAX_PIECES) {
            lockon(CommandCenter.getInstance().getSinistar().getCenter(), MAX_SPEED);
        }
        else {
            for (Movable crystal : CommandCenter.getInstance().getMovFloaters()){
                if (crystal instanceof Crystal) {
                    if (getCenter().distance(crystal.getCenter()) < CRYSTAL_TRACK_DIST) {
                        lockon(crystal.getCenter(), MAX_SPEED);
                    }
                }
            }
        }
        setOrientation((int) ((180 / Math.PI) * Math.atan2(getDeltaY(),getDeltaX())));
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage img;
        if (hasCrystal()){
            img = CommandCenter.getInstance().getWorkerShipCrystal();
        }
        else {
            img = CommandCenter.getInstance().getWorkerShip();
        }
        drawImage(img, g);
    }

    public int getFramesSinceTurn() {
        return framesSinceTurn;
    }

    public void setFramesSinceTurn(int framesSinceTurn) {
        this.framesSinceTurn = framesSinceTurn;
    }

    public boolean hasCrystal() {
        return crystal;
    }

    public void setCrystal(boolean hasCrystal) {
        this.crystal = hasCrystal;
    }
}