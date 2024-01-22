package edu.uchicago.gerber.fox.mvc.model;

import edu.uchicago.gerber.fox.mvc.controller.Game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Fox extends Sprite {

    //static fields
    private int stepTimer = 0; //̤count step when running, used to swap images
    private int jumpTimer = 0; //count step when jumping, used to swap images

    private final int lowestHeight = 685; // lowest height on the ground
    private final int GRAVITY_CONSTANT = 3; // used to control jumping
    private final int MAX_JUMP_TIME = 11;// used to control jumping
    public static final int INITIAL_SPAWN_TIME = 46;
    private int invisible;


    //fox's motion states
    public enum MotionState{
        RUNNING,
        JUMPING,
    }
    private MotionState currentState = MotionState.RUNNING;


    public Fox(){
        super(124, 43);
        setTeam(Team.FRIEND);
        setCenter(new Point(Game.DIM.width / 5, getCurrentHeight()));
        getBounds().add(new Rectangle(getCenter().x, getCenter().y - 21, 62, 32));
        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(0, loadGraphic("/imgs/fox/run1.png") );
        rasterMap.put(1, loadGraphic("/imgs/fox/run2.png") );
        rasterMap.put(2, loadGraphic("/imgs/fox/run3.png") );
        rasterMap.put(3, loadGraphic("/imgs/fox/run4.png") );
        rasterMap.put(4, loadGraphic("/imgs/fox/jump1.png") );
        rasterMap.put(5, loadGraphic("/imgs/fox/jump2.png") );
        rasterMap.put(6, loadGraphic("/imgs/fox/jump3.png") );
        rasterMap.put(7, loadGraphic("/imgs/fox/jump4.png") );
        rasterMap.put(8, loadGraphic("/imgs/fox/jump5.png") );
        rasterMap.put(9, null );

        setRasterMap(rasterMap);

    }
    @Override
    public void move(){
        super.move();

        if (invisible > 0) invisible--;
        if(currentState == MotionState.JUMPING){
            jumpTimer++;
            // fox is jumping currently
            if(jumpTimer == MAX_JUMP_TIME){//the jumpspeed decreases when approaching JUMP_HIGHT, it might never reach it
                setDeltaY(GRAVITY_CONSTANT); //time = 0
            }else{
                // change speed by GRAVITY_CONSTANT
                double currentSpeed = getDeltaY();
                setDeltaY(currentSpeed + GRAVITY_CONSTANT);
            }

            if(getCenter().y > lowestHeight){
                setCurrentState("RUNNING"); // fox lands
                setDeltaY(0);
                jumpTimer = 0;
                int centerX = getCenter().x;
                setCenter(new Point(centerX, lowestHeight));
                getBounds().remove(0);
                getBounds().add(new Rectangle(getCenter().x, getCenter().y - 21, 62, 32));
            }
        }
    }

    public int getCurrentHeight(){
        return lowestHeight;
    }


    public boolean isJumping() {

        if(currentState == MotionState.JUMPING){
            return true;
        }
        return false;

    }

    public void setInvisible(int num){
        invisible = num;
    }
    public int getMaxSpeed(){
        return MAX_JUMP_TIME * GRAVITY_CONSTANT;
    }

    public void setCurrentState(String state){
        if(state.equals("JUMPING")){
            currentState = MotionState.JUMPING;
        }else if(state.equals("RUNNING")){
            currentState = MotionState.RUNNING;
        }
    }
    @Override
    public void draw(Graphics g){
        if (invisible > 0){
            renderRaster((Graphics2D) g, getRasterMap().get(9));
        }
        else if(currentState == MotionState.RUNNING){
            renderRaster((Graphics2D) g, getRasterMap().get(stepTimer));
            if (getLifeSpan() % 3 == 0) {//times 3 to make fox moves slower
                stepTimer++;
                stepTimer = stepTimer % 4;//4 running images in total
            }
        }else{
            //fox jumping
            double currentSpeed = getDeltaY();
            int index;

            if(currentSpeed < 0){
                //jumping up
                index = 5 + (int)currentSpeed * 5 / getMaxSpeed();
                if(index == 5){
                    index--; // jumpTimer range [0,4]
                }
            }else{
                if((int)currentSpeed > 4 * (getMaxSpeed()) / 5){
                    index = 0;
                }else{
                    index = 4;
                }
            }
            renderRaster((Graphics2D) g, getRasterMap().get(index + 4));

        }

    }

}
