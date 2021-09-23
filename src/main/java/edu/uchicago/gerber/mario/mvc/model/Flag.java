package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Class represents a flag to be taken down to clear the level
 */
public class Flag extends Sprite {
    private Image imgFlag = new ImageIcon(Sprite.strImageDir + "Flag.gif").getImage();
    private final int DESCEND_SPEED = +3; //Flag goes down at this speed when level is cleared
    public static final int FLAG_WORTH = 10000;

    public Flag(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.FRIEND);
        setHeight(32);
        setWidth(32);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgFlag,getCenter().x,getCenter().y,null);
    }

    @Override
    public void move() {
        if (CommandCenter.getInstance().isLevelClear()) {
            getCenter().y+= DESCEND_SPEED;
            CommandCenter.getInstance().setPntFlagCenterTracker(getCenter().x,getCenter().y);
        }
        super.move();
    }
}
