package joust.mvc.model;

import java.awt.*;
import java.io.File;

public abstract class Sprite implements Movable {

    private Point pos;              // position of sprite center
    private int radius;             // radius of sprite hitbox
    private int vx;                 // velocity in x direction
    private int vy;                 // velocity in y direction
    private Team team;              // sprite's team
    private int animTicker;         // timer for animations

    public final int ORGANIC_FRAMERATE = 20;        // framerate for organic movement animation

    public static String imgDir = System.getProperty("user.dir") + File.separator + "src"
                            + File.separator + "joust" + File.separator + "images" + File.separator;


    public Sprite(Point initPos, Team team) {
        this.pos = initPos;
        this.team = team;
    }

    @Override
    public void setTeam(Team team) { this.team = team; }
    @Override
    public Team getTeam() { return team; }

    @Override
    public abstract void draw(Graphics g);

    @Override
    public Point getPos() {
        return pos;
    }
    public void setPos(Point pos) { this.pos = pos; }
    public int getX() { return (int)getPos().getX(); }
    public int getY() { return (int)getPos().getY(); }

    @Override
    public int getRadius() {
        return radius;
    }
    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getVx() {
        return vx;
    }
    public void setVx(int vx) {
        this.vx = vx;
    }

    public int getVy() {
        return vy;
    }
    public void setVy(int vy) {
        this.vy = vy;
    }

    public static String getImgDir() {
        return imgDir;
    }

    public int getAnimTicker() { return animTicker; }
    public void animTick() {
        animTicker += 1;
        if (animTicker == Integer.MAX_VALUE) animTicker = 0;
    }

    // return an int between 0 and the number of frames-1
    // when you have an animation with three images, do getAnimFrame(3, FRAMERATE_VALUE) to automatically index through Image[] array
    public int getLoopingAnimFrame(int numFrames, int rate) {
        return (animTicker/rate)%numFrames;
    }
    public int getOneTimeAnimFrame(int numFrames, int rate, int startTick) { return ((animTicker-startTick)/rate)%numFrames; }
}
