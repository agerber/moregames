package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.controller.Sound;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.weapon.BulletEnemyE;
import edu.uchicago.gerber.g1941.mvc.model.weapon.BulletRocket;

import java.awt.*;
import java.util.LinkedList;

public class AircraftEnemyE extends Aircraft {
    // This is the enemy Aircraft E, the final BOSS
    // Enemy Aircraft E can lunch rocket (Class BulletRocket) directly heads to the AircraftFriend (player's Aircraft)
    // And fire Bullet (Class BulletEnemyE)

    public static final int MIN_RADIUS = 68;
    public static final int SPEED = 3;
    public static int DESTROY_SCORE = 1000;
    private final int HP = 1000;
    private final int MAX_SPEED = 5;
    private final String IMAGE_PATH = "/imgs/fal/aircraftEnemyD.png";
    private boolean arriveDestination; // Check whether it arrives in the middle part of the screen


    public AircraftEnemyE() {
        setCenter(new Point(Game.DIM.width / 2, 0));
        setTeam(Movable.Team.FOE);
        setRadius(MIN_RADIUS);
        setDeltaX(0);
        setDeltaY(SPEED);
        setBeingHit(false);
        setHp(HP);
        setDestroyScore(DESTROY_SCORE);
        setImagePath(IMAGE_PATH);
        arriveDestination = false;
        setTotalHp(HP);
    }


    // It can random move if arrives in the middle part of the screen
    @Override
    public void move() {
        int upperBound = Game.DIM.height / 4;
        int lowerBound = Game.DIM.height / 4 * 2;
        int leftBound = this.getRadius();
        int rightBound = Game.DIM.width - this.getRadius();
        Point center = this.getCenter();
        if ((center.y < (upperBound + lowerBound) / 2) && !arriveDestination) {
            double newXPos = center.x + getDeltaX();
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) newXPos, (int) newYPos));
        } else {
            if (!arriveDestination) {
                setDeltaX(somePosNegValue(MAX_SPEED));
                setDeltaY(somePosNegValue(MAX_SPEED));
            }
            arriveDestination = true;
            if (CommandCenter.getInstance().getFrame() % 100 == 0 ||
                    center.x + getDeltaX() < leftBound ||
                    center.x + getDeltaX() > rightBound ||
                    center.y + getDeltaY() > lowerBound ||
                    center.y + getDeltaY() < upperBound) {
                int possibleDeltaX = somePosNegValue(MAX_SPEED);
                int possibleDeltaY = somePosNegValue(MAX_SPEED);
                if (center.x + possibleDeltaX < leftBound) {
                    setDeltaX(-possibleDeltaX);
                } else if (center.x + possibleDeltaX > rightBound) {
                    setDeltaX(-possibleDeltaX);
                } else {
                    setDeltaX(possibleDeltaX);
                }
                if (center.y + possibleDeltaY > lowerBound) {
                    setDeltaY(-possibleDeltaY);
                } else if (center.y + possibleDeltaY < upperBound) {
                    setDeltaY(-possibleDeltaY);
                } else {
                    setDeltaY(possibleDeltaY);
                }
            }
            setCenter(new Point((int) (center.x + getDeltaX()), (int) (center.y + getDeltaY())));
        }
        fire();
        lunchRocket();
    }


    // Fire bullet (Class BulletEnemyE) to twelve different angles
    public void fire() {
        if (CommandCenter.getInstance().getFrame() % 48 == 0) {
            int[] orientations = {0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330};
            for (int ori : orientations) {
                CommandCenter.getInstance().getOpsQueue().enqueue(
                        new BulletEnemyE(getCenter(), ori, getDeltaX(), getDeltaY()),
                        GameOp.Action.ADD);
            }
        }
    }


    // Lunch Rocket (Class BulletRocket)
    public void lunchRocket() {
        if (CommandCenter.getInstance().getFrame() % 90 == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletRocket(getCenter()),
                    GameOp.Action.ADD);
        }
    }


    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        if (getHp() <= 0) {
            Sound.playSound("kapow.wav");
            CommandCenter.getInstance().setBossDead(true);
        }
    }



}
