package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/23/2016.
 */
public class Cloud1 extends Sprite{

    private Image imgCloud1 = new ImageIcon(Sprite.strImageDir + "Cloud1.gif").getImage();
    private int nCenterX;
    private int nCenterY;
    private static final int CLOUD1_SPEED = -3; // Cloud 1 will be the fastest

    public Cloud1(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        this.nCenterX = nCenterX;
        this.nCenterY = nCenterY;
        setTeam(Team.BACKGROUND);
        setDeltaX(CLOUD1_SPEED);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgCloud1,getCenter().x,getCenter().y,null);
    }

    @Override
    public void move(){
        super.move();
        nCenterX+= getDeltaX();
        setCenter(new Point(nCenterX, nCenterY));;
    }
}
