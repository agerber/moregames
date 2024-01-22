package edu.uchicago.gerber.fox.mvc.model;

import edu.uchicago.gerber.fox.mvc.controller.CommandCenter;
import edu.uchicago.gerber.fox.mvc.controller.GameOp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

//Background Image is a Movable class as the image move from right to left
//the game frame size is 1100 * 900, I use an image of size 2200 * 900 and set the center (1099, 450) to make it move seamlessly
//Pits are considered parts of the background and this class is responsible for spawning pits
public class BackGround extends Sprite{
    private final double MOVE_SPEED = 10.0;

    public static Map<Integer, Pit> pits = new HashMap<>();
    // Pits on background mapped with its location index


    public BackGround(Point center){
        super(2200, 900);
        setCenter(center);
        setDeltaX(-MOVE_SPEED);
        setTeam(Team.FRIEND);
        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(0, loadGraphic("/imgs/background.png") );

        setRasterMap(rasterMap);

        //spawnPits(1);
        // cannot spawn the pits in the constructor, the singleton CommandCenter would try to init again and again
    }
    public void clearPits(){
        pits.clear();
    }

    public boolean hasPits(int idx){
        return pits.containsKey(idx);
    }
    private void spawnPits(int times){

        for (int i = 0; i < times; i++){

            int intervalidx = ThreadLocalRandom.current().nextInt(12, 22);

            if (!pits.containsKey(intervalidx)){
                Pit current = spawnOnePit();
                pits.put(intervalidx, current);
                current.setIdx(intervalidx);
                //set center for the pit
                int centerX = (intervalidx - 11) * 100 + this.getCenter().x + 50;
                int centerY = 685 + current.getHeight()/2;
                current.setCenter(new Point(centerX, centerY));
                current.setDeltaX(-MOVE_SPEED);
                //set bounding box for pits
                current.getBounds().add(new Rectangle(centerX , centerY - current.getHeight()/2 + 5, 50, 70));
            }
        }
    }

    private Pit spawnOnePit(){
        int type = ThreadLocalRandom.current().nextInt(0, 3);
        int width = 100;
        int height = 45;
        switch (type){
            case 0:
                height = 144;
                break;
            case 1:
                height = 144;
                break;
            case 2:
                height = 120;
                break;
            default:
                break;
        }
        Pit current = new Pit(width, height, type);
        CommandCenter.getInstance().getOpsQueue().enqueue(current, GameOp.Action.ADD);

        return current;
    }

    private void drawPits(Graphics g){
        for(Integer i: pits.keySet()){
            Pit current = pits.get(i);
            int centerX = current.getCenter().x;
            if(centerX + 50 > 0){
                current.renderRaster((Graphics2D) g, current.getRasterMap().get(0));
            }

        }
    }



    @Override
    public void move(){
        if(getCenter().x < 0){
            super.move();
            spawnPits(1);
        }else{
            super.move();
        }

    }

    @Override
    public void draw(Graphics g){
        renderRaster((Graphics2D) g, getRasterMap().get(0));
        drawPits(g);
    }

}
