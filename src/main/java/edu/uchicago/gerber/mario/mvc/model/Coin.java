package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * Created by John on 5/24/2016.
 */
public class Coin extends Sprite {
    private Image imgCoin = new ImageIcon(Sprite.strImageDir + "Coin.gif").getImage();
    public static final int COIN_WORTH = 100;

    public Coin(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.FRIEND);
        setHeight(28);
        setWidth(16);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgCoin,getCenter().x,getCenter().y,null);
    }


}
