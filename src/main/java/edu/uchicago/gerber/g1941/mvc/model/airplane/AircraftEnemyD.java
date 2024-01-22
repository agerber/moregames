package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;

public class AircraftEnemyD extends Aircraft {
    // This is the enemy Aircraft D
    // Enemy Aircraft D will stay (implemented by method stay()) in the middle of the screen

    public static final int MIN_RADIUS = 48;
    public static final int SPEED = 5;
    public static int DESTROY_SCORE = 500;
    private final int HP = 200;
    private final String IMAGE_PATH = "/imgs/fal/aircraftEnemyD.png";


    public AircraftEnemyD() {
        setCenter(new Point(Game.DIM.width / 2, 0));
        setTeam(Movable.Team.FOE);
        setRadius(MIN_RADIUS);
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
        stay();
    }


    // Stay in the middle of the screen if it arrives at the middle part
    public void stay() {
        if (getCenter().y == Game.DIM.height / 3) {
            setDeltaX(0);
            setDeltaY(0);
        }
    }



}
