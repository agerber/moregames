package joust.mvc.model;

import joust.mvc.controller.Game;

import javax.swing.*;
import java.awt.*;

public class EarthWallBlock extends Platform {
    private Image[] earthWallBlocksL = { new ImageIcon(Sprite.getImgDir() + "earthWallBlockL1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthWallBlockL2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthWallBlockL3.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthWallBlockL4.png").getImage() };
    private Image[] earthWallBlocksR = { new ImageIcon(Sprite.getImgDir() + "earthWallBlockR1.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthWallBlockR2.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthWallBlockR3.png").getImage(),
            new ImageIcon(Sprite.getImgDir() + "earthWallBlockR4.png").getImage() };
    private int blockNum;
    private Direction direction;

    private final int BLOCK_WIDTH = earthWallBlocksL[0].getWidth(null);  // 16


    public EarthWallBlock(int blockNum, Point pos, Direction direction) {
        super(pos, 18, 16);
        this.blockNum = blockNum;
        this.direction = direction;
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(direction == Direction.LEFT ? earthWallBlocksL[blockNum] : earthWallBlocksR[blockNum], (int)getPos().getX(), (int)getPos().getY(), null);
        if (Game.DRAW_COLLISION_POINTS) {
            g.setColor(Color.GREEN);
            g.drawOval(getHitBoxTopLeftCorner().x, getHitBoxTopLeftCorner().y, 2, 2);
            g.drawOval(getHitBoxBottomRightCorner().x, getHitBoxBottomRightCorner().y, 2, 2);
        }
    }
}
