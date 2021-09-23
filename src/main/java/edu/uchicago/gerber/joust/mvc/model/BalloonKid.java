package joust.mvc.model;

import joust.mvc.controller.Game;
import joust.sounds.Sound;

import javax.swing.*;
import java.awt.*;

public class BalloonKid extends BalloonFighter {

    // life statuses
    private boolean shocked;
    private boolean popped;

    ////Balloon Kid images. [0] is with 1 balloon, [1] is with 2 balloons
    // stationary facing left
    private Image[][] bKidStatL = { { new ImageIcon(Sprite.getImgDir() + "bKidStatL1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatL1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatL1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidStatL2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatL2_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatL2_2.png").getImage() } };
    // stationary facing right
    private Image[][] bKidStatR = { { new ImageIcon(Sprite.getImgDir() + "bKidStatR1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatR1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatR1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidStatR2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatR2_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidStatR2_2.png").getImage() } };

    // running facing left
    private Image[][] bKidRunL = { { new ImageIcon(Sprite.getImgDir() + "bKidRunL1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunL1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunL1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidRunL2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunL2_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunL2_2.png").getImage() } };
    // running facing right
    private Image[][] bKidRunR = { { new ImageIcon(Sprite.getImgDir() + "bKidRunR1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunR1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunR1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidRunR2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunR2_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidRunR2_2.png").getImage() } };

    // airborne facing left
    private Image[][] bKidAirL = { { new ImageIcon(Sprite.getImgDir() + "bKidAirL1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirL1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirL1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidAirL2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirL2_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirL2_2.png").getImage() } };
    // airborne facing right
    private Image[][] bKidAirR = { { new ImageIcon(Sprite.getImgDir() + "bKidAirR1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirR1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirR1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidAirR2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirR2_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidAirR2_2.png").getImage() } };

    // airborne flap facing left
    private Image[][] bKidFlapL = { { new ImageIcon(Sprite.getImgDir() + "bKidFlapL1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidFlapL1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidFlapL1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidFlapL2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidFlapL2_1.png").getImage(),
                                            new ImageIcon(Sprite.getImgDir() + "bKidFlapL2_2.png").getImage() } };
    // airborne flap facing right
    private Image[][] bKidFlapR = { { new ImageIcon(Sprite.getImgDir() + "bKidFlapR1_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidFlapR1_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidFlapR1_2.png").getImage() },
                                    { new ImageIcon(Sprite.getImgDir() + "bKidFlapR2_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "bKidFlapR2_1.png").getImage(),
                                            new ImageIcon(Sprite.getImgDir() + "bKidFlapR2_2.png").getImage() } };

    // popped
    private Image bKidPopped[] = {new ImageIcon(Sprite.getImgDir() + "bKidPopped_0.png").getImage(),
                                    new ImageIcon(Sprite.getImgDir() + "bKidPopped_1.png").getImage(),
                                    new ImageIcon(Sprite.getImgDir() + "bKidPopped_2.png").getImage()};
    // zapped
    private Image bKidShocked = new ImageIcon(Sprite.getImgDir() + "bKidShocked.png").getImage();

    private final int RUN_FRAMERATE = 2;            // framerate for running animation

    public static final int DEAD_POINT_LOSS = -1000;
    public static final int STARTING_X = 30;
    public static final int STARTING_Y = Game.DIM.height - 130;


    public BalloonKid() {
        super(new Point(STARTING_X,STARTING_Y), Team.BKID);
        resetBKid();
    }

    public void resetBKid() {
        setDirection(Direction.RIGHT);
        setBalloons(BKID_MAX_BALLOONS);
        setVx(0);
        setVy(0);
        setPos(new Point(STARTING_X,STARTING_Y));
        setCollisionPoints();
        setAirborne(false);
        setMoving(false);
        setDead(false);
        setEaten(false);
        setAboutToBeEaten(false);
        setPopped(false);
        setShocked(false);
    }

    @Override
    public void move() {
        if (!isDead() && !isEaten()) {
            // set vertical speed
            if (!isAirborne()) setVy(0);
            else if (isAirborne()) {
                setVy(getVy() + Movable.Y_ACCEL_RATE);
                if (getVy() > MAX_FALL_SPEED) { setVy(MAX_FALL_SPEED); }
            }

            // set horizontal speed
            if (!isMoving()) setVx(0);
            else if (getDirection() == Direction.RIGHT) setVx(Math.min(MAX_BKID_MOVE_SPEED, getVx() + Movable.X_ACCEL_RATE));      // accelerate by 1 per frame
            else if (getDirection() == Direction.LEFT) setVx(Math.max(-MAX_BKID_MOVE_SPEED, getVx() - Movable.Y_ACCEL_RATE)); // accererate by 1 per frame
        }
        else if (isEaten()) {
            setVx(0);
            setVy(DRAGGED_UNDER_SPEED);
        }
        else {
            setVx(0);
            setVy(Math.min(DEATH_FALL_SPEED,getVy()+1));
        }
        // keep bKid within the bounds of the screen at all times
        int newX;
        int bKidPoseWidth = 10;
        if (getPos().getX() + getVx() < -3*bKidPoseWidth) newX = Game.DIM.width - bKidPoseWidth;
        else if (getPos().getX() + getVx() > Game.DIM.width + bKidPoseWidth) newX = -2*bKidPoseWidth;
        else newX = (int)getPos().getX() + getVx();

        // don't let bKid go above height of the screen
        if (getPos().getY() + getVy() <= 0) balloonBump();

        getPos().setLocation(newX, getPos().getY() + getVy());
        setCollisionPoints();
        if (isInvincible()) checkInvincibility();
    }

    public void setPose() {

        int balloonInd = Math.max(0,getBalloons()-1);        // protect against ArrayOutOfBounds
        if (!isPopped() && !shocked) {
            if (!isAirborne()) {
                if (getDirection() == Direction.RIGHT) {
                    if (getVx() == 0) { setFighterPose(bKidStatR[balloonInd][getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]); }
                    else { setFighterPose(bKidRunR[balloonInd][getLoopingAnimFrame(3,RUN_FRAMERATE)]); }
                }
                else {
                    if (getVx() == 0) { setFighterPose(bKidStatL[balloonInd][getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]); }
                    else { setFighterPose(bKidRunL[balloonInd][getLoopingAnimFrame(3, RUN_FRAMERATE)]); }
                }
            }
            else if (isAirborne()) {
                if (getDirection() == Direction.RIGHT) {
                    if (!isFlap()) { setFighterPose(bKidAirR[balloonInd][getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]); }
                    else if (isFlap()) { setFighterPose(bKidFlapR[balloonInd][getLoopingAnimFrame(3, FLAP_FRAMERATE)]); }
                }
                else if (getDirection() == Direction.LEFT) {
                    if (!isFlap()) { setFighterPose(bKidAirL[balloonInd][getLoopingAnimFrame(3, ORGANIC_FRAMERATE)]); }
                    else if (isFlap()) { setFighterPose(bKidFlapL[balloonInd][getOneTimeAnimFrame(3, FLAP_FRAMERATE, getFlapAnimStartFrame())]); }
                }
                if (isFlap() && getOneTimeAnimFrame(3,FLAP_FRAMERATE, getFlapAnimStartFrame()) == 2) { setFlap(false); }      // flap animation is complete
            }
        }
        else if (isPopped()) { setFighterPose(bKidPopped[getLoopingAnimFrame(3, FLAP_FRAMERATE)]); }
        else if (shocked) { setFighterPose(bKidShocked); }
    }

    @Override
    public boolean isPopped() { return popped; }
    @Override
    public void setPopped(boolean popped) {
        this.popped = popped;
        if (popped) {
            setVy(-8);
            Sound.playSound("bKidDownFastQuiet.wav");
            setDead(true);
        }
    }

    // zapped by lightning
    public boolean isShocked() { return shocked; }
    public void setShocked(boolean shocked) {
        this.shocked = shocked;
        if (shocked) {
            Sound.playSound("bKidDownFastQuiet.wav");
            setDead(true);
        }
    }
}
