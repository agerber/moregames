package joust.mvc.model;

import joust.mvc.controller.Game;
import joust.sounds.Sound;
import java.awt.*;

public abstract class BalloonFighter extends Sprite {

    private Direction direction;    // fighter facing right or left
    private boolean moving;         // fighter is moving
    private boolean airborne;       // is fighter airborne
    private boolean aboutToBeEaten; // about to be eaten by a reaper fish
    private boolean eaten;          // eaten by a reaper fish
    private boolean dead;

    // collision points
    private Point leftFootPos;
    private Point rightFootPos;
    private Point balloonPos;
    private Point frontPos;
    private Point backPos;

    // health status
    private int balloons;
    private boolean invincible;
    private int invincibilityFade;

    // current fighter image displayed
    private Image fighterPose;

    public static final int FLAP_FRAMERATE = 1;           // framerate for flapping animation
    private boolean flap;                                 // for one-off flap animation
    private int flapAnimStartFrame;                       // beginning frame of one-time flap animation

    public static final int MAX_BKID_MOVE_SPEED = 8;
    public static final int MAX_BFOX_MOVE_SPEED = 2;
    public static final int JUMP_SPEED = -11;             // negative because it's in the y-direction
    public static final int BALLOON_POP_SPEED = -12;
    public static final int FLAP_SPEED = -6;
    public static final int DEATH_FALL_SPEED = 20;       // fall speed after death
    public static final int DRAGGED_UNDER_SPEED = 28;   // dragged under by fish
    public static int MAX_FALL_SPEED = 4;
    public static final int BALLOON_RADIUS = 13;

    public static final int BKID_MAX_BALLOONS = 2;
    public static final int FOX_MAX_BALLOONS = 1;

    public static enum BumpDirection {
        RIGHT, LEFT
    }

    public BalloonFighter(Point pos, Team team) {
        super(pos, team);
        leftFootPos = new Point(0, 0);
        rightFootPos = new Point(0, 0);
        balloonPos = new Point(0, 0);
        frontPos = new Point(0, 0);
        backPos = new Point(0, 0);
        setCollisionPoints();
    }

    @Override
    public abstract void move();

    @Override
    public void draw(Graphics g) {
        if(this instanceof BalloonKid
                || (this instanceof BalloonFox && (this.isAirborne() || !CommandCenter.getInstance().isPaused()))) { animTick(); }
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(fighterPose, getX(), getY(),null);

        // watch hitboxes
        if (Game.DRAW_COLLISION_POINTS) {
            g.setColor(Color.GREEN);
            g.drawOval(getX(),getY(),2,2);                  // position animated
            g.drawOval(getLeftFootPos().x, getLeftFootPos().y, 2, 2);         // left foot position hit box
            g.drawOval(getRightFootPos().x, getRightFootPos().y, 2, 2);         // right foot position hit box
            g.drawOval(getBalloonPos().x, getBalloonPos().y, 26, 26);   // balloon position hitbox
            g.drawOval(getFrontPos().x, getFrontPos().y, 2, 2);
            g.drawOval(getBackPos().x, getBackPos().y, 2, 2);
        }
    }

    public abstract void setPose();

    public void setCollisionPoints() {
        leftFootPos.setLocation(getX() + 8, getY() + 46);
        rightFootPos.setLocation(getX() + 24, getY() + 46);
        balloonPos.setLocation(getX() + 14, getY() + 10);
        frontPos.setLocation(getX() + 24, getY() + 30);
        backPos.setLocation(getX() + 8, getY() + 30);
    }

    public void jump() {
        if (!isAirborne()) {
            setVy(JUMP_SPEED);
            setAirborne(true);
        }
        else {
            setVy(FLAP_SPEED);
            flap = true;
            flapAnimStartFrame = getAnimTicker();
        }
        if (this instanceof BalloonKid) { Sound.playSound("bKidJump.wav"); }
        else if (this instanceof BalloonFox) {
            if (CommandCenter.getInstance().getbFoxJumpSoundTicker() > 1) {
                Sound.playSound("bFoxJumpQuiet.wav");
                CommandCenter.getInstance().setbFoxJumpSoundTicker(0);
            }
        }
    }

    public void popJump() {
        setVy(BALLOON_POP_SPEED);
    }

    public void sideBump(BumpDirection direction) {
        if(direction == BumpDirection.RIGHT) { setVx((int)(1.2*Math.abs(getVx()))); }
        else if (direction == BumpDirection.LEFT) { setVx((int)(-1.2*Math.abs(getVx()))); }
        if (CommandCenter.getInstance().getBumpSoundTicker() > 2) {
            Sound.playSound("bumpQuiet.wav");
            CommandCenter.getInstance().setBumpSoundTicker(0);
        }
    }

    public void balloonBump() {
        setVy(2*Math.abs(getVy()));
        if (CommandCenter.getInstance().getBumpSoundTicker() > 2) {
            Sound.playSound("bumpQuiet.wav");
            CommandCenter.getInstance().setBumpSoundTicker(0);
        }
    }

    public void popBalloon() {
        balloons -= 1;
        PoppedBalloon poppedBalloon = new PoppedBalloon(this);
        CommandCenter.getInstance().getOpsList().enqueue(poppedBalloon, CollisionOp.Operation.ADD);
        Sound.playSound("pop.wav");
    }

    public abstract boolean isPopped();
    public abstract void setPopped(boolean popped);

    // balloon, popped, and zapped getters and setters
    public int getBalloons() { return balloons; }
    public void setBalloons(int balloons) { this.balloons = balloons; }

    public boolean isFlap() { return flap; }
    public void setFlap(boolean flap) { this.flap = flap; }

    // collision points
    public Point getLeftFootPos() { return leftFootPos; }
    public Point getRightFootPos() { return rightFootPos; }
    public Point getBalloonPos() { return balloonPos; }
    public Point getFrontPos() { return frontPos; }
    public Point getBackPos() { return backPos; }

    public void setFighterPose(Image fighterPose) { this.fighterPose = fighterPose; }

    public int getFlapAnimStartFrame() { return flapAnimStartFrame; }

    public boolean isAirborne() { return airborne; }
    public void setAirborne(boolean airborne) {
        this.airborne = airborne;
        if (this instanceof BalloonFox && !airborne) {
            BalloonFox bFox = (BalloonFox)(this);
            bFox.setParachuting(false);
            bFox.setPumpAnimStartFrame(getAnimTicker());
        }
    }

    public boolean isMoving() { return moving; }
    public void setMoving(boolean moving) { this.moving = moving; }

    public boolean isDead() { return dead; }
    public void setDead(boolean dead) {
        this.dead = dead;
        if (dead) {
            if (this instanceof BalloonKid) {
                CommandCenter.getInstance().setbKidDead(true);
                ScorePop scorePop = new ScorePop(new Point(getX() + 20, getY()), BalloonKid.DEAD_POINT_LOSS);
                CommandCenter.getInstance().getOpsList().enqueue(scorePop, CollisionOp.Operation.ADD);
            }
            if (this instanceof BalloonFox) {
                ScorePop scorePop = new ScorePop(new Point(getX() + 20, getY()), BalloonFox.POINTS);
                CommandCenter.getInstance().getOpsList().enqueue(scorePop, CollisionOp.Operation.ADD);
            }
        }
    }

    public boolean isAboutToBeEaten() { return aboutToBeEaten; }
    public void setAboutToBeEaten(boolean aboutToBeEaten) { this.aboutToBeEaten = aboutToBeEaten; }

    public boolean isEaten() { return eaten; }
    public void setEaten(boolean eaten) {
        this.eaten = eaten;
        if (eaten) {
            setAboutToBeEaten(false);
            setDead(true);
        }
    }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public boolean isInvincible() { return invincible; }
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
        if (invincible) invincibilityFade = 0;
    }

    public void checkInvincibility() {
        invincibilityFade += 1;
        if (invincibilityFade == 25) {
            setInvincible(false);
        }
    }
}
