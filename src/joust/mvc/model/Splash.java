package joust.mvc.model;

import joust.mvc.controller.Game;

import javax.swing.*;
import java.awt.*;

public class Splash extends Sprite {

    Image[] splashImg = { new ImageIcon(Sprite.getImgDir() + "splash_0.png").getImage(),
                            new ImageIcon(Sprite.getImgDir() + "splash_1.png").getImage(),
                            new ImageIcon(Sprite.getImgDir() + "splash_2.png").getImage(),
                            new ImageIcon(Sprite.getImgDir() + "splash_3.png").getImage() };
    Image splashPose;

    private final int SPLASH_FRAMERATE = 3;
    private int splashAnimStartFrame;

    public Splash(BalloonFighter bFighter) {
        super(new Point(bFighter.getX(), Game.DIM.height - 70), Team.DEBRIS);
        splashAnimStartFrame = getAnimTicker();
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        if (!CommandCenter.getInstance().isPaused()) { animTick(); }
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(splashPose, getX(), getY(),null);
    }

    public void setPose() {
        setSplashPose(splashImg[getOneTimeAnimFrame(4, SPLASH_FRAMERATE, splashAnimStartFrame)]);

        if (getOneTimeAnimFrame(4, SPLASH_FRAMERATE, splashAnimStartFrame) == 3) {
            CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
        }
    }

    public Image getSplashPose() { return splashPose; }
    public void setSplashPose(Image splashPose) { this.splashPose = splashPose; }
}
