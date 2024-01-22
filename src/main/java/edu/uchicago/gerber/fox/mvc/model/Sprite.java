package edu.uchicago.gerber.fox.mvc.model;

import edu.uchicago.gerber.fox.mvc.controller.Game;
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

//the lombok @Data gives us automatic getters and setters on all members

//A Sprite can be either vector or raster. We do not implement the draw(Graphics g) method, thereby forcing extending
// classes to implement draw() depending on their graphics mode: vector or raster.  See Falcon, and WhiteCloudDebris
// classes for raster implementation of draw(). See ShieldFloater, Bullet, or Asteroid for vector implementations of
// draw().
@Data
public abstract class Sprite implements Movable {
    //the center-point of this sprite
    private Point center;
    //this causes movement; change-in-x and change-in-y
    private double deltaX, deltaY;

    //every sprite has a team: friend, foe, floater, or debris.
    private Team team;
    private int lifeSpan = 0; //timer for sprite with multiple images
    private int width;
    private int height;


    //used for vector rendering
    private Color color;
    // 2D rectangles to bound the sprites, used for collision detection
    private List<Rectangle> bounds = new LinkedList<>();

    //Either you use the cartesian points and color above (vector), or you can use the BufferedImages here (raster).
    //Keys in this map can be any object (?) you want. See Falcon and WhiteCloudDebris for example implementations.
    private Map<?, BufferedImage> rasterMap;


    //constructor
    public Sprite(int width, int height) {

        //place the sprite at some random location in the game-space at instantiation
        setCenter(new Point(Game.R.nextInt(Game.DIM.width),
                Game.R.nextInt(Game.DIM.height)));
        setWidth(width);
        setHeight(height);
    }


    @Override
    public void move() {

        //The following code block just keeps the sprite inside the bounds of the frame.
        //To ensure this behavior among all sprites in your game, make sure to call super.move() in extending classes
        // where you need to override the move() method.

        //right-bounds reached
        if (center.x > Game.DIM.width) {
            setCenter(new Point(1, center.y));
        //left-bounds reached
        } else if (center.x < 0) {
            if(this instanceof Obstacle){
                // obstacle moves out of frame
                ((Obstacle) this).alive = false;
            }else if(this instanceof BonusCoin){
                ((BonusCoin) this).alive = false;// check the derived class first
            }else if(this instanceof Coin){
                ((Coin) this).alive = false;
            }else{
                setCenter(new Point(Game.DIM.width - 1, center.y));
            }
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
        //move bounding boxes
        for (Rectangle bound: bounds){
            bound.translate((int)getDeltaX(), (int)getDeltaY());
            //not very precise
        }
        lifeSpan++;
    }



    //A protected sprite will not be destroyed upon collision
    @Override
    public boolean isProtected() {
        //by default, sprites are not protected
        return false;
    }


    //used to load raster graphics
    protected BufferedImage loadGraphic(String imagePath) {
        BufferedImage bufferedImage;
        try {
           // ImageIO.read(Objects.requireNonNull(edu.uchicago.gerber.g1941.mvc.model.Sprite.class
            // .getResourceAsStream("/g1941/" + imagePath)));
            bufferedImage = ImageIO.read(Objects.requireNonNull(Sprite.class.getResourceAsStream("/fox/" +imagePath)));
        }
        catch (IOException e) {
            e.printStackTrace();
            bufferedImage = null;
        }
        return bufferedImage;
    }

    public void drawBoundingBox(Graphics2D g2d){
        //for debug
        for (Rectangle bound : bounds){
            g2d.draw(bound);
        }

    }

    //https://www.tabnine.com/code/java/methods/java.awt.geom.AffineTransform/rotate
    protected void renderRaster(Graphics2D g2d, BufferedImage bufferedImage) {

        if (bufferedImage ==  null) {
            System.out.println("Image not found");
            return;
        }

        int centerX = getCenter().x;
        int centerY = getCenter().y;
        int width = this.width;
        int height = this.height;
        AffineTransform oldTransform = g2d.getTransform();
        try {
            double scaleX = width * 1.0 / bufferedImage.getWidth();
            double scaleY = height * 1.0 / bufferedImage.getHeight();

            AffineTransform affineTransform = new AffineTransform( oldTransform );
            if ( centerX != 0 || centerY != 0 ) {
                affineTransform.translate( centerX, centerY );
            }
            affineTransform.scale( scaleX, scaleY );

            affineTransform.translate( -bufferedImage.getWidth() / 2.0, -bufferedImage.getHeight() / 2.0 );

            g2d.setTransform( affineTransform );

            g2d.drawImage( bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null );
        } finally {
            g2d.setTransform( oldTransform );

        }
        //drawBoundingBox(g2d);
        // Turn this on for debugging
    }
}
