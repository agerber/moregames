package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/24/2016.
 */
public class Ground extends Sprite{

    private Image imgGround = new ImageIcon(Sprite.strImageDir + "Ground3.gif").getImage();
    private int nCenterX;
    public static int nCenterY = 712; // Height of ground will be fixed for all levels

    public Ground(int nCenterX) {
        super(nCenterX,nCenterY);
        this.nCenterX = nCenterX;
        setTeam(Team.PLATFORM);
        setCenter(new Point(nCenterX, nCenterY));
        setHeight(88);
        setWidth(510);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgGround,getCenter().x,getCenter().y,null);
    }


}
