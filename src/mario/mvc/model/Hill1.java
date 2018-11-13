package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/24/2016.
 */
public class Hill1 extends Sprite {

    private Image imgHill1 = new ImageIcon(Sprite.strImageDir + "Hill1.gif").getImage();

    public Hill1(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.BACKGROUND);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgHill1,getCenter().x,getCenter().y,null);
    }

}
