package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 6/3/2016.
 */
public class Koopa extends Sprite {

    private Image imgKoopa;
    private Image imgKoopaLeft = new ImageIcon(Sprite.strImageDir + "Green_Koopa_Left.gif").getImage();
    private Image imgKoopaRight = new ImageIcon(Sprite.strImageDir + "Green_Koopa_Right.gif").getImage();
    private Image imgKoopaShell = new ImageIcon(Sprite.strImageDir + "Green_Koopa Troopa_Shell1.gif").getImage();
    private final int HORIZONTAL_SPEED = +4; //Steps per frame
    private int nDeadTimeLeft = 0;
    private boolean bDead, bLookingLeft;
    public static final int WORTH = +500;
    private int nWorthDeltaY = -5;
    private int nWorthY = 0;

    public Koopa(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.FOE);
        setCenter(new Point(nCenterX, nCenterY));
        setDeltaX(-HORIZONTAL_SPEED);
        setHeight(46);
        setWidth(32);
        bDead = false;
        bLookingLeft = true;
        imgKoopa = imgKoopaLeft;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgKoopa,getCenter().x,getCenter().y,null);
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
        bLookingLeft = true;
    }

    @Override
    public void setRightDirection() {
        setDeltaX(HORIZONTAL_SPEED);
        bLookingLeft = false;
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
            imgKoopa = imgKoopaShell;
            if (nDeadTimeLeft == 10) {
                setCenter(new Point(getCenter().x, getCenter().y + 16));
            }
            nDeadTimeLeft--;
        }
        else if (bLookingLeft) {
            imgKoopa = imgKoopaLeft;
        } else {
            imgKoopa = imgKoopaRight;
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
