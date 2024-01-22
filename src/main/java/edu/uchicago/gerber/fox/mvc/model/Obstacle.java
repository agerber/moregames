package edu.uchicago.gerber.fox.mvc.model;

import edu.uchicago.gerber.fox.mvc.controller.CommandCenter;
import edu.uchicago.gerber.fox.mvc.controller.Game;
import edu.uchicago.gerber.fox.mvc.controller.GameOp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Obstacle extends Sprite {


    private final double MOVE_SPEED = 10.0;
    public boolean alive = true;
    public static final int SPAWN_NEW_OBSTACLE = Game.FRAMES_PER_SECOND * 2;



    public Obstacle(Point center, int width, int height, int type){
        super(width, height);
        setTeam(Team.FOE);
        setCenter(center);
        setDeltaX(-MOVE_SPEED);
        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        switch (type){
            case 0:
                rasterMap.put(0, loadGraphic("/imgs/obstacles/cactus1.png") );
                getBounds().add(new Rectangle(getCenter().x - 6, getCenter().y - 55, 11, 109));
                getBounds().add(new Rectangle(getCenter().x - 22, getCenter().y - 6, 14, 29));
                getBounds().add(new Rectangle(getCenter().x + 10, getCenter().y - 33, 10, 40));
                break;
            case 1:
                rasterMap.put(0, loadGraphic("/imgs/obstacles/cactus2.png") );
                getBounds().add(new Rectangle(getCenter().x - 9, getCenter().y - 59, 12, 117));
                getBounds().add(new Rectangle(getCenter().x - 24, getCenter().y - 24, 10, 39));
                getBounds().add(new Rectangle(getCenter().x, getCenter().y - 30, 20, 70));
                break;
            case 2:
                rasterMap.put(0, loadGraphic("/imgs/obstacles/stone1.png") );
                getBounds().add(new Rectangle(getCenter().x - 24, getCenter().y - 26,32 , 23));
                getBounds().add(new Rectangle(getCenter().x - 33, getCenter().y - 6,69 , 31));
                break;
            case 3:
                rasterMap.put(0, loadGraphic("/imgs/obstacles/stone2.png") );
                getBounds().add(new Rectangle(getCenter().x - 34, getCenter().y - 6,71 , 30));
                getBounds().add(new Rectangle(getCenter().x - 18, getCenter().y - 22,32 , 16));
                break;
            case 4:
                rasterMap.put(0, loadGraphic("/imgs/obstacles/stone3.png") );
                getBounds().add(new Rectangle(getCenter().x - 29, getCenter().y - 4, 58 , 21));
                getBounds().add(new Rectangle(getCenter().x - 14, getCenter().y - 18,24 , 15));
                break;
            default:
                break;
        }

        setRasterMap(rasterMap);
    }

    @Override
    public void move(){
        super.move();
    }

    @Override
    public void draw(Graphics g){
        if (alive) {
            //System.out.println("render obst raster");
            renderRaster((Graphics2D) g, getRasterMap().get(0));
        }else{
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
    }

}
