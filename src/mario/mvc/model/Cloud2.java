package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/24/2016.
 */
public class Cloud2 extends Sprite{

    private Image imgCloud2 = new ImageIcon(Sprite.strImageDir + "Cloud2.gif").getImage();
    private int nCenterX;
    private int nCenterY;
    private static final int CLOUD2_SPEED = -2; // Cloud 2 will be of medium speed

    public Cloud2(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        this.nCenterX = nCenterX;
        this.nCenterY = nCenterY;
        setTeam(Team.BACKGROUND);
        setDeltaX(CLOUD2_SPEED);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgCloud2,getCenter().x,getCenter().y,null);
    }

    @Override
    public void move(){
        super.move();
        nCenterX+= getDeltaX();
        setCenter(new Point(nCenterX, nCenterY));;
    }
}
