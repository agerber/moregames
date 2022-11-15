package edu.uchicago.gerber.galaga.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewShipFloater extends Sprite {


	public NewShipFloater() {

		setTeam(Team.FLOATER);

		setExpiry(251);
		setRadius(50);
		setColor(Color.BLUE);

		//set random DeltaX
		setDeltaX(somePosNegValue(10));

		//set rnadom DeltaY
		setDeltaY(somePosNegValue(10));
		
		//set random spin
		setSpin(somePosNegValue(10));

		//cartesian points which define the shape of the polygon
		List<Point> pntCs = new ArrayList<>();
		pntCs.add(new Point(5, 5));
		pntCs.add(new Point(4,0));
		pntCs.add(new Point(5, -5));
		pntCs.add(new Point(0,-4));
		pntCs.add(new Point(-5, -5));
		pntCs.add(new Point(-4,0));
		pntCs.add(new Point(-5, 5));
		pntCs.add(new Point(0,4));

		setCartesians(pntCs);
	}




}
