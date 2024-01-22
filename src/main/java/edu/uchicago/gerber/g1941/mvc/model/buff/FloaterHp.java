package edu.uchicago.gerber.g1941.mvc.model.buff;

import java.awt.*;

public class FloaterHp extends Floater {
    // Add Hp for AircraftFriend

    private final int RADIUS = 30;
    private final int EXPIRY = 260;
    private final Color COLOR = Color.CYAN;
    public static final int HP_INCREMENT = 20;


    public FloaterHp() {
        setColor(COLOR);
        setExpiry(EXPIRY);
        setRadius(RADIUS);
        setImagePath("/imgs/fal/fuelCan.png");
    }



}
