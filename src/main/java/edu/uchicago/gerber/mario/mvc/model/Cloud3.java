package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/24/2016.
 */
public class Cloud3 extends Sprite{
    private Image imgCloud3 = new ImageIcon(Sprite.strImageDir + "Cloud3.gif").getImage();
    private int nCenterX;
    private int nCenterY;
    private static final int CLOUD3_SPEED = -1; // Cloud 3 will be the slowest

    public Cloud3(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        this.nCenterX = nCenterX;
        this.nCenterY = nCenterY;
        setTeam(Team.BACKGROUND);
        setDeltaX(CLOUD3_SPEED);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgCloud3,getCenter().x,getCenter().y,null);
    }

    @Override
    public void move(){
        super.move();
        nCenterX+= getDeltaX();
        setCenter(new Point(nCenterX, nCenterY));;
    }
}
