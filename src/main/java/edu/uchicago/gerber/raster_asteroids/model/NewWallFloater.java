package edu.uchicago.gerber.raster_asteroids.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewWallFloater extends Sprite {


	public NewWallFloater() {

		setTeam(Team.FLOATER);

		setExpiry(250);
		setRadius(50);
		setColor(Color.ORANGE);

		//set random DeltaX
		setDeltaX(somePosNegValue(10));

		//set random DeltaY
		setDeltaY(somePosNegValue(10));
		
		//set random spin
		setSpin(somePosNegValue(10));

		//cartesian points which define the shape of the polygon
		List<Point> listPoints = new ArrayList<>();
		listPoints.add(new Point(5, 5));
		listPoints.add(new Point(4,0));
		listPoints.add(new Point(5, -5));
		listPoints.add(new Point(0,-4));
		listPoints.add(new Point(-5, -5));
		listPoints.add(new Point(-4,0));
		listPoints.add(new Point(-5, 5));
		listPoints.add(new Point(0,4));

		setCartesians(CommandCenter.pointsListToArray(listPoints));

	}

	@Override
	public void draw(Graphics g) {
		renderVector(g);
	}




}
