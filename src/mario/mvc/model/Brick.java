package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/25/2016.
 */
public class Brick extends Sprite {
    private Image imgBrick = new ImageIcon(Sprite.strImageDir + "Brick.gif").getImage();

    public Brick(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.PLATFORM);
        setHeight(32);
        setWidth(32);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgBrick,getCenter().x,getCenter().y,null);
    }

}
