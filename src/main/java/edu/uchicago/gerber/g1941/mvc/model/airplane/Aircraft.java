package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.controller.Sound;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.Sprite;
import edu.uchicago.gerber.g1941.mvc.model.WhiteCloudDebris;

import java.awt.*;
import java.util.LinkedList;


public class Aircraft extends Sprite {
    // There are some wired error if using @Data, therefore I decided to write getter and setter methods by myself

    private int destroyScore; // The score player gets by destroying this Aircraft
    private String imagePath; // The image path of this Aircraft
    public void setDestroyScore(int destroyScore) {this.destroyScore = destroyScore;} // Set the destroyScore
    public int getDestroyScore() {return this.destroyScore;} // Get the destroyScore
    public void setImagePath(String imagePath) {this.imagePath = imagePath;} // Set the image path
    public String getImagePath() {return this.imagePath;} // Get the image path


    // The current Aircraft hit by a Bullet, the input fireForce is the fireForce of the Bullet
    public boolean hit(int fireForce) {
        setHp(getHp() - fireForce);
        if (getHp() <= 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(this), GameOp.Action.ADD);
        }
        return getHp() > 0;
    }


    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        if (getHp() <= 0) {
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + getDestroyScore());
            Sound.playSound("kapow.wav");
        }
    }


    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()), getRadius(), getRadius());
    }


    // Draw the HP bar of the current Aircraft
    // The method will be Override in the AircraftFriend (player's Aircraft) Class
    // Because the HP bar of AircraftFriend shows in the top left corner
    public void drawHp(Graphics g) {
        int HpBarWidth = this.getRadius() * 2;
        int HpBarHeight = 5;
        int xVal = getCenter().x - HpBarWidth / 2;
        int yVal = getCenter().y - this.getRadius() - HpBarHeight;
        g.setColor(Color.BLUE);
        g.drawRect(xVal, yVal, HpBarWidth, HpBarHeight);
        int percent = (int) ((double) this.getHp() / (double) this.getTotalHp() * HpBarWidth);
        g.setColor(Color.CYAN);
        g.fillRect(xVal, yVal, percent, HpBarHeight);
    }



}
