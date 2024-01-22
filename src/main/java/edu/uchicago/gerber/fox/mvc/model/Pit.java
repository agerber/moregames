package edu.uchicago.gerber.fox.mvc.model;

import edu.uchicago.gerber.fox.mvc.controller.CommandCenter;
import edu.uchicago.gerber.fox.mvc.controller.GameOp;
import lombok.Getter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Pit extends Sprite{
    private final double MOVE_SPEED = 10.0;
    @Getter
    private int idx = 0;
    public Pit(int width, int height, int type){
        super(width, height);
        setTeam(Team.FOE);
        //center set when sprawling
        //bounds set after center set
        setDeltaX(-MOVE_SPEED);
        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        switch (type){
            case 0:
                rasterMap.put(0, loadGraphic("/imgs/utils/pit1.png") );
                break;
            case 1:
                rasterMap.put(0, loadGraphic("/imgs/utils/pit2.png") );
                break;
            case 2:
                rasterMap.put(0, loadGraphic("/imgs/utils/pit3.png") );
                break;
            default:
                break;
        }
        setRasterMap(rasterMap);
    }
    public void setIdx(int i){
        idx = i;
    }

    @Override
    public void move(){
        //super.move(); not overwriting super() because the pits need to be out of game frame
        setCenter(new Point(getCenter().x+(int)getDeltaX(), getCenter().y));
        for (Rectangle bound: getBounds()){
            bound.translate((int)getDeltaX(), (int)getDeltaY());
            //not very precise
        }
        //remove
        if (getCenter().x + 50  < 0){
            System.out.println("inside Pis: center x"+getCenter().x);
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
            System.out.println("pit "+ " removed");
            int currentIdx = getIdx();
            CommandCenter.getInstance().getBgImage1().pits.remove(currentIdx);
        }
    }

    @Override
    public void draw(Graphics g){
        // draw by background image
    }
}
