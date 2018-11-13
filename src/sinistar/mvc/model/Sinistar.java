package sinistar.mvc.model;

import sinistar.sounds.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Created by michaelvasiliou on 11/18/16.
 */
public class Sinistar extends Sprite{
    private static final int SINISTAR_SIZE = 70;
    private static final int MAX_SPEED = 20;
    public static final int MAX_PIECES = 10;
    public static final int PIECE_SCORE = 500;
    private static final int SINISTAR_KILL_POINTS = 15000;
    private int numPieces;
    private boolean alive;
    private BufferedImage currentImg;

    public Sinistar() {
        super();
        setTeam(Team.FOE);
        setNumPieces(0);
        setMinSpeed(1);
        setMaxSpeed(MAX_SPEED);
        setColor(Color.CYAN);
        setRadius(SINISTAR_SIZE);
        setAlive(false);
        setSpriteScore(PIECE_SCORE);
    }

    public void updateByPieces() {
        currentImg = CommandCenter.getInstance().getSinistarImg(numPieces);
        if (numPieces == MAX_PIECES) {
            if (!isAlive()) {
                setAlive(true);
                Sound.playSound("alive.wav");
            }
        }
        if (numPieces <= 0 && isAlive()){
            setAlive(false);
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + SINISTAR_KILL_POINTS);
            setNumPieces(0);
            setDeltaX(0);
            setDeltaY(0);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getNumPieces() {
        return numPieces;
    }

    public void setNumPieces(int numPieces) {
        this.numPieces = numPieces;
    }

    @Override
    public void move() {
        super.move();
        updateByPieces();
        if (isAlive()) {
            lockon(CommandCenter.getInstance().getFalcon().getCenter(), MAX_SPEED);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (currentImg != null){
            drawImage(currentImg, g);
        }
    }
}