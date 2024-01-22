package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.airplane.AircraftShipGun;

import java.awt.*;

public class BulletShip extends Bullet {
    // The bullet of Ship (Class AircraftShipGun)

    private final int FIRE_FORCE = 10;
    private final int FIRE_EXPIRY = 50;


    public BulletShip(AircraftShipGun gun, double alpha) {
        int gunX = gun.getCenter().x;
        int gunY = gun.getCenter().y;
        final int DISTANCE_BETWEEN_BULLETS = 10;
        double orientationRadians = Math.toRadians(gun.getOrientation());
        double shiftX = Math.cos(orientationRadians) * alpha * DISTANCE_BETWEEN_BULLETS;
        double shiftY = Math.sin(orientationRadians) * alpha * DISTANCE_BETWEEN_BULLETS;
        Point center = new Point(gunX + (int)shiftX, gunY + (int)shiftY);
        setCenter(center);
        setFireForce(FIRE_FORCE);
        setTeam(Movable.Team.FOE);
        setImagePath("/imgs/fal/bulletShip.png");
        if (!gun.getRotateBack()) {
            setOrientation(gun.getOrientation() + 90);
        } else {
            setOrientation(gun.getOrientation() + 90);
        }
        final double FIRE_POWER = 10.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER;
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;
        setDeltaX(gun.getDeltaX() + vectorX);
        setDeltaY(gun.getDeltaY() + vectorY);
        setRadius(6);
        setExpiry(FIRE_EXPIRY);
    }


    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()), getRadius(), getRadius());
    }



}
