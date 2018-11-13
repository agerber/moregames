package sinistar.mvc.model;

import java.awt.*;
import java.util.ArrayList;


public class Bullet extends Sprite {

	  private final int FIRE_POWER = 35;
	  private static final int EXPIRE_FRAMES = 15;
	  private static final int BULLET_SIZE = 8;
	
public Bullet(Sprite origin){
		super();
		if (origin instanceof Falcon) {
			setTeam(Team.FRIEND);
		}
		else {
			setTeam(Team.FOE);
		}
		//defined the points on a cartesean grid
		ArrayList<Point> pntCs = new ArrayList<Point>();
		
		pntCs.add(new Point(0,3)); //top point
		pntCs.add(new Point(1,-1));
		pntCs.add(new Point(0,-2));
		pntCs.add(new Point(-1,-1));

		assignPolarPoints(pntCs);

	    setExpire(EXPIRE_FRAMES);
	    setRadius(BULLET_SIZE);
		setMinSpeed(FIRE_POWER);
		setMaxSpeed(FIRE_POWER);
	    

	    //everything is relative to the falcon ship that fired the bullet
	    setDeltaX( origin.getDeltaX() +
	               Math.cos( Math.toRadians( origin.getOrientation() ) ) * FIRE_POWER );
	    setDeltaY( origin.getDeltaY() +
	               Math.sin( Math.toRadians( origin.getOrientation() ) ) * FIRE_POWER );
	    setCenter( origin.getCenter() );

	    //set the bullet orientation to the falcon (ship) orientation
	    setOrientation(origin.getOrientation());
	}

	@Override
	public void move(){

		super.move();
		if (getExpire() == 0)
			CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
		else
			setExpire(getExpire() - 1);
	}

	@Override
	public void draw(Graphics g){
		super.draw(g);
		g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
	}
}
