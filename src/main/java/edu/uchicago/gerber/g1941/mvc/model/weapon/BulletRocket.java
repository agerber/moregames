package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;

public class BulletRocket extends Bullet {
    // The rocket of enemy Aircraft E (Class AircraftEnemyB)
    // It will find the player's Aircraft and hit it

    private final int FIRE_FORCE = 10;
    private final double SPEED = 10.0;


    public BulletRocket(Point center) {
        setCenter(center);
        setFireForce(FIRE_FORCE);
        setTeam(Movable.Team.FOE);
        setImagePath("/imgs/fal/BulletRocket.png");
    }


    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()), 3, 12);
    }


    @Override
    public void move(){
        super.move();
        suicide(CommandCenter.getInstance().getAircraftFriend(), SPEED);
    }



}
