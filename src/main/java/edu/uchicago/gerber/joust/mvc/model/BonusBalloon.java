package joust.mvc.model;

import joust.mvc.controller.Game;

import javax.swing.*;
import java.awt.*;

public class BonusBalloon extends Sprite {
    private boolean burst;
    private int burstTimer; // show bursted bubble for a few frames
    private int sinCenter;
    private final int RISE_SPEED = -3;
    private final double SWAY_SPEED = 13.0;     // smaller is faster
    private final int SWAY_WIDTH = 4;
    public static final int POINTS = 400;
    private Direction direction;

    // wiggling bubble images
    private Image balloonRImg = new ImageIcon(Sprite.getImgDir() + "bonusBalloonR.png").getImage();
    private Image balloonLImg = new ImageIcon(Sprite.getImgDir() + "bonusBalloonL.png").getImage();

    // bursted balloon image
    private Image balloonBurstImg = new ImageIcon(Sprite.getImgDir() + "poppedBalloon.png").getImage();

    private Image balloonPose;

    public BonusBalloon(Pipe pipe, int y) {
        super(new Point((int)(pipe.getPos().x + 6), y), Team.BONUSBALLOON);
        sinCenter = getX();
        direction = Direction.RIGHT;
        setRadius(12);
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
        if (Math.sin(getAnimTicker()/SWAY_SPEED - Math.sin(getAnimTicker() + 1)/SWAY_SPEED) <= 0) { direction = Direction.RIGHT; }
        else { direction = Direction.LEFT; }

        // remove when floated offscreen
        if (getPos().y <= -10) { CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE); }
    }

    @Override
    public void draw(Graphics g) {
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(balloonPose, getX(), getY(),null);
    }

    public void setPose() {
        if (!burst) {
            setBubblePose(direction == Direction.RIGHT ? balloonRImg : balloonLImg);
        }
        else {
            setBubblePose(balloonBurstImg);
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

    public Image getBubblePose() { return balloonPose; }
    public void setBubblePose(Image bubblePose) { this.balloonPose = bubblePose; }

    public int getBurstTimer() { return burstTimer; }
    public void setBurstTimer(int burstTimer) { this.burstTimer = burstTimer; }
}
