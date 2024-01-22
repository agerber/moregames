package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.model.weapon.BulletEnemyF;

import java.awt.*;

public class AircraftEnemyF extends Aircraft {
    // This is the enemy Aircraft E
    // Enemy Aircraft E can fire bullet (Class BulletEnemyF)
    // Enemy Aircraft E occurs in MAP 2 and MAP3
    // Players need to pass MAP 1, then they can play MAP 2

    public static final int MIN_RADIUS = 28;
    public static final int SPEED = 5;
    public static int DESTROY_SCORE = 200;
    private final int HP = 20;
    private final String IMAGE_PATH = "/imgs/fal/aircraftEnemyF.png";


    public AircraftEnemyF(Point center) {
        setTeam(Team.FOE);
        setRadius(MIN_RADIUS);
        setCenter(center);
        setDeltaX(0);
        setDeltaY(SPEED);
        setBeingHit(false);
        setHp(HP);
        setDestroyScore(DESTROY_SCORE);
        setImagePath(IMAGE_PATH);
        setTotalHp(HP);
    }


    @Override
    public void move() {
        super.move();
        fire();
    }


    // Fire bullet (Class BulletEnemyF)
    // Fire bullet to the lower left if the Aircraft is in the left half side of the screen, vice versa
    public void fire() {
        if (CommandCenter.getInstance().getFrame() % 16 == 0) {
            int bulletOrientation = getCenter().getX() > (double) Game.DIM.width / 2 ? 45 : -45;
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletEnemyF(getCenter(), bulletOrientation),
                    GameOp.Action.ADD);
        }
    }



}
