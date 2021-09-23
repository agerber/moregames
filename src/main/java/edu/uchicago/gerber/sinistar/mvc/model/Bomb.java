package edu.uchicago.gerber.sinistar.mvc.model;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Bomb extends Sprite {

	private final int FIRE_POWER = 20;
	private final int MAX_EXPIRE = 50;
	private final int BOMB_SIZE = 3;
	private final int SINISTAR_LOCKON_DIST = 800;
	private int currentBombNum;

	public Bomb(Falcon fal) {

		super();
		setTeam(Team.FRIEND);
		setExpire(MAX_EXPIRE);
		setRadius(BOMB_SIZE);
		setMinSpeed(FIRE_POWER);
		setMaxSpeed(FIRE_POWER);

		setDeltaX(fal.getDeltaX()
				+ Math.cos(Math.toRadians(fal.getOrientation())) * FIRE_POWER);
		setDeltaY(fal.getDeltaY()
				+ Math.sin(Math.toRadians(fal.getOrientation())) * FIRE_POWER);
		setCenter(fal.getCenter());

		setOrientation(fal.getOrientation());
		currentBombNum = 1;
	}
	
	@Override
	public void move() {
		super.move();
		if(getCenter().distance(CommandCenter.getInstance().getSinistar().getCenter()) < SINISTAR_LOCKON_DIST){
			lockon(CommandCenter.getInstance().getSinistar().getCenter(), FIRE_POWER);
		}

		if (getExpire() == 0) {
			CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
		}
		else {
			setExpire(getExpire() - 1);
		}
	}
	
	@Override
	public void draw(Graphics g){
		BufferedImage img = CommandCenter.getInstance().getBomb(currentBombNum);
		drawImage(img, g);
		currentBombNum += 1;
		if (currentBombNum >= 4){
			currentBombNum = 1;
		}
	}
}
