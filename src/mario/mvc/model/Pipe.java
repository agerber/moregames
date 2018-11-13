package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Pipe class is used by Mario to enter into the game.
 */
public class Pipe extends Sprite {
    private Image imgPipe = new ImageIcon(Sprite.strImageDir + "Pipe.gif").getImage();

    public Pipe(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.PLATFORM);
        setHeight(65);
        setWidth(65);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgPipe,getCenter().x,getCenter().y,null);
    }

}
