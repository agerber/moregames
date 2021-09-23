package edu.uchicago.gerber.joust.mvc.model;

import edu.uchicago.gerber.joust.mvc.controller.Game;

import javax.swing.*;
import java.awt.*;

public class Pipe extends Platform {

    public static enum Length {
        SHORT, LONG
    }

    private Image shortPipeImg = new ImageIcon(Sprite.getImgDir() + "pipeShort.png").getImage();
    private Image longPipeImg = new ImageIcon(Sprite.getImgDir() + "pipeLong.png").getImage();
    private Length length;

    public Pipe(Length length, Point pos) {
        super(pos, 26, length == Length.SHORT ? 24 : 34);
        this.length = length;
    }

    @Override
    public void move() { }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(length == Length.SHORT ? shortPipeImg : longPipeImg, (int)getPos().getX(), (int)getPos().getY(), null);
        if (Game.DRAW_COLLISION_POINTS) {
            g.setColor(Color.GREEN);
            g.drawOval(getHitBoxTopLeftCorner().x, getHitBoxTopLeftCorner().y, 2, 2);
            g.drawOval(getHitBoxBottomRightCorner().x, getHitBoxBottomRightCorner().y, 2, 2);
        }
    }
}
