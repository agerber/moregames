package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 6/4/2016.
 */
public class Firework extends Sprite {

    private Image imgFirework1 = new ImageIcon(Sprite.strImageDir + "Firework1.gif").getImage();
    private Image imgFirework2 = new ImageIcon(Sprite.strImageDir + "Firework2.gif").getImage();
    private Image imgFirework3 = new ImageIcon(Sprite.strImageDir + "Firework3.gif").getImage();
    private Image imgFirework;
    private static final int EXPIRY_LIMIT = 18;
    private int nExpiryCounter;

    public Firework(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.FRIEND);
        nExpiryCounter = EXPIRY_LIMIT;
        imgFirework = imgFirework1;

    }

    public void decrExpiryCounter() {
        nExpiryCounter--;
    }

    public int getExpiryCounter() {
        return nExpiryCounter;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        if (nExpiryCounter/6 >= 2) {
            imgFirework = imgFirework1;
        } else if (nExpiryCounter/6 == 1) {
            imgFirework = imgFirework2;
        } else {
            imgFirework = imgFirework3;
        }
        g2d.drawImage(imgFirework,getCenter().x,getCenter().y,null);
    }
}
