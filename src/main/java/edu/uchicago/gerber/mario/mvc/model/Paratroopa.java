package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Paratroopa is a type of Foe like Koopa but faster and is introduced in higher levels
 */
public class Paratroopa extends Sprite {

    private Image imgParatroopa;
    private Image imgParatroopaLeft = new ImageIcon(Sprite.strImageDir + "Paratroopa_Left.gif").getImage();
    private Image imgParatroopaRight = new ImageIcon(Sprite.strImageDir + "Paratroopa_Right.gif").getImage();
    private Image imgParatroopaShell = new ImageIcon(Sprite.strImageDir + "Red_Koopa_Troopa_Shell1.gif").getImage();
    private final int HORIZONTAL_SPEED = +5; //Steps per frame
    private int nDeadTimeLeft = 0;
    private boolean bDead, bLookingLeft;
    public static final int WORTH = +1000;
    private int nWorthDeltaY = -5;
    private int nWorthY = 0;

    public Paratroopa(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.FOE);
        setCenter(new Point(nCenterX, nCenterY));
        setDeltaX(-HORIZONTAL_SPEED);
        setHeight(46);
        setWidth(32);
        bDead = false;
        bLookingLeft = true;
        imgParatroopa = imgParatroopaLeft;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgParatroopa,getCenter().x,getCenter().y,null);
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
            imgParatroopa = imgParatroopaShell;
            if (nDeadTimeLeft == 10) {
                setCenter(new Point(getCenter().x, getCenter().y + 14));
            }
            nDeadTimeLeft--;
        }
        else if (bLookingLeft) {
            imgParatroopa = imgParatroopaLeft;
        } else {
            imgParatroopa = imgParatroopaRight;
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
