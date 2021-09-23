package edu.uchicago.gerber.joust.mvc.model;



import edu.uchicago.gerber.joust.mvc.controller.Game;

import javax.swing.*;
import java.awt.*;

public class EarthBlock extends Platform {

    private Image[] earthBlocks = { new ImageIcon(Sprite.getImgDir() + "earthBlock1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthBlock2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthBlock3.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthBlock4.png").getImage() };
    private int blockNum;

    private final int BLOCK_WIDTH = earthBlocks[0].getWidth(null);  // 16


    public EarthBlock(int blockNum, Point pos) {
        super(pos, 18, 16);
        this.blockNum = blockNum;
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(earthBlocks[blockNum], (int)getPos().getX(), (int)getPos().getY(), null);
        if (Game.DRAW_COLLISION_POINTS) {
            g.setColor(Color.GREEN);
            g.drawOval(getHitBoxTopLeftCorner().x, getHitBoxTopLeftCorner().y, 2, 2);
            g.drawOval(getHitBoxBottomRightCorner().x, getHitBoxBottomRightCorner().y, 2, 2);
        }
    }
}
