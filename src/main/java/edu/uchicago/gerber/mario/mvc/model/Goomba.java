package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/27/2016.
 */
public class Goomba extends Sprite {
    private Image imgGoomba = new ImageIcon(Sprite.strImageDir + "Goomba.gif").getImage();
    private Image imgGoombaCrushed = new ImageIcon(Sprite.strImageDir + "Goomba_crushed.gif").getImage();
    private final int HORIZONTAL_SPEED = +3;
    private int nDeadTimeLeft = 0;
    private boolean bDead;
    public static final int WORTH = +100;
    private int nWorthDeltaY = -5;
    private int nWorthY = 0;

    public Goomba(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.FOE);
        setCenter(new Point(nCenterX, nCenterY));
        setDeltaX(-HORIZONTAL_SPEED);
        setHeight(32);
        setWidth(32);
        bDead = false;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgGoomba,getCenter().x,getCenter().y,null);
        if (bDead) {
            g2d.drawString(String.format("%03d",WORTH), getCenter().x,nWorthY);
        }
    }

    @Override
    public void move(){
        setImage();
        if (!bDead) {
            super.move();
            setCenter(new Point(getCenter().x + getDeltaX(), getCenter().y));
        } else {
            nWorthY+= nWorthDeltaY;

        }

    }

    @Override
    public void setLeftDirection() {
        setDeltaX(-HORIZONTAL_SPEED);
    }

    @Override
    public void setRightDirection() {
        setDeltaX(HORIZONTAL_SPEED);
    }

    @Override
    public void setDead() {
        bDead = true;
        nDeadTimeLeft = 10;
        nWorthY = getCenter().y;
    }

    @Override
    public boolean isDead() {
        return bDead;
    }

    private void setImage() {
        if (bDead) {
            imgGoomba = imgGoombaCrushed;
            if (nDeadTimeLeft == 10) {
                setCenter(new Point(getCenter().x, getCenter().y + 16));
            }
            nDeadTimeLeft--;
        }
    }

    @Override
    public int getDeadTimeLeft() {
        return nDeadTimeLeft;
    }

    @Override
    public int getWorth() {
        return WORTH;
    }
}
