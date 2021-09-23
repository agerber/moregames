package edu.uchicago.gerber.joust.mvc.model;

import java.awt.*;

public interface Movable {

    public static final int X_ACCEL_RATE = 1;
    public static final int Y_ACCEL_RATE = 1;

    public static enum Team {
        BKID, BFOX, FISH, PLATFORM, CLOUD, BOLT, BUBBLE, BONUSBALLOON, SCOREPOP, DEBRIS
    }

    public static enum Direction {
        RIGHT, LEFT
    }

    //for the game to move and draw movable objects
    public void move();
    public void draw(Graphics g);

    //for collision detection
    public Point getPos();
    public int getRadius();
    public void setTeam(Team team);
    public Team getTeam();

} //end Movable
