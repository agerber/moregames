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
	//must be multiple of 3
	public static final int INITIAL_SPAWN_TIME = 68;

	//use for spawning and protection
	private int spawn = INITIAL_SPAWN_TIME;


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
		setRadius(32);


		//see the resources directory in the root of this project.
		//Using enums as keys is safer b/c we know the value exists when we get it later;
		//if we had hard-coded strings here and below, there's a chance we could misspell it below.
		Map<String, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(ImageState.FALCON.toString(), loadGraphic("/asteroids/imgs/falcon50.png") );
		rasterMap.put(ImageState.FALCON_THR.toString(), loadGraphic("/asteroids/imgs/falcon50thrust.png") );
		rasterMap.put(ImageState.FALCON_PRO.toString(), loadGraphic("/asteroids/imgs/falcon50protect.png") );
		rasterMap.put(ImageState.FALCON_PRO_THR.toString(), loadGraphic("/asteroids/imgs/falcon50protect_thrust.png") );
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

		//set image-state
		ImageState imageState;
		if (isProtected()){
			if (thrusting) imageState = ImageState.FALCON_PRO_THR; else imageState = ImageState.FALCON_PRO;
		}
		else { //not protected
			if (thrusting) imageState = ImageState.FALCON_THR; else imageState = ImageState.FALCON;
		}

		//cast (widen the aperture of) the graphics object to gain access to methods of Graphics2D
		//and render the image according to the image-state
		renderRaster((Graphics2D) g, getRasterMap().get(imageState.toString()));

		//draw cyan shield, and warn player of impending non-protection
		if (isProtected() && (spawn > 20 || spawn % 8 != 0)) {
			//you can add vector elements to raster graphics
			g.setColor(Color.CYAN);
			g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
		}



	}



} //end class
