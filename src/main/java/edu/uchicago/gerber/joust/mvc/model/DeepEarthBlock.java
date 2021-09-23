package edu.uchicago.gerber.joust.mvc.model;

import edu.uchicago.gerber.joust.mvc.controller.Game;


import javax.swing.*;
import java.awt.*;

public class DeepEarthBlock extends Platform {

    public static enum Direction {
        RIGHT, LEFT
    }

    private Image rightBlock = new ImageIcon(Sprite.getImgDir() + "deepEarthR.png").getImage();
    private Image leftBlock = new ImageIcon(Sprite.getImgDir() + "deepEarthL.png").getImage();
    private Direction direction;

//    private Point[] hitBox = new Point[2];

    private final int LEFT_X = -15;
    private final int RIGHT_X = Game.DIM.width + 15 - rightBlock.getWidth(null);
    private final int BOTH_Y = Game.DIM.height - (int)(1.4*rightBlock.getHeight(null));
    private final int BLOCK_WIDTH = rightBlock.getWidth(null);      // 208
    private final int BLOCK_HEIGHT = rightBlock.getHeight(null);    // 64

    public DeepEarthBlock(Direction direction) {
        super(new Point(0,0), 208, 64);
        this.direction = direction;
        if (this.direction == Direction.RIGHT) {
            getPos().setLocation(RIGHT_X, BOTH_Y);
        }
        else if (this.direction == Direction.LEFT) {
            getPos().setLocation(LEFT_X, BOTH_Y);
        }
        updateHitBoxBottomRightCorner();
        updateHitBoxTopLeftCorner();
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        if (direction == Direction.RIGHT) g2d.drawImage(rightBlock, (int)getPos().getX(), (int)getPos().getY(), null);
        else g2d.drawImage(leftBlock, (int)getPos().getX(), (int)getPos().getY(), null);
        if (Game.DRAW_COLLISION_POINTS) {
            g.setColor(Color.GREEN);
            g.drawOval(getHitBoxBottomRightCorner().x, getHitBoxBottomRightCorner().y, 2, 2);
            g.drawOval(getHitBoxTopLeftCorner().x, getHitBoxTopLeftCorner().y, 2, 2);
        }
    }
}
