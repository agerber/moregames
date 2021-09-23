package edu.uchicago.gerber.joust.mvc.model;



import edu.uchicago.gerber.joust.mvc.controller.Game;
import edu.uchicago.gerber.joust.mvc.controller.Sound;

import javax.swing.*;
import java.awt.*;

public class Bolt extends Sprite {

    private int timeLeft = 250;
    private boolean fizzled;
    private int bounceCount;

    private Image[] boltImg = { new ImageIcon(Sprite.getImgDir() + "bolt_0.png").getImage(),
                                new ImageIcon(Sprite.getImgDir() + "bolt_1.png").getImage(),
                                new ImageIcon(Sprite.getImgDir() + "bolt_2.png").getImage(),
                                new ImageIcon(Sprite.getImgDir() + "bolt_3.png").getImage(),
                                new ImageIcon(Sprite.getImgDir() + "bolt_4.png").getImage() };

    private Image boltPose;

    private final int FLICKER_FRAMERATE = 1;

    public Bolt(Cloud cloud) {
        super(new Point(cloud.getX() + 15,cloud.getY() + 40), Team.BOLT);

        // 25 = vel_x^2 + vel_y^2
        setVx((int)(5*Math.random()));
        setVy((int)Math.sqrt(25.0-Math.pow(getVx(),2)));
        if (Math.random() <= 0.5) setVx(-getVx());
        if (Math.random() <= 0.5) setVy(-getVy());

        timeLeft = 250;
        setRadius(5);
    }

    @Override
    public void move() {

        // have the lightning bounce around the screen
        if (getPos().x <= 0) {
            setVx(Math.abs(getVx()));
            incBounceCount();
            if (CommandCenter.getInstance().getShockSoundTicker() > 3) {
                Sound.playSound("shockedQuiet.wav");
                CommandCenter.getInstance().setShockSoundTicker(0);
            }
        }
        if (getPos().x >= Game.DIM.width - 2*getRadius()) {
            setVx(-Math.abs(getVx()));
            incBounceCount();
            if (CommandCenter.getInstance().getShockSoundTicker() > 3) {
                Sound.playSound("shockedQuiet.wav");
                CommandCenter.getInstance().setShockSoundTicker(0);
            }
        }
        if (getPos().y <= 0) {
            verticalBounce();
            if (CommandCenter.getInstance().getShockSoundTicker() > 3) {
                Sound.playSound("shockedQuiet.wav");
                CommandCenter.getInstance().setShockSoundTicker(0);
            }
        }
        getPos().setLocation(getPos().getX() + getVx(), getPos().getY() + getVy());

        // fade bolt away
        setTimeLeft(getTimeLeft()-1);
        if (getTimeLeft() <= 0) setFizzled(true);
    }

    public void verticalBounce() {
        setVy(-getVy());
        incBounceCount();
    }

    @Override
    public void draw(Graphics g) {
        animTick();
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(boltPose, getX(), getY(),null);
    }

    private void setPose() {
        setBoltPose(boltImg[getLoopingAnimFrame(5, FLICKER_FRAMERATE)]);
    }

    public void checkCollision(BalloonKid bKid) {
        // bKid collision points of interest
        Point leftFoot = bKid.getLeftFootPos();
        Point rightFoot = bKid.getRightFootPos();
        Point front = bKid.getFrontPos();
        Point back = bKid.getBackPos();
        Point balPos = bKid.getBalloonPos();
        int balRad = BalloonFighter.BALLOON_RADIUS;

        if ((leftFoot.x >= getX() && leftFoot.x <= getX() + getRadius() && leftFoot.y >= getY() && leftFoot.y <= getY() + getRadius())
                || (rightFoot.x >= getX() && rightFoot.x <= getX() + getRadius() && rightFoot.y >= getY() && rightFoot.y <= getY() + getRadius())
                || (back.x >= getX() && back.x <= getX() + getRadius() && back.y - 10 >= getY() && back.y - 10 <= getY() + getRadius())
                || (front.x >= getX() && front.x <= getX() + getRadius() && front.y - 10 >= getY() && front.y - 10 <= getY() + getRadius())
                    || (getPos().distance(balPos) < balRad + getRadius()) && !bKid.isInvincible()) {
            bKid.setShocked(true);
            setFizzled(true);
        }
    }

    public Image getBoltPose() { return boltPose; }
    public void setBoltPose(Image boltPose) { this.boltPose = boltPose; }

    public int getTimeLeft() { return timeLeft; }
    public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }

    public boolean isFizzled() { return fizzled; }
    public void setFizzled(boolean fizzled) {
        this.fizzled = fizzled;
        if (fizzled) { CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE); }
    }

    private void incBounceCount() {
        bounceCount += 1;
        if (bounceCount >= 3) { setFizzled(true); }
    }
}
