package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;

public class BulletEnemyF extends Bullet {
    // The bullet of enemy Aircraft F (Class AircraftEnemyF)

    private final int FIRE_FORCE = 20;
    private final double DELTA_X = 5;
    private final double DELTA_Y = 15;


    public BulletEnemyF(Point center, int bulletOrientation) {
        setCenter(center);
        setFireForce(FIRE_FORCE);
        setTeam(Movable.Team.FOE);
        setOrientation(bulletOrientation);
        setImagePath("/imgs/fal/bulletF.png");
        if (bulletOrientation < 0) {
            setDeltaX(DELTA_X);
            setDeltaY(DELTA_Y);
        } else {
            setDeltaX(-DELTA_X);
            setDeltaY(DELTA_Y);
        }
    }


    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()), 3, 12);
    }



}
