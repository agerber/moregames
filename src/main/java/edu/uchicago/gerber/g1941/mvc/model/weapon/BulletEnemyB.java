package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BulletEnemyB extends Bullet {
    // The bullet of enemy Aircraft B (Class AircraftEnemyB)

    private final int FIRE_FORCE = 10;


    public BulletEnemyB(Point center, int orientation, double deltaX, double deltaY) {
        setFireForce(FIRE_FORCE);
        setTeam(Movable.Team.FOE);
        setColor(Color.BLUE);
        setExpiry(20);
        setRadius(6);
        setCenter(center);
        setOrientation(90);
        final double FIRE_POWER = 10.0;
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
