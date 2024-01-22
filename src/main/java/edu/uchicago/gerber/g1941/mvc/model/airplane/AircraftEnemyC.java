package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;

public class AircraftEnemyC extends Aircraft {
    // This is the enemy Aircraft C
    // Enemy Aircraft C can suicide, which means it will directly head to the AircraftFriend (player's Aircraft)
    // And try to destroy the AircraftFriend

    public static final int MIN_RADIUS = 18;
    public static final int SPEED = 12;
    public static int DESTROY_SCORE = 200;
    private final int HP = 10;
    private final String IMAGE_PATH = "/imgs/fal/aircraftEnemyB.png";


    public AircraftEnemyC() {
        setTeam(Movable.Team.FOE);
        setRadius(MIN_RADIUS);
        int centerX = randomPosValue(Game.DIM.width - getRadius() * 2) + getRadius();
        setCenter(new Point(centerX, 0));
        setBeingHit(false);
        setHp(HP);
        setDestroyScore(DESTROY_SCORE);
        setImagePath(IMAGE_PATH);
        setTotalHp(HP);
    }


    @Override
    public void move() {
        super.move();
        suicide(CommandCenter.getInstance().getAircraftFriend(), SPEED); // See comments of suicide method in Class Sprite
    }



}
