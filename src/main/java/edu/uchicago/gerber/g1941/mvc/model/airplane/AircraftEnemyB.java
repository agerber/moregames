package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.model.weapon.BulletEnemyB;

import java.awt.*;

public class AircraftEnemyB extends Aircraft {
    // This is the enemy Aircraft B
    // Enemy Aircraft B can fire bullet (Class BulletEnemyB)

    public static final int MIN_RADIUS = 28;
    public static final int SPEED = 7;
    public static int DESTROY_SCORE = 200;
    private final int HP = 20;
    private final String IMAGE_PATH = "/imgs/fal/aircraftEnemyB.png";


    public AircraftEnemyB() {
        setTeam(Team.FOE);
        setRadius(MIN_RADIUS);
        int centerX = randomPosValue(Game.DIM.width - getRadius() * 2) + getRadius();
        setCenter(new Point(centerX, 0));
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


    // Fire bullet (Class BulletEnemyB)
    public void fire() {
        if (CommandCenter.getInstance().getFrame() % 16 == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletEnemyB(getCenter(), getOrientation(), getDeltaX(), getDeltaY()),
                    GameOp.Action.ADD);
        }
    }



}
