package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * This class represents building block of type platform to place friendly items like coins and stars
 */
public class Block extends Sprite {
    private Image imgBlock = new ImageIcon(Sprite.strImageDir + "Block.gif").getImage();

    public Block(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.PLATFORM);
        setHeight(32);
        setWidth(32);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgBlock,getCenter().x,getCenter().y,null);
    }
}
