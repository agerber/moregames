package edu.uchicago.gerber.joust.mvc.model;



import edu.uchicago.gerber.joust.mvc.controller.Sound;

import java.awt.*;

public abstract class Platform extends Sprite {

    private Point hitBoxTopLeftCorner;
    private Point hitBoxBottomRightCorner;
    private int blockWidth;
    private int blockHeight;

    public Platform(Point pos, int blockWidth, int blockHeight) {
        super(pos, Team.PLATFORM);
        this.blockHeight = blockHeight;
        this.blockWidth = blockWidth;
        updateHitBoxBottomRightCorner();
        updateHitBoxTopLeftCorner();
        setTeam(Team.PLATFORM);
    }

    @Override
    public void move() { }

    @Override
    public abstract void draw(Graphics g);

    public void checkSideBump(Movable mov) {
        if (mov instanceof BalloonFighter) {
            // bFighter collision points of interest
            BalloonFighter bFighter = (BalloonFighter)(mov);
            Point front = bFighter.getFrontPos();
            Point back = bFighter.getBackPos();
            Point bal = bFighter.getBalloonPos();
            int balRight = bal.x + BalloonKid.BALLOON_RADIUS;   // right side of the balloon
            int balLeft = bal.x - BalloonKid.BALLOON_RADIUS;    // left side of the balloon

            if ((front.x >= hitBoxTopLeftCorner.x && front.x <= hitBoxTopLeftCorner.x + 10
                    && front.y >= hitBoxTopLeftCorner.y && front.y <= hitBoxBottomRightCorner.y)
                    || (balRight >= hitBoxTopLeftCorner.x && balRight <= hitBoxTopLeftCorner.x + 10
                    && bal.y >= hitBoxTopLeftCorner.y && bal.y <= hitBoxBottomRightCorner.y)) { bFighter.sideBump(BalloonFighter.BumpDirection.LEFT); }
            else if ((back.x >= hitBoxBottomRightCorner.x - 10 && back.x <= hitBoxBottomRightCorner.x
                    && back.y >= hitBoxTopLeftCorner.y && back.y <= hitBoxBottomRightCorner.y)
                    || (balLeft >= hitBoxBottomRightCorner.x - 10 && balLeft <= hitBoxBottomRightCorner.x
                    && bal.y >= hitBoxTopLeftCorner.y && bal.y <= hitBoxBottomRightCorner.y)) { bFighter.sideBump(BalloonFighter.BumpDirection.RIGHT); }
        }
    }

    public void checkBalloonBump(Movable mov) {
        if (mov instanceof BalloonFighter) {
            // bFighter collision points of interest
            BalloonFighter bFighter = (BalloonFighter)(mov);
            Point bal = bFighter.getBalloonPos();
            int balTop = bal.y - BalloonKid.BALLOON_RADIUS;     // top of the balloon
            int balRight = bal.x + BalloonKid.BALLOON_RADIUS;   // right side of the balloon
            int balLeft = bal.x - BalloonKid.BALLOON_RADIUS;    // left side of the balloon

            if (balTop <= getHitBoxBottomRightCorner().y && balTop >= getHitBoxBottomRightCorner().y - 10
                && balRight >= getHitBoxTopLeftCorner().x && balLeft <= getHitBoxBottomRightCorner().x) { bFighter.balloonBump(); }
        }
    }

    public boolean checkLand(Movable mov) {
        if (mov instanceof BalloonFighter) {
            BalloonFighter bFighter = (BalloonFighter)(mov);
            Point leftFoot = bFighter.getLeftFootPos();
            Point rightFoot = bFighter.getRightFootPos();
            if ((rightFoot.x > hitBoxTopLeftCorner.x && leftFoot.x < hitBoxBottomRightCorner.x
                    && leftFoot.y >= hitBoxTopLeftCorner.y && leftFoot.y <= hitBoxTopLeftCorner.y + 20)) { return true; }
            else return false;
        }
        else return false;
    }

    public void checkBoltBump(Bolt bolt) {
        Point boltCenter = new Point(bolt.getX() + bolt.getRadius()/2, bolt.getY() + bolt.getRadius()/2);

        if (boltCenter.x >= getHitBoxTopLeftCorner().x && boltCenter.x <= getHitBoxBottomRightCorner().x
            && boltCenter.y >= getHitBoxTopLeftCorner().y && boltCenter.y <= getHitBoxBottomRightCorner().y) {
            bolt.verticalBounce();
            if (CommandCenter.getInstance().getShockSoundTicker() > 3) {
                Sound.playSound("shockedQuiet.wav");
                CommandCenter.getInstance().setShockSoundTicker(0);
            }        }
    }

    public Point getHitBoxTopLeftCorner() { return hitBoxTopLeftCorner; }
    public Point getHitBoxBottomRightCorner() { return hitBoxBottomRightCorner; }
    public void updateHitBoxTopLeftCorner() {
        hitBoxTopLeftCorner = getPos();
    }
    public void updateHitBoxBottomRightCorner() {
        hitBoxBottomRightCorner = new Point(getPos().x + blockWidth, getPos().y + blockHeight);
    }
}
