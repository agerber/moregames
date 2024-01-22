package edu.uchicago.gerber.g1941.mvc.model.buff;

import java.awt.*;

public class FloaterMissile extends Floater {
    // Add missile to the aircraft friend

    private final int RADIUS = 30;
    private final int EXPIRY = 260;
    private final Color COLOR = Color.CYAN;


    public FloaterMissile() {
        setColor(COLOR);
        setExpiry(EXPIRY);
        setRadius(RADIUS);
        setImagePath("/imgs/fal/missile.png");
    }



}
