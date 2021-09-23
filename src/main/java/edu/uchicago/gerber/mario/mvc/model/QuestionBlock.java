package mario.mvc.model;

import javax.swing.*;
import java.awt.*;

/**
 * This question block is implemented to dispend coins but can be extended to give out other items.
 */
public class QuestionBlock extends Sprite {

    private Image imgQuestionBlock = new ImageIcon(Sprite.strImageDir + "Question_Block.gif").getImage();
    private Image imgEmptyBlock = new ImageIcon(Sprite.strImageDir + "Empty_Block.gif").getImage();
    private Image imgDisplay;
    public static final int COIN_WORTH = 100;
    private static int nCoinsHeld;

    public QuestionBlock(int nCenterX, int nCenterY){
        super(nCenterX,nCenterY);
        setTeam(Team.PLATFORM);
        setHeight(32);
        setWidth(32);
        nCoinsHeld = 10;
        imgDisplay = imgQuestionBlock;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgDisplay,getCenter().x,getCenter().y,null);
    }

    // Method to reduce coins held in this block
    public void decrCoins() {
        nCoinsHeld--;
    }

    @Override
    public void move() {
        if(nCoinsHeld == 0) {
            imgDisplay = imgEmptyBlock;
        }
        super.move();
    }

    public int getCoinsHeld(){
        return nCoinsHeld;
    }
}
