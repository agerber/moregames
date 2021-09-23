package edu.uchicago.gerber.sinistar.mvc.model;



import edu.uchicago.gerber.sinistar.mvc.controller.Game;

import java.awt.*;
import java.util.ArrayList;

public class Crystal extends Sprite {

	private static final int CRYSTAL_RADIUS = 3;
	private static final int EXPIRE_FRAMES = 250;
	private static final int MAX_SPEED = 10;
	private static final int LOCKON_SPEED = 18;
	private static final int CRYSTAL_TRACK_DIST = 200;
	private static final int CRYSTAL_SCORE = 100;
	private boolean lockedon = false;
	private int nSpin;

	public Crystal(Point paramCenter) {
		super();
		setSpriteScore(CRYSTAL_SCORE);
		setTeam(Team.FLOATER);
		setMinSpeed(MAX_SPEED);
		setMaxSpeed(MAX_SPEED);
		ArrayList<Point> pntCs = new ArrayList<Point>();
		pntCs.add(new Point(1, 1));
		pntCs.add(new Point(1, -1));
		pntCs.add(new Point(-1, -1));
		pntCs.add(new Point(-1, 1));

		assignPolarPoints(pntCs);
		setExpire(EXPIRE_FRAMES);
		setRadius(CRYSTAL_RADIUS);
		setColor(Color.LIGHT_GRAY);
		setRandomVelocity();
		setCenter(paramCenter);
		setOrientation(Game.R.nextInt(360));
	}

	@Override
	public void move() {
		if (getCenter().distance(CommandCenter.getInstance().getFalcon().getCenter()) < CRYSTAL_TRACK_DIST) {
			lockon(CommandCenter.getInstance().getFalcon().getCenter(), LOCKON_SPEED);
			lockedon = true;
		}
		else if (lockedon) {
			setRandomVelocity();
			lockedon = false;
		}
		setOrientation(getOrientation() + getSpin());

		//adding expire functionality
		if (getExpire() == 0)
			CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
		else
			setExpire(getExpire() - 1);
		super.move();
	}

	public int getSpin() {
		return this.nSpin;
	}

	public void setSpin(int nSpin) {
		this.nSpin = nSpin;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
		g.setColor(Color.WHITE);
		g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
	}
}