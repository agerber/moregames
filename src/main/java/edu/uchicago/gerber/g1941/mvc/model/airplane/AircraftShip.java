package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.awt.*;

public class AircraftShip extends Aircraft {
    // This is the Ship, Ship has a Gun (Class AircraftShipGun)
    // Ship is in the sea
    // Therefore, no collision between ship and aircraft
    // Ship can not be destroyed
    // Ship occurs in MAP 2 and MAP3
    // Players need to pass MAP 1, then they can play MAP 2

    public static final int MIN_RADIUS = 28;
    public static final int SPEED = 1;
    private final String IMAGE_PATH = "/imgs/fal/ship_02.png";


    public AircraftShip() {
        setTeam(Movable.Team.SHIP);
        setRadius(MIN_RADIUS);
        int centerX = randomPosValue(Game.DIM.width - getRadius() * 2) + getRadius();
        setCenter(new Point(centerX, 0));
        setDeltaX(0);
        setDeltaY(SPEED);
        setBeingHit(false);
        setImagePath(IMAGE_PATH);
        setHp(1);
    }


    // Override, because draw based on the width and height instead of radius, ship has a shape of rectangle
    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, loadGraphic(getImagePath()),12, 12 * 7);
    }


    @Override
    public void drawHp(Graphics g) {
        //do nothing, since ship can not be destroyed, it's unnecessary to draw the HP
    }



}
