package edu.uchicago.gerber.raster_asteroids.model;

import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Data
public class Falcon extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================
	
	private static final double THRUST = .65;
	private final static int DEGREE_STEP = 9;
	public static final int INITIAL_SPAWN_TIME = 50;

	//a counter which counts down from INITIAL_SPAWN_TIME to zero (see move()). Used for determining protection
	private int spawn;

	public static final int MAX_SHIELD = 200;

	private boolean thrusting = false;
	public enum TurnState {
		IDLE, LEFT, RIGHT
	}
	private TurnState turnState = TurnState.IDLE;

	public enum ImageState {
		FALCON, //normal ship
		FALCON_THR, //normal ship thrusting
		FALCON_PRO, //protected ship (green)
		FALCON_PRO_THR //protected ship (green) thrusting
	}


	// ==============================================================
	// CONSTRUCTOR 
	// ==============================================================
	
	public Falcon() {

		setTeam(Team.FRIEND);

		//this is the radius of the falcon
		setRadius(37);



		//We use HashMap which has a seek-time of O(1)
		//See the resources directory in the root of this project for pngs.
		//Using enums as keys is safer b/c we know the value exists when we get it later;
		//if we had hard-coded strings here and below, there's a chance we could misspell it below or elsewhere.

    	Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(ImageState.FALCON, loadGraphic("/asteroids/imgs/fal/falcon125.png") );
		rasterMap.put(ImageState.FALCON_THR, loadGraphic("/asteroids/imgs/fal/falcon125_thr.png") );
		rasterMap.put(ImageState.FALCON_PRO, loadGraphic("/asteroids/imgs/fal/falcon125_PRO.png") );
		rasterMap.put(ImageState.FALCON_PRO_THR, loadGraphic("/asteroids/imgs/fal/falcon125_PRO_thr.png") );
		setRasterMap(rasterMap);


	}

	//if spawning then make invincible. You can also set conditions for power-up-shields here, etc.
	@Override
	public boolean isProtected() {
		return  spawn > 0;

	}

	// ==============================================================
	// METHODS 
	// ==============================================================
	@Override
	public void move() {
		super.move();

		if (spawn > 0) spawn--;

		//apply some thrust vectors using trig.
		if (thrusting) {
			double adjustX = Math.cos(Math.toRadians(getOrientation()))
					* THRUST;
			double adjustY = Math.sin(Math.toRadians(getOrientation()))
					* THRUST;
			setDeltaX(getDeltaX() + adjustX);
			setDeltaY(getDeltaY() + adjustY);
		}

		switch (turnState){
			case LEFT:
				if (getOrientation() <= 0) {
					setOrientation(360);
				}
				setOrientation(getOrientation() - DEGREE_STEP);
				break;
			case RIGHT:
				if (getOrientation() >= 360) {
					setOrientation(0);
				}
				setOrientation(getOrientation() + DEGREE_STEP);
				break;
			default:
				//do nothing

		}

	}

	//this is a raster implementation of draw()
	@Override
	public void draw(Graphics g) {

		//set local image-state
		ImageState imageState;
		if (isProtected()){
			if (thrusting) imageState = ImageState.FALCON_PRO_THR; else imageState = ImageState.FALCON_PRO;
		}
		else { //not protected
			if (thrusting) imageState = ImageState.FALCON_THR; else imageState = ImageState.FALCON;
		}

		//cast (widen the aperture of) the graphics object to gain access to methods of Graphics2D
		//and render the image according to the image-state
		renderRaster((Graphics2D) g, getRasterMap().get(imageState));

		//you can also add vector elements to raster graphics
		//draw cyan shield, and warn player of impending non-protection
//		if (isProtected() && !(spawn <= 21 && spawn % 7 == 0)) {
//
//			g.setColor(Color.CYAN);
//			g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
//		}



	}



} //end class
