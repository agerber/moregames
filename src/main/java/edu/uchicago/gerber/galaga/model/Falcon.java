package edu.uchicago.gerber.galaga.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Falcon extends Sprite {

	// ==============================================================
	// FIELDS
	// ==============================================================

	private static final double THRUST = .65;
	private final static int DEGREE_STEP = 9;
	//must be multiple of 3
	public static final int FADE_INITIAL_VALUE = 51;

	//private boolean shield = false;
	private boolean thrusting = false;
	private boolean breaking = false;
	private boolean turningRight = false;
	private boolean turningLeft = false;


	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================

	public Falcon() {

		setTeam(Team.FRIEND);

		//this is the size (radius) of the falcon
		setRadius(35);


		List<Point> pntCs = new ArrayList<>();
		// Robert Alef's awesome falcon design
		pntCs.add(new Point(0,9));
		pntCs.add(new Point(-1, 6));
		pntCs.add(new Point(-1,3));
		pntCs.add(new Point(-4, 1));
		pntCs.add(new Point(4,1));
		pntCs.add(new Point(-4,1));
		pntCs.add(new Point(-4, -2));
		pntCs.add(new Point(-1, -2));
		pntCs.add(new Point(-1, -9));
		pntCs.add(new Point(-1, -2));
		pntCs.add(new Point(-4, -2));
		pntCs.add(new Point(-10, -8));
		pntCs.add(new Point(-5, -9));
		pntCs.add(new Point(-7, -11));
		pntCs.add(new Point(-4, -11));
		pntCs.add(new Point(-2, -9));
		pntCs.add(new Point(-2, -10));
		pntCs.add(new Point(-1, -10));
		pntCs.add(new Point(-1, -9));
		pntCs.add(new Point(1, -9));
		pntCs.add(new Point(1, -10));
		pntCs.add(new Point(2, -10));
		pntCs.add(new Point(2, -9));
		pntCs.add(new Point(4, -11));
		pntCs.add(new Point(7, -11));
		pntCs.add(new Point(5, -9));
		pntCs.add(new Point(10, -8));
		pntCs.add(new Point(4, -2));
		pntCs.add(new Point(1, -2));
		pntCs.add(new Point(1, -9));
		pntCs.add(new Point(1, -2));
		pntCs.add(new Point(4,-2));
		pntCs.add(new Point(4, 1));
		pntCs.add(new Point(1, 3));
		pntCs.add(new Point(1,6));
		pntCs.add(new Point(0,9));

		setCartesians(pntCs);
	}

	@Override
	public boolean isProtected() {
		return getFade() < 255;
	}

	@SafeVarargs
	private final void accelerateDecelerate(boolean forward, List<Movable>... movableLists){


		BiConsumer<Sprite, Boolean> slowerOrFaster =  (sprite, fwd) -> {

			double forwardOrBack = fwd ? 0.5 : -0.5;
			sprite.setDeltaY(sprite.getDeltaY() + forwardOrBack);

		};

		//convert Stream<List<Movable>> to a single Stream<Movable> using flatmap, then map Movable (widen aperture) to
		// Sprite, then apply logic above
		Arrays.stream(movableLists)
				.flatMap(Collection::stream)
				.map(m -> (Sprite) m)
				.forEach(s -> slowerOrFaster.accept(s, forward));


	}

	// ==============================================================
	// METHODS
	// ==============================================================
	@Override
	public void move() {
		//super.move();

		if (isProtected()) {
			setFade(getFade() + 3);
		}

		//apply some thrust vectors using trig.
		if (thrusting) {

			//get all foes
			accelerateDecelerate(true,
					CommandCenter.getInstance().getMovFoes(),
					CommandCenter.getInstance().getMovFloaters(),
					CommandCenter.getInstance().getMovDebris()
			);
		}

		if (breaking){
			accelerateDecelerate(false,
					CommandCenter.getInstance().getMovFoes(),
					CommandCenter.getInstance().getMovFloaters(),
					CommandCenter.getInstance().getMovDebris()
			);
		}



//			double adjustX = Math.cos(Math.toRadians(getOrientation()))
//					* THRUST;
//			double adjustY = Math.sin(Math.toRadians(getOrientation()))
//					* THRUST;
//			setDeltaX(getDeltaX() + adjustX);
//			setDeltaY(getDeltaY() + adjustY);

		//rotate left
		if (turningLeft) {
//			if (getOrientation() <= 0) {
//				setOrientation(360);
//			}
//			setOrientation(getOrientation() - DEGREE_STEP);
			getCenter().move(getCenter().x - 5, getCenter().y);
		}
		//rotate right
		if (turningRight) {
//			if (getOrientation() >= 360) {
//				setOrientation(0);
//			}
//			setOrientation(getOrientation() + DEGREE_STEP);
			getCenter().move(getCenter().x + 5, getCenter().y);
		}

	} //end move



	//methods for moving the falcon
	public void rotateLeft() {
		turningLeft = true;
	}

	public void rotateRight() {
		turningRight = true;
	}

	public void stopRotating() {
		turningRight = false;
		turningLeft = false;
	}

	public void thrustOn() {
		thrusting = true;
	}

	public void thrustOff() {
		thrusting = false;
	}


	public void breakOn() {
		breaking = true;
	}

	public void breakOff() {
		breaking = false;
	}


	private int adjustColor(int colorNum, int adjust) {
		return Math.max(colorNum - adjust, 0);
	}

	@Override
	public void draw(Graphics g) {

		Color colShip;
		if (getFade() == 255) {
			colShip = getColor(); //get native color of the sprite
		}
		//flash to warn player of impending non-protection
		else if (getFade() > 220 && getFade() % 9 == 0 ){
			colShip = new Color(0, 32, 128); //dark blue
		}
		//some increasingly lighter shade of blue
		else {
			colShip = new Color(

					adjustColor(getFade(), 200), //red
					adjustColor(getFade(), 175), //green
					getFade() //blue
			);
		}

		//most Sprites do not have flames, but Falcon does
		double[] flames = { 23 * Math.PI / 24 + Math.PI / 2, Math.PI + Math.PI / 2, 25 * Math.PI / 24 + Math.PI / 2 };
		Point[] pntFlames = new Point[flames.length];

		//thrusting
		if (thrusting) {
			//the flame
			for (int nC = 0; nC < flames.length; nC++) {
				if (nC % 2 != 0) //odd
				{
					//adjust the position so that the flame is off-center
					pntFlames[nC] = new Point((int) (getCenter().x + 2
							* getRadius()
							* Math.sin(Math.toRadians(getOrientation())
							+ flames[nC])), (int) (getCenter().y - 2
							* getRadius()
							* Math.cos(Math.toRadians(getOrientation())
							+ flames[nC])));

				} else //even
				{
					pntFlames[nC] = new Point((int) (getCenter().x + getRadius()
							* 1.1
							* Math.sin(Math.toRadians(getOrientation())
							+ flames[nC])),
							(int) (getCenter().y - getRadius()
									* 1.1
									* Math.cos(Math.toRadians(getOrientation())
									+ flames[nC])));

				} //end even/odd else
			} //end for loop

			g.setColor(colShip); //flames same color as ship
			g.fillPolygon(
					Arrays.stream(pntFlames)
							.map(pnt -> pnt.x)
							.mapToInt(Integer::intValue)
							.toArray(),

					Arrays.stream(pntFlames)
							.map(pnt -> pnt.y)
							.mapToInt(Integer::intValue)
							.toArray(),

					flames.length);

		} //end if flame

		draw(g,colShip);

	} //end draw()

} //end class
