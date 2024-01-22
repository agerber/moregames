package edu.uchicago.gerber.g1941.mvc.model.airplane;

import java.awt.*;

public class AircraftEnemyA extends Aircraft {
    // This is the enemy Aircraft A

    public static final int MIN_RADIUS = 28;
    public static final int SPEED = 5;
    public static int DESTROY_SCORE = 100;
    private final int HP = 20;
    private final String IMAGE_PATH = "/imgs/fal/enemySmall.png";


    public AircraftEnemyA(Point givenCenter) {
        setCenter(givenCenter);
        setTeam(Team.FOE);
        setRadius(MIN_RADIUS);
        setDeltaX(0);
        setDeltaY(SPEED);
        setBeingHit(false);
        setHp(HP);
        setDestroyScore(DESTROY_SCORE);
        setImagePath(IMAGE_PATH);
        setTotalHp(HP);
    }



}
