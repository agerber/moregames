package joust.mvc.model;

import javax.swing.*;
import java.awt.*;

public class PoppedBalloon extends Sprite {

    Image poppedBalloonImg = new ImageIcon(Sprite.getImgDir() + "poppedBalloon.png").getImage();

    PoppedBalloon(BalloonFighter bFighter) {
        super(new Point(bFighter.getX() + 5, bFighter.getY()), Team.DEBRIS);
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        animTick();
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(poppedBalloonImg, getX(), getY(),null);
        if (getAnimTicker() >= 3) CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
    }
}
