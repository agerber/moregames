package joust.mvc.model;

import joust.mvc.controller.Game;

import javax.swing.*;
import java.awt.*;

public class ReaperFish extends Sprite {

    // wiggling bubble images
    private Image[] fishBreachImg = { new ImageIcon(Sprite.getImgDir() + "fish_0.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "fish_1.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "fish_2.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "fish_3.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "fish_4.png").getImage(),
                                        new ImageIcon(Sprite.getImgDir() + "fish_5.png").getImage() };

    private Image fishPose;

    private BalloonFighter fishFood;
    private boolean breaching;
    private final int BREACH_FRAMERATE = 4;
    private int breachAnimStartFrame;


    public ReaperFish(BalloonFighter fishFood) {
        super(new Point(fishFood.getX(), Game.DIM.height - 75), Team.FISH);
        breachAnimStartFrame = getAnimTicker();
        this.fishFood = fishFood;
        setBreaching(true);
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        if(!CommandCenter.getInstance().isPaused()) { animTick(); }
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(fishPose, getX(), getY(),null);
    }

    public void setPose() {
        if (breaching) {
            setFishPose(fishBreachImg[getOneTimeAnimFrame(6, BREACH_FRAMERATE, breachAnimStartFrame)]);

            // pull bFighter under the water at frame 3
            if (!fishFood.isEaten() && getOneTimeAnimFrame(6, BREACH_FRAMERATE, breachAnimStartFrame) == 2) {
                fishFood.setEaten(true);
            }
            if (getOneTimeAnimFrame(6, BREACH_FRAMERATE, breachAnimStartFrame) == 5) {
                setBreaching(false);
                CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
            }
        }
    }

    public void setBreachAnimStartFrame(int breachAnimStartFrame) { this.breachAnimStartFrame = breachAnimStartFrame; }

    public Image getFishPose() { return fishPose; }
    public void setFishPose(Image fishPose) { this.fishPose = fishPose; }

    public boolean isBreaching() { return breaching; }
    public void setBreaching(boolean breaching) { this.breaching = breaching; }
}
