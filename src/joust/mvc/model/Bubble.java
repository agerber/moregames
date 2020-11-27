package joust.mvc.model;

import joust.mvc.controller.Game;
import joust.sounds.Sound;

import javax.swing.*;
import java.awt.*;

public class Bubble extends Sprite {

    private boolean burst;
    private int burstTimer; // show bursted bubble for a few frames
    private int sinCenter;
    private final int RISE_SPEED = -3;
    private final double SWAY_SPEED = 8.0;     // smaller is faster
    private final int SWAY_WIDTH = 15;
    public static final int POINTS = 200;

    // wiggling bubble images
    private Image[] bubbleWiggleImg = { new ImageIcon(Sprite.getImgDir() + "bubble_0.png").getImage(),
                                    new ImageIcon(Sprite.getImgDir() + "bubble_1.png").getImage(),
                                    new ImageIcon(Sprite.getImgDir() + "bubble_2.png").getImage() };
    // bursted bubble image
    private Image bubbleBurstImg = new ImageIcon(Sprite.getImgDir() + "bubbleBurst.png").getImage();

    private Image bubblePose;

    public Bubble() {
        super(new Point((int)(Game.DIM.width*Math.random()), Game.DIM.height + 20), Team.BUBBLE);
        sinCenter = getX();
        setRadius(15);
    }

    @Override
    public void move() {

        animTick();
        // value between sin(-pi) to sin(pi)
        int sinWave = (int)(SWAY_WIDTH*Math.sin(getAnimTicker()/SWAY_SPEED));

        if (!burst) {
            setVy(RISE_SPEED);
            getPos().setLocation(sinWave + sinCenter, getPos().getY() + getVy());
        } else {
            setBurstTimer(getBurstTimer() + 1);
        }

        // remove when floated offscreen
        if (getPos().y <= -10) { CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE); }
    }

    @Override
    public void draw(Graphics g) {
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(bubblePose, getX(), getY(),null);
    }

    public void setPose() {
        if (!burst) {
            setBubblePose(bubbleWiggleImg[getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]);
        }
        else {
            setBubblePose(bubbleBurstImg);
        }
    }

    public void checkCollision(BalloonKid bKid) {
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
                || (getPos().distance(balPos) < balRad + getRadius())) {
            setBurst(true);
        }
    }

    public boolean isBurst() { return burst; }
    public void setBurst(boolean burst) {
        this.burst = burst;
        if (burst) {
            ScorePop scorePop = new ScorePop(new Point(getX() + 20, getY()), POINTS);
            CommandCenter.getInstance().getOpsList().enqueue(scorePop, CollisionOp.Operation.ADD);
        }
    }

    public Image getBubblePose() { return bubblePose; }
    public void setBubblePose(Image bubblePose) { this.bubblePose = bubblePose; }

    public int getBurstTimer() { return burstTimer; }
    public void setBurstTimer(int burstTimer) { this.burstTimer = burstTimer; }
}
