package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;

public class BulletEnemyE extends Bullet {
    // The bullet of enemy Aircraft E (Class AircraftEnemyE)

    private final int FIRE_FORCE = 10;
    private final int FIRE_EXPIRY = 50;


    public BulletEnemyE(Point center, int orientation, double deltaX, double deltaY) {
        setFireForce(FIRE_FORCE);
        setTeam(Movable.Team.FOE);
        setColor(Color.BLUE);
        setExpiry(FIRE_EXPIRY);
        setRadius(6);
        setCenter(center);
        setOrientation(orientation);
        setImagePath("/imgs/fal/bullet_01.png");
        final double FIRE_POWER = 10.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER;
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;
        setDeltaX(deltaX + vectorX);
        setDeltaY(deltaY + vectorY);
    }


    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic("/imgs/fal/bullet_01.png"), getRadius(), getRadius());
    }


}
