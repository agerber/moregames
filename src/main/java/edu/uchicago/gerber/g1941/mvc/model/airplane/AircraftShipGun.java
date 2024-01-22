package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.weapon.BulletShip;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class AircraftShipGun extends Aircraft {
    // This is the Gun on the Ship (Class AircraftShip)
    // ShipGun can rotate (method rotateLeftSide() and rotateRightSide)
    // And fire bullet (Class BulletShip) along the current shipGun orientation
    // ShipGun is in the sea
    // Therefore, no collision between shipGun and aircraft
    // ShipGun can not be destroyed
    // ShipGun occurs in MAP 2 and MAP3
    // Players need to pass MAP 1, then they can play MAP 2

    public static final int MIN_RADIUS = 18;
    private final String IMAGE_PATH = "/imgs/fal/shipGun.png";
    private boolean rotateBack; // Check whether the gun should rotate back


    public void setRotateBack(boolean rotateBack) {this.rotateBack = rotateBack;} // The setter of rotateBack
    public boolean getRotateBack() {return this.rotateBack;} // The getter of rotateBack


    public AircraftShipGun(AircraftShip ship) {
        setTeam(Movable.Team.SHIP);
        setRadius(MIN_RADIUS);
        setCenter(new Point(ship.getCenter().x, ship.getCenter().y + 50));
        setDeltaX(ship.getDeltaX());
        setDeltaY(ship.getDeltaY());
        setBeingHit(false);
        setImagePath(IMAGE_PATH);
        setOrientation(0);
        setHp(1);
        setRotateBack(false);
    }


    // Override
    // Because shipGun can rotate, and need to adjust the center of rotation due to the special shape of it
    // The line followed by // W. is the code for altering the rotation center
    @Override
    public void draw(Graphics g) {
        BufferedImage bufferedImage = loadGraphic(getImagePath());
        Graphics2D g2d = (Graphics2D) g;
        if (bufferedImage ==  null) return;
        int centerX = getCenter().x;
        int centerY = getCenter().y;
        int width = 15;
        int height = width / 3 * 5;
        double angleRadians = Math.toRadians(getOrientation());
        AffineTransform oldTransform = g2d.getTransform();
        try {
            double scaleX = width * 1.0 / bufferedImage.getWidth();
            double scaleY = height * 1.0 / bufferedImage.getHeight();
            AffineTransform affineTransform = new AffineTransform( oldTransform );
            if ( centerX != 0 || centerY != 0 ) {
                affineTransform.translate( centerX, centerY );
            }
            affineTransform.scale( scaleX, scaleY );
            int newCenterX = 0; // W.
            int newCenterY = -9; // W.
            affineTransform.translate(newCenterX, newCenterY); // W.
            if ( angleRadians != 0 ) {
                affineTransform.rotate( angleRadians );
            }
            affineTransform.translate(-newCenterX, -newCenterY); // W.
            affineTransform.translate( -bufferedImage.getWidth() / 2.0, -bufferedImage.getHeight() / 2.0 );
            g2d.setTransform( affineTransform );
            g2d.drawImage( bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null );
        } finally {
            g2d.setTransform( oldTransform );
        }
    }


    // Move
    // Rotate to the left side if ship is on the right side of the screen, vice versa
    @Override
    public void move() {
        super.move();
        if (getCenter().x < Game.DIM.width / 2) {
            rotateRightSide();
            fireRightSide();
        } else {
            rotateLeftSide();
            fireLeftSide();
        }
    }


    // Change the orientation of shipGun to make it rotate
    public void rotateLeftSide() {
        if (getOrientation() >= 0 && getOrientation() <= 180 && !getRotateBack()) {
            setOrientation(getOrientation() + 1);
            if (getOrientation() == 180) {
                setRotateBack(true);
            }
        } else {
            setOrientation(getOrientation() - 1);
            if (getOrientation() == 0) {
                setRotateBack(false);
            }
        }
    }


    // Change the orientation of shipGun to make it rotate
    public void rotateRightSide() {
        if (getOrientation() <= 0 && getOrientation() >= -180 && !getRotateBack()) {
            setOrientation(getOrientation() - 1);
            if (getOrientation() == -180) {
                setRotateBack(true);
            }
        } else {
            setOrientation(getOrientation() + 1);
            if (getOrientation() == 0) {
                setRotateBack(false);
            }
        }
    }


    // Fire two bullets (Class BulletShip)
    // The -0.9 and -0.2 are to fine-tune of the bullet locations
    public void fireLeftSide() {
        if (CommandCenter.getInstance().getFrame() % 32 == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletShip(this, -0.9),
                    GameOp.Action.ADD);
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletShip(this, -0.2),
                    GameOp.Action.ADD);
        }
    }


    // Fire two bullets (Class BulletShip)
    // The 0.9 and 0.2 are to fine-tune of the bullet locations
    public void fireRightSide() {
        if (CommandCenter.getInstance().getFrame() % 32 == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletShip(this, 0.9),
                    GameOp.Action.ADD);
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new BulletShip(this, 0.2),
                    GameOp.Action.ADD);
        }
    }


    @Override
    public void drawHp(Graphics g) {
        //do nothing, since ship can not be destroyed, it's unnecessary to draw the HP
    }



}
