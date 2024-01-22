package edu.uchicago.gerber.fox.mvc.model;

import edu.uchicago.gerber.fox.mvc.controller.CommandCenter;
import edu.uchicago.gerber.fox.mvc.controller.Game;
import edu.uchicago.gerber.fox.mvc.controller.GameOp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Coin extends Sprite{
    private final double MOVE_SPEED = 10.0;
    private int stepTimer = 0;
    public boolean alive = true;
    public static final int SPAWN_NEW_COIN = Game.FRAMES_PER_SECOND / 4;
    public Coin(){
        super(35, 35);
        setTeam(Team.COIN);
        setCenter(new Point(1099,670));
        setDeltaX(-MOVE_SPEED);
        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(0, loadGraphic("/imgs/utils/coin1.png") );
        rasterMap.put(1, loadGraphic("/imgs/utils/coin2.png") );
        rasterMap.put(2, loadGraphic("/imgs/utils/coin3.png") );
        rasterMap.put(3, loadGraphic("/imgs/utils/coin4.png") );
        setRasterMap(rasterMap);
        getBounds().add(new Rectangle(getCenter().x - 16, getCenter().y - 16,35 , 35));
    }

    @Override
    public void move(){
        super.move();
    }

    @Override
    public void draw(Graphics g){
        if(alive){
            switch (stepTimer){
                case 0:
                    setWidth(5);
                    break;
                case 1:
                    setWidth(29);
                    break;
                case 2:
                    setWidth(35);
                    break;
                case 3:
                    setWidth(29);
                    break;
                default:
                    break;
            }
            renderRaster((Graphics2D) g, getRasterMap().get(stepTimer));
            if (getLifeSpan() % 2 == 0) {//times 2 to make coin moves slower
                stepTimer++;
                stepTimer = stepTimer % 4;//4 images in total
            }
        }else{
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }

    }
}

