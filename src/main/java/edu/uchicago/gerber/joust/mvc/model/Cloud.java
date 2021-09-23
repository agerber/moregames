package edu.uchicago.gerber.joust.mvc.model;


import edu.uchicago.gerber.joust.mvc.controller.Sound;

import javax.swing.*;
import java.awt.*;

public class Cloud extends Sprite {

    private boolean striking;
    private boolean boltLoosed;
    private int charge;

    Image cloudImg = new ImageIcon(Sprite.getImgDir() + "cloud.png").getImage();
    Image[] cloudLightningImg = {  new ImageIcon(Sprite.getImgDir() + "cloudPreLightning_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudPreLightning_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudPreLightning_2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudPreLightning_3.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudLightning_0.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudLightning_1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudLightning_2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudLightning_3.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "cloudLightning_4.png").getImage() };

    Image cloudPose;

    private final int LIGHTNING_STRIKE_FRAMERATE = 4;
    private int strikeAnimStartFrame;

    public Cloud(Point pos) {
        super(pos, Team.CLOUD);
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        if (!CommandCenter.getInstance().isPaused()) { animTick(); }
        setPose();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(cloudPose, getX(), getY(),null);
    }

    private void setPose() {
        if (striking) {
            setCloudPose(cloudLightningImg[getOneTimeAnimFrame(9, LIGHTNING_STRIKE_FRAMERATE, strikeAnimStartFrame)]);
            // loose a bolt of lightning at start of lightning flash animation
            if (getOneTimeAnimFrame(9, LIGHTNING_STRIKE_FRAMERATE, strikeAnimStartFrame) == 4 && !boltLoosed) {
                Bolt bolt = new Bolt(this);
                CommandCenter.getInstance().getOpsList().enqueue(bolt, CollisionOp.Operation.ADD);
                setBoltLoosed(true);

                if (CommandCenter.getInstance().getLightningSoundTicker() > 1) {
                    Sound.playSound("lightningStrike.wav");
                    CommandCenter.getInstance().setLightningSoundTicker(0);
                }
            }
            if (getOneTimeAnimFrame(9, LIGHTNING_STRIKE_FRAMERATE, strikeAnimStartFrame) == 8) {
                setStriking(false);
                setBoltLoosed(false);
            }
        }
        else {
            setCloudPose(cloudImg);
        }
    }

    public void setCloudPose(Image cloudPose) { this.cloudPose = cloudPose; }
    public Image getCloudPose() { return cloudPose; }

    public boolean isStriking() { return striking; }
    public void setStriking(boolean striking) {
        this.striking = striking;
        if (striking) strikeAnimStartFrame = getAnimTicker();
    }

    public boolean isBoltLoosed() { return boltLoosed; }
    public void setBoltLoosed(boolean boltLoosed) { this.boltLoosed = boltLoosed; }

    public int getStrikeAnimStartFrame() { return strikeAnimStartFrame; }

    public int getCharge() { return charge; }
    public void setCharge(int charge) { this.charge = charge; }
}
