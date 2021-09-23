package edu.uchicago.gerber.joust.mvc.model;



import edu.uchicago.gerber.joust.mvc.controller.Game;
import edu.uchicago.gerber.joust.mvc.controller.Sound;

import javax.swing.*;
import java.awt.*;

public class BalloonFox extends BalloonFighter {

    public static enum Behavior {
        SEEKER, DRIFTER
    }

    // life statuses
    private boolean popped;
    private boolean parachuting;
    private boolean parachuteUnfolding;         // boolean for unfolding parachute one-time animation
    private int parachuteAnimStartFrame;        // starting animTick for parachute one-time animation
    private int driftTick;
    private int driftTime;
    private Behavior behavior;

    private int pumpAnimStartFrame;

    ////Balloon Fox images
    // pumping facing left
    private Image[] bFoxPumpL = { new ImageIcon(Sprite.getImgDir() + "bFoxPumpDL_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpUL_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpDL_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpUL_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpDL_2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpUL_2.png").getImage() };
    // pumping facing right
    private Image[] bFoxPumpR = { new ImageIcon(Sprite.getImgDir() + "bFoxPumpDR_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpUR_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpDR_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpUR_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpDR_2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPumpUR_2.png").getImage() };

    // parachuting facing left
    private Image[] bFoxParaL = { new ImageIcon(Sprite.getImgDir() + "bFoxParaL_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxParaL_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxParaL_2.png").getImage() };
    // parachuting facing right
    private Image[] bFoxParaR = { new ImageIcon(Sprite.getImgDir() + "bFoxParaR_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxParaR_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxParaR_2.png").getImage() };

    // airborne facing left
    private Image[] bFoxAirL = { new ImageIcon(Sprite.getImgDir() + "bFoxAirL_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxAirL_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxAirL_2.png").getImage() };
    // airborne facing right
    private Image[] bFoxAirR = { new ImageIcon(Sprite.getImgDir() + "bFoxAirR_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxAirR_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxAirR_2.png").getImage() };

    // airborne flap facing left
    private Image[] bFoxFlapL = { new ImageIcon(Sprite.getImgDir() + "bFoxFlapL_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxFlapL_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxFlapL_2.png").getImage() };
    // airborne flap facing right
    private Image[] bFoxFlapR = { new ImageIcon(Sprite.getImgDir() + "bFoxFlapR_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxFlapR_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxFlapR_2.png").getImage() };

    // popped facing left
    private Image[] bFoxPoppedL = { new ImageIcon(Sprite.getImgDir() + "bFoxPoppedL_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPoppedL_1.png").getImage() };
    // popped facing right
    private Image[] bFoxPoppedR = { new ImageIcon(Sprite.getImgDir() + "bFoxPoppedR_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "bFoxPoppedR_1.png").getImage() };

    private final int PUMP_FRAMERATE = 17;
    private final int DEATH_FLAP_FRAMERATE = 5;
    public static final int POINTS = 500;

    public BalloonFox(Point pos, Behavior behavior) {
        super(pos, Team.BFOX);
        setDirection(Direction.LEFT);
        setBehavior(behavior);
        setBalloons(FOX_MAX_BALLOONS);
        setVx(0);
        setVy(0);
        setCollisionPoints();
        setAirborne(false);
        setMoving(false);
        setDead(false);
        setPopped(false);
        setPumpAnimStartFrame(getAnimTicker());
    }

    @Override
    public void move() {
        if (!isDead()) {
            if (!isAirborne()) {
                setVx(0);
                setVy(0);
            }
            else {
                if (parachuteUnfolding) {
                    setVy(5);
                }
                else if (parachuting) {
                    setVy(3);
                    if (getDirection() == Direction.LEFT) setVx(-2);
                    else if (getDirection() == Direction.RIGHT) setVx(2);
                }
                else {
                    // Seekers always move towards bKid
                    if (behavior == Behavior.SEEKER) {
                        BalloonKid bKid = CommandCenter.getInstance().getBKid();
                        if (bKid != null) {
                            if (bKid.getBalloonPos().x > getBalloonPos().x) {
                                setVx(Math.min(MAX_BFOX_MOVE_SPEED, getVx() + Movable.X_ACCEL_RATE));
                                setDirection(Direction.RIGHT);
                            } else if (bKid.getBalloonPos().x < getBalloonPos().x) {
                                setVx(Math.min(-MAX_BFOX_MOVE_SPEED, getVx() + Movable.X_ACCEL_RATE));
                                setDirection(Direction.LEFT);
                            } else setVx(0);
                        }
                    }
                    // Drifters float mindlessly from side to side
                    else if (behavior == Behavior.DRIFTER) {
                        driftTicker();
                        if (driftTick >= driftTime) {
                            driftTick = 0;
                            driftTime = (int)(150*Math.random());
                            if (getDirection() == Direction.RIGHT) {
                                setDirection(Direction.LEFT);
                                setVx(-MAX_BFOX_MOVE_SPEED);
                            }
                            else {
                                setDirection(Direction.RIGHT);
                                setVx(MAX_BFOX_MOVE_SPEED);
                            }
                        }
                    }

                    if ((Math.random() > 0.90 || getY() > Game.DIM.height-150) && getY() > 120) { jump(); }
                    else setVy(Math.min(MAX_FALL_SPEED, getVy() + Movable.Y_ACCEL_RATE));
                }
            }
        }
        else if (isEaten()) {
            setVx(0);
            setVy(DRAGGED_UNDER_SPEED);
        }
        else {
            setVx(0);
            setVy(Math.min(DEATH_FALL_SPEED,getVy()+1));
        }

        // keep bFox within the bounds of the screen at all times
        int newX;
        int bFoxPoseWidth = 10;
        if (getPos().getX() + getVx() < -3*bFoxPoseWidth) newX = Game.DIM.width - bFoxPoseWidth;
        else if (getPos().getX() + getVx() > Game.DIM.width + bFoxPoseWidth) newX = -2*bFoxPoseWidth;
        else newX = (int)getPos().getX() + getVx();

        // don't let bFox go above height of the screen
        if (getPos().getY() + getVy() <= 0) balloonBump();

        getPos().setLocation(newX, getPos().getY() + getVy());
        setCollisionPoints();
        if (isInvincible()) checkInvincibility();
    }

    @Override
    public void setPose() {
        if (!isPopped()) {
            if (!isAirborne()) {
                if (getDirection() == Direction.RIGHT) {
                    // pumping right animation
                    setFighterPose(bFoxPumpR[getOneTimeAnimFrame(6, PUMP_FRAMERATE, pumpAnimStartFrame)]);
                    // when the pumping animation is complete, send the fox airborne
                    if (getOneTimeAnimFrame(6, PUMP_FRAMERATE, pumpAnimStartFrame) == 5) liftoff();
                }
                else if (getDirection() == Direction.LEFT) {
                    // pumping left animation
                    setFighterPose(bFoxPumpL[getOneTimeAnimFrame(6, PUMP_FRAMERATE, pumpAnimStartFrame)]);
                    // when pumping animation is complete, send the fox airborne
                    if (getOneTimeAnimFrame(6, PUMP_FRAMERATE, pumpAnimStartFrame) == 5) liftoff();
                }
            }
            else if (isAirborne()) {
                if (getDirection() == Direction.RIGHT) {
                    if (parachuting) {
                        if (!parachuteUnfolding) setFighterPose(bFoxParaR[2]);
                        else {
                            setFighterPose(bFoxParaR[getOneTimeAnimFrame(3, 2, parachuteAnimStartFrame)]);
                            // when the parachute unfolding animation is complete, go to a static parachute
                            if (getOneTimeAnimFrame(3, 2, parachuteAnimStartFrame) == 2) setParachuteUnfolding(false);
                        }
                    }
                    else {
                        if (!isFlap()) { setFighterPose(bFoxAirR[getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]); }
                        else if (isFlap()) { setFighterPose(bFoxFlapR[getLoopingAnimFrame(2, FLAP_FRAMERATE)]); }
                    }
                }
                else if (getDirection() == Direction.LEFT) {
                    if (parachuting) {
                        if (!parachuteUnfolding) setFighterPose(bFoxParaL[2]);
                        else {
                            setFighterPose(bFoxParaL[getOneTimeAnimFrame(3, 2, parachuteAnimStartFrame)]);
                            // when the parachute unfolding animation is complete, go to a static parachute
                            if (getOneTimeAnimFrame(3, 2, parachuteAnimStartFrame) == 2) setParachuteUnfolding(false);
                        }
                    }
                    else {
                        if (!isFlap()) { setFighterPose(bFoxAirL[getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]); }
                        else if (isFlap()) { setFighterPose(bFoxFlapL[getOneTimeAnimFrame(3, FLAP_FRAMERATE, getFlapAnimStartFrame())]); }
                    }
                }
                if (isFlap() && getOneTimeAnimFrame(3, FLAP_FRAMERATE, getFlapAnimStartFrame()) == 2) { setFlap(false); }      // flap animation is complete
            }
        }
        else if (isPopped()) {
            if (getDirection() == Direction.LEFT) setFighterPose(bFoxPoppedL[getLoopingAnimFrame(2, DEATH_FLAP_FRAMERATE)]);
            else if (getDirection() == Direction.RIGHT) setFighterPose(bFoxPoppedR[getLoopingAnimFrame(2, DEATH_FLAP_FRAMERATE)]);
        }
    }

    public void checkClash(BalloonKid bKid) {
        // bKid collision points of interest
        Point bKidLeftFoot = bKid.getLeftFootPos();
        Point bKidRightFoot = bKid.getRightFootPos();
        int bKidBalTop = bKid.getBalloonPos().y - BALLOON_RADIUS;
        int bKidBalRight = bKid.getBalloonPos().x + BALLOON_RADIUS;
        int bKidBalLeft = bKid.getBalloonPos().x - BALLOON_RADIUS;

        // bFox collision points of interest
        Point bFoxLeftFoot = getLeftFootPos();
        Point bFoxRightFoot = getRightFootPos();
        int bFoxBalTop = getBalloonPos().y - BALLOON_RADIUS;
        int bFoxBalRight = getBalloonPos().x + BALLOON_RADIUS;
        int bFoxBalLeft = getBalloonPos().x - BALLOON_RADIUS;
        int bFoxBottom = getRightFootPos().y;

        if (isAirborne()) {
            // bKid's balloon gets popped
            if ((bFoxLeftFoot.x >= bKidBalLeft && bFoxLeftFoot.x <= bKidBalRight
                    && bFoxLeftFoot.y >= bKidBalTop && bFoxLeftFoot.y <= bKidBalTop + 40)
                    || (bFoxRightFoot.x >= bKidBalLeft && bFoxRightFoot.x <= bKidBalRight
                    && bFoxRightFoot.y >= bKidBalTop && bFoxRightFoot.y <= bKidBalTop + 40)) {
                if(!bKid.isInvincible()) {
                    bKid.popBalloon();
                    bKid.setInvincible(true);
                }
                popJump();
            }
            // airborne bFox collides with bKid
            else if ((bKidLeftFoot.x >= bFoxBalLeft && bKidLeftFoot.x <= bFoxBalRight
                    && bKidLeftFoot.y >= bFoxBalTop && bKidLeftFoot.y <= bFoxBalTop + 40)
                    || (bKidRightFoot.x >= bFoxBalLeft && bKidRightFoot.x <= bFoxBalRight
                    && bKidRightFoot.y >= bFoxBalTop && bKidRightFoot.y <= bFoxBalTop + 40)) {
                // bFox balloon gets popped
                if (isAirborne() && !isParachuting()) {
                    popBalloon();
                    setInvincible(true);
                    bKid.popJump();
                }
                // bFox gets knocked offstage while parachuting to earth
                else if (isParachuting() && !isInvincible()) {
                    setPopped(true);
                }
            }
        }
        // bFox gets knocked off the stage when grounded
        else if (!isAirborne()) {
            if ((bKid.getBackPos().x <= getFrontPos().x && bKid.getBackPos().x >= getBackPos().x
                    && bKid.getBackPos().y >= bFoxBalTop && bKid.getBackPos().y <= bFoxBottom)
                || (bKid.getFrontPos().x <= getFrontPos().x && bKid.getFrontPos().x >= getBackPos().x
                    && bKid.getFrontPos().y >= bFoxBalTop && bKid.getFrontPos().y <= bFoxBottom)) {
                setPopped(true);
            }
        }
    }

    public void liftoff() {
        setBalloons(FOX_MAX_BALLOONS);
        setAirborne(true);
    }

    @Override
    public boolean isPopped() { return popped; }
    @Override
    public void setPopped(boolean popped) {
        this.popped = popped;
        if (popped) {
            setVy(-8);
            Sound.playSound("bFoxDown.wav");
            setDead(true);
        }
    }

    public boolean isParachuting() { return parachuting; }
    public void setParachuting(boolean parachuting) {
        this.parachuting = parachuting;
        if (parachuting) {
            setParachuteUnfolding(true);
        }
    }

    public int getParachuteAnimStartFrame() { return parachuteAnimStartFrame; }
    public void setParachuteAnimStartFrame(int parachuteAnimStartFrame) { this.parachuteAnimStartFrame = parachuteAnimStartFrame; }

    public boolean isParachuteUnfolding() { return parachuteUnfolding; }
    public void setParachuteUnfolding(boolean parachuteUnfolding) {
        this.parachuteUnfolding = parachuteUnfolding;
        if (parachuteUnfolding){
            setParachuteAnimStartFrame(getAnimTicker());
        }
    }

    public int getPumpAnimStartFrame() { return pumpAnimStartFrame; }
    public void setPumpAnimStartFrame(int pumpAnimStartFrame) { this.pumpAnimStartFrame = pumpAnimStartFrame; }

    public Behavior getBehavior() { return behavior; }
    public void setBehavior(Behavior behavior) { this.behavior = behavior; }

    private void driftTicker() { driftTick += 1; }
}
