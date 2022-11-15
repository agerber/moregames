package edu.uchicago.gerber.galaga.model;


import edu.uchicago.gerber.galaga.controller.Game;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

//the lombok @Data gives us automatic getters and setters on all members
@Data
public abstract class Sprite implements Movable {
    //the center-point of this sprite
    private Point center;
    //this causes movement; change-in-x and change-in-y
    private double deltaX, deltaY;

    //every sprite has a team: friend, foe, floater, or debris.
    private Team team;
    //the radius of circumscribing circle
    private int radius;

    //orientation from 0-359
    private int orientation;
    //natural mortality (short-lived sprites only)
    private int expiry;
    //the color of this sprite
    private Color color;

    //some sprites spin, such as floaters and asteroids
    private int spin;

    //use for fade-in/fade-out
    private int fade;

    //these are Cartesian points used to draw the polygon.
    //once set, their values do not change. It's the job of the render() method to adjust for orientation and location.
    private Point[] cartesians;

    //constructor
    public Sprite() {

        //default sprite color
        setColor(Color.WHITE);
        //place the sprite at some random location in the frame at instantiation
        setCenter(new Point(Game.R.nextInt(Game.DIM.width), Game.DIM.height));


    }


    @Override
    public void move() {

        //The following code block just keeps the sprite inside the bounds of the frame.
        //To ensure this behavior among all sprites in your game, make sure to call super.move() in extending classes
        // where you need to override the move() method.
        Point center = getCenter();

        //right-bounds reached
        if (center.x > Game.DIM.width) {
            setCenter(new Point(Game.DIM.width -1, center.y));
            setDeltaX(-getDeltaX());
        //left-bounds reached
        } else if (center.x < 0) {
          //  setCenter(new Point(Game.DIM.width - 1, center.y));
            setCenter(new Point(1, center.y));
            setDeltaX(-getDeltaX());
        //bottom-bounds reached
        } else if (center.y > Game.DIM.height) {
            setCenter(new Point(center.x, 1));
        //top-bounds reached
        } else if (center.y < 0) {
            setCenter(new Point(center.x, Game.DIM.height - 1));
        //in-bounds
        } else {
            double newXPos = center.x + getDeltaX();
            double newYPos = center.y + getDeltaY();
            setCenter(new Point((int) newXPos, (int) newYPos));
        }

        //expire (decrement expiry) on short-lived objects only
        //the default value of expiry is zero, so this block will only apply to expiring sprites
        if (getExpiry() > 0) expire();

        //if a sprite spins, adjust its orientation
        //the default value of spin is zero, therefore non-spinning objects will not call this block.
        if (getSpin() != 0) {
            setOrientation(getOrientation() + getSpin());
        }

    }

    private void expire() {

        //if a short-lived sprite has an expiry of one, it commits suicide by enqueuing itself (this) onto the
        //opsList with an operation of REMOVE
        if (getExpiry() == 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
        //and then decrements in all cases
        setExpiry(getExpiry() - 1);

    }


    protected double hypotFunction(double dX, double dY) {
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }

    protected int somePosNegValue(int seed) {
        int randomNumber = Game.R.nextInt(seed);
        if (randomNumber % 2 == 0)
            randomNumber = -randomNumber;
        return randomNumber;
    }

    protected int somePosValue(int seed) {
        return Game.R.nextInt(seed);

    }

    @Override
    public boolean isProtected() {
        //by default, sprites are not protected
        return false;
    }

    //certain Sprites, such as Asteroid use this
    protected Point[] polarToCartesian(List<PolarPoint> polPolars) {

        //when casting from double to int, we truncate and lose precision, so best to be generous with multiplier
        final int PRECISION_MULTIPLIER = 1000;
        Function<PolarPoint, Point> polarToCartTransform = pp -> new Point(
                (int) (getCenter().x + pp.getR() * getRadius() * PRECISION_MULTIPLIER
                        * Math.sin(Math.toRadians(getOrientation())
                        + pp.getTheta())),
                (int) (getCenter().y - pp.getR() * getRadius() * PRECISION_MULTIPLIER
                        * Math.cos(Math.toRadians(getOrientation())
                        + pp.getTheta())));

        return polPolars.stream()
                .map(polarToCartTransform)
                .toArray(Point[]::new);

    }

    protected List<PolarPoint> cartesianToPolar(List<Point> pntCartesians) {

        BiFunction<Point, Double, PolarPoint> cartToPolarTransform = (pnt, hyp) -> new PolarPoint(
                //this is r from PolarPoint(r,theta).
                hypotFunction(pnt.x, pnt.y) / hyp, //r is relative to the largestHypotenuse
                //this is theta from PolarPoint(r,theta)
                Math.toDegrees(Math.atan2(pnt.y, pnt.x)) * Math.PI / 180
        );


        //determine the largest hypotenuse
        double largestHypotenuse = 0;
        for (Point pnt : pntCartesians)
            if (hypotFunction(pnt.x, pnt.y) > largestHypotenuse)
                largestHypotenuse = hypotFunction(pnt.x, pnt.y);


        //we must make hypotenuse final to pass into a stream.
        final double hyp = largestHypotenuse;


        return pntCartesians.stream()
                .map(pnt -> cartToPolarTransform.apply(pnt, hyp))
                .collect(Collectors.toList());

    }

    @Override
    public void draw(Graphics g) {
        //set the native color of the sprite
        g.setColor(getColor());
        render(g);

    }

    public void draw(Graphics g, Color color) {
        //set custom color
        g.setColor(color);
        render(g);

    }

    private void render(Graphics g) {

        // to render this Sprite, we need to, 1: convert raw cartesians to raw polars, 2: adjust polars
        // for orientation of sprite. Convert back to cartesians 3: adjust for center-point (location).
        // and 4: pass the cartesian-x and cartesian-y coords as arrays, along with length, to drawPolygon().

        //convert raw cartesians to raw polars
        List<PolarPoint> polars = cartesianToPolar(Arrays.asList(getCartesians()));

        //rotate raw polars given the orientation of the sprite. Then convert back to cartesians.
        Function<PolarPoint, Point> adjustForOrientation =
                pp -> new Point(
                        (int)  (pp.getR() * getRadius()
                                * Math.sin(Math.toRadians(getOrientation())
                                + pp.getTheta())),

                        (int)  (pp.getR() * getRadius()
                                * Math.cos(Math.toRadians(getOrientation())
                                + pp.getTheta())));

        // adjust for the location (center-point) of the sprite.
        // the reason we subtract the y-value has to do with how Java plots the vertical axis for
        // graphics (from top to bottom)
        Function<Point, Point> adjustForLocation =
                p -> new Point(
                         getCenter().x + p.x,
                         getCenter().y - p.y);



        g.drawPolygon(
                polars.stream()
                        .map(adjustForOrientation)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.x)
                        .mapToInt(Integer::intValue)
                        .toArray(),

                polars.stream()
                        .map(adjustForOrientation)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.y)
                        .mapToInt(Integer::intValue)
                        .toArray(),

                getCartesians().length);

        //for debugging center-point. Feel free to remove these two lines.
        //#########################################
        g.setColor(Color.ORANGE);
        g.fillOval(getCenter().x - 1, getCenter().y - 1, 2, 2);
        //g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
        //#########################################
    }


    //in order to overload a lombok'ed method, we need to use the @Tolerate annotation
    //this overloaded method allows us to pass-in either a List<Point> or Point[] (lombok'ed method) to setCartesians()
    @Tolerate
    public void setCartesians(List<Point> pntPs) {
        setCartesians(pntPs.stream()
                .toArray(Point[]::new));

    }


}
