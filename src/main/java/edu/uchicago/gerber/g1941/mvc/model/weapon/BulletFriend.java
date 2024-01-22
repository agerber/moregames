package edu.uchicago.gerber.g1941.mvc.model.weapon;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BulletFriend extends Bullet {
    // The bullet of player's Aircraft (Class AircraftFriend)

    private final int FIRE_FORCE = 10;


    public BulletFriend(Point center, int orientation, double deltaX, double deltaY) {
        setFireForce(FIRE_FORCE);
        setTeam(Team.FRIEND);
        setColor(Color.ORANGE);
        setExpiry(20);
        setRadius(6);
        setCenter(center);
        setOrientation(orientation);
        final double FIRE_POWER = 35.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER;
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;
        setDeltaX(deltaX + vectorX);
        setDeltaY(deltaY + vectorY);
        List<Point> listPoints = new ArrayList<>();
        listPoints.add(new Point(0, 3));
        listPoints.add(new Point(1, -1));
        listPoints.add(new Point(0, 0));
        listPoints.add(new Point(-1, -1));
        setCartesians(listPoints.toArray(new Point[0]));
    }

    
    
}
