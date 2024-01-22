package edu.uchicago.gerber.g1941.mvc.model.buff;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.Sound;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.Sprite;

import java.awt.*;
import java.util.LinkedList;

public abstract class Floater extends Sprite {
    // This is the abstract class for Floaters
    // The Floaters is like the buff in video games, it will enhance the player's Aircraft

    private final int MAX_SPEED = 5;
    private String imagePath;
    public void setImagePath(String imagePath) {this.imagePath = imagePath;}
    public String getImagePath() {return this.imagePath;}


    public Floater() {
        setTeam(Team.FLOATER);
        setDeltaX(somePosNegValue(MAX_SPEED));
        setDeltaY(somePosNegValue(MAX_SPEED));
        setSpin(somePosNegValue(10));
        setCenter(new Point(
                Game.R.nextInt(Game.DIM.width / 3) + Game.DIM.width / 3,
                Game.R.nextInt(Game.DIM.height / 3) + Game.DIM.height / 3
        ));
    }


    @Override
    public void draw(Graphics g) {
        g.setColor(this.getColor());
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()), 20, 15);
        g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() * 2, getRadius() * 2);
    }


    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        //if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
        if (getExpiry() > 0) {
            Sound.playSound("shieldup.wav");
            CommandCenter.getInstance().getAircraftFriend().setShield(10);
        }
    }



}
