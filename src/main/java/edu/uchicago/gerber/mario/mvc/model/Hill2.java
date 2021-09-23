package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/24/2016.
 */
public class Hill2 extends Sprite {
    private Image imgHill2 = new ImageIcon(Sprite.strImageDir + "Hill2.gif").getImage();

    public Hill2(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.BACKGROUND);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgHill2,getCenter().x,getCenter().y,null);
    }

}
