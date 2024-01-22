package edu.uchicago.gerber.g1941.mvc.model.buff;

import java.awt.*;

public class FloaterBullet extends Floater {
    // Enhance bullet of AircraftFriend

    private final int RADIUS = 30;
    private final int EXPIRY = 260;
    private final Color COLOR = Color.CYAN;


    public FloaterBullet() {
        setColor(COLOR);
        setExpiry(EXPIRY);
        setRadius(RADIUS);
        setImagePath("/imgs/fal/bullet.png");
    }



}
