package edu.uchicago.gerber.g1941.mvc.model;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.controller.Utils;
import edu.uchicago.gerber.g1941.mvc.model.airplane.Aircraft;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;


// -------------------------------------------------------------
// Nearly no change
// Change the method move()
// move()
// The Sprite will be removed if it exceeds the range of the screen, it will not occur on the left edge if it reaches the right edge

// Add a method suicide()
// The Sprite will directly head to target Sprite

// Add a method randomPosValue()
// Return random positive value
// -------------------------------------------------------------


@Data
public abstract class Sprite implements Movable {
    private Point center;
    private double deltaX, deltaY;
    private Team team;
    private int radius;
    private int orientation;
    private int expiry;
    private int spin;
    private Point[] cartesians;
    private Color color;
    private Map<?, BufferedImage> rasterMap;
    private int hp;
    private int totalHp;
    private boolean beingHit;


    public Sprite() {
        //place the sprite at some random location in the game-space at instantiation
        setCenter(new Point(Game.R.nextInt(Game.DIM.width),
                Game.R.nextInt(Game.DIM.height)));
    }


    @Override
    public void move() {
        if (center.x > Game.DIM.width || center.x < 0 || center.y > Game.DIM.height || center.y < 0) {
            setExpiry(1);
        } else {
            double newXPos = center.x + getDeltaX();
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) newXPos, (int) newYPos));
        }
        if (expiry > 0) expire();
        if (spin != 0) orientation += spin;
    }


    private void expire() {
        if (expiry == 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
        expiry--;
    }


    protected int somePosNegValue(int seed) {
        int randomNumber = Game.R.nextInt(seed);
        return (randomNumber % 2 == 0) ? randomNumber : -randomNumber;
    }


    protected int randomPosValue(int seed) {
        return Game.R.nextInt(seed);
    }


    public static BufferedImage loadGraphic(String imagePath) {
        BufferedImage bufferedImage;
        try {
            bufferedImage =
                    ImageIO.read(Objects.requireNonNull(Sprite.class.getResourceAsStream("/g1941/" + imagePath)));
        }
        catch (IOException e) {
            e.printStackTrace();
            bufferedImage = null;
        }
        return bufferedImage;
    }


    //https://www.tabnine.com/code/java/methods/java.awt.geom.AffineTransform/rotate
    protected void renderRaster(Graphics2D g2d, BufferedImage bufferedImage, int width, int height) {
        if (bufferedImage ==  null) return;
        int centerX = getCenter().x;
        int centerY = getCenter().y;
        width = width * 2;
        height = height * 2;
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
            if ( angleRadians != 0 ) {
                affineTransform.rotate( angleRadians );
            }
            affineTransform.translate( -bufferedImage.getWidth() / 2.0, -bufferedImage.getHeight() / 2.0 );
            g2d.setTransform( affineTransform );
            g2d.drawImage( bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null );
        } finally {
            g2d.setTransform( oldTransform );
        }
    }


    protected void renderVector(Graphics g) {
        //set the graphics context color to the color of the sprite
        g.setColor(color);
        // To render this Sprite in vector mode, we need to, 1: convert raw cartesians to raw polars, 2: rotate polars
        // for orientation of sprite. 3: Convert back to cartesians 4: adjust for center-point (location).
        // and 5: pass the cartesian-x and cartesian-y coords as arrays, along with length, to g.drawPolygon().
        //1: convert raw cartesians to raw polars (used later in stream below).
        //The reason we convert cartesian-points to polar-points is that it's much easier to rotate polar-points
        List<PolarPoint> polars = Utils.cartesianToPolar(getCartesians());
        //2: rotate raw polars given the orientation of the sprite.
        Function<PolarPoint, PolarPoint> rotatePolarByOrientation =
                pp -> new PolarPoint(
                        pp.getR(),
                        pp.getTheta() + Math.toRadians(getOrientation()) //rotated Theta
                );
        //3: convert the rotated polars back to cartesians
        Function<PolarPoint, Point> polarToCartesian =
                pp -> new Point(
                        (int)  (pp.getR() * getRadius() * Math.sin(pp.getTheta())),
                        (int)  (pp.getR() * getRadius() * Math.cos(pp.getTheta())));
        //4: adjust the cartesians for the location (center-point) of the sprite.
        // the reason we subtract the y-value has to do with how Java plots the vertical axis for
        // graphics (from top to bottom)
        Function<Point, Point> adjustForLocation =
                p -> new Point(
                         getCenter().x + p.x,
                         getCenter().y - p.y);
        //5: draw the polygon using the List of raw polars from above, applying mapping transforms as required
        g.drawPolygon(
                polars.stream()
                        .map(rotatePolarByOrientation)
                        .map(polarToCartesian)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.x)
                        .mapToInt(Integer::intValue)
                        .toArray(),
                polars.stream()
                        .map(rotatePolarByOrientation)
                        .map(polarToCartesian)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.y)
                        .mapToInt(Integer::intValue)
                        .toArray(),
                polars.size());
        //for debugging center-point and collision. Feel free to remove these three lines.
        //#########################################
        //g.setColor(Color.GRAY);
        //g.fillOval(getCenter().x - 1, getCenter().y - 1, 2, 2);
        //g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
        //#########################################
    }


    //default behavior for adding and removing objects from game space
    @Override
    public void add(LinkedList<Movable> list) {
        list.add(this);
    }


    @Override
    public void remove(LinkedList<Movable> list) {
        list.remove(this);
    }


    protected void suicide(Aircraft target, double speed) {
        Point targetCenter = target.getCenter();
        double distanceX = this.getCenter().x - targetCenter.x;
        double distanceY = this.getCenter().y - targetCenter.y;
        double angleRadians = Math.atan2(distanceY, distanceX);
        double angleDegrees = Math.toDegrees(angleRadians);
        angleDegrees += 90;
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }
        setOrientation((int) angleDegrees);
        setDeltaX(-speed * Math.cos(angleRadians));
        setDeltaY(-speed * Math.sin(angleRadians));
    }



}
