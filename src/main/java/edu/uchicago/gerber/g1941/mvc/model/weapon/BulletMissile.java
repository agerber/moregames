package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.airplane.*;

import java.awt.*;

public class BulletMissile extends Bullet {
    // The missile of player's Aircraft (Class AircraftFriend)
    // It will find the nearest enemy aircraft and hit it

    private final int FIRE_FORCE = 20;
    private final double SPEED = 30.0;  // This is like the Speed of missile


    public BulletMissile() {
        setCenter(CommandCenter.getInstance().getAircraftFriend().getCenter());
        setFireForce(FIRE_FORCE);
        setTeam(Movable.Team.FRIEND);
        setImagePath("/imgs/fal/missile.png");
    }


    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()), 20, 15);
    }


    @Override
    public void move(){
        super.move();
        Aircraft target = findNearestEnemy();
        if (target != null) {
            suicide(target, SPEED);
        }
    }


    // Find the nearest enemy aircraft, and return the enemy aircraft, return null otherwise
    public Aircraft findNearestEnemy() {
        double minDistance = Game.DIM.width * Game.DIM.height;
        int aircraftFriendX = CommandCenter.getInstance().getAircraftFriend().getCenter().x;
        int aircraftFriendY = CommandCenter.getInstance().getAircraftFriend().getCenter().y;
        int enemyX;
        int enemyY;
        Aircraft target = null;
        double curDistance;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Aircraft) {
                enemyX = movFoe.getCenter().x;
                enemyY = movFoe.getCenter().y;
                curDistance = Math.sqrt(Math.pow(aircraftFriendX - enemyX, 2) + Math.pow(aircraftFriendY - enemyY, 2));
                if (curDistance < minDistance) {
                    minDistance = curDistance;
                    target = (Aircraft) movFoe;
                }
            }
        }
        return target;
    }



}
