package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/28/2016.
 */
public class Bush1 extends Sprite {
    private Image imgBush1 = new ImageIcon(Sprite.strImageDir + "Bush1.gif").getImage();

    public Bush1(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.BACKGROUND);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgBush1,getCenter().x,getCenter().y,null);
    }

}
