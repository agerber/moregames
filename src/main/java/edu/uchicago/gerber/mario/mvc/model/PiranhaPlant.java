package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Piranha plant is a man-eating plant and belongs to FOE but it cannot be killed by jumping on it so beware!
 */
public class PiranhaPlant extends Sprite {
    private Image imgPiranhaPlant = new ImageIcon(Sprite.strImageDir + "Piranha_Plant.gif").getImage();

    public PiranhaPlant(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.FOE);
        setHeight(46);
        setWidth(32);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgPiranhaPlant,getCenter().x,getCenter().y,null);
    }
}
