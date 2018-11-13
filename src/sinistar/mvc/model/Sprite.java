package sinistar.mvc.model;

import sinistar.mvc.controller.Game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Sprite implements Movable {
	//the center-point of this sprite
	private Point pntCenter;
	//this causes movement; change in x and change in y
	private double dDeltaX, dDeltaY;
	//every sprite needs to know about the size of the gaming environ
	private Dimension dim; //dim of the gaming environment

	//we need to know what team we're on
	private Team mTeam;

	//the radius of circumscibing circle
	private int nRadius;

	private int maxSpeed;
	private int minSpeed;

	private int nOrientation;
	private int nExpiry; //natural mortality (short-living objects)
	//the color of this sprite
	private Color col;

	//radial coordinates
	//this game uses radial coordinates to render sprites
	public double[] dLengths;
	public double[] dDegrees;
	

	//fade value for fading in and out
	private int nFade;

	//these are used to draw the polygon. You don't usually need to interface with these
	private Point[] pntCoords; //an array of points used to draw polygon
	private int[] nXCoords;
	private int[] nYCoords;

	private int spriteScore;


	@Override
	public Team getTeam() {
		//default
	  return mTeam;
	}

	public void setTeam(Team team){
		mTeam = team;
	}

	public void move() {

		Point pnt = getCenter();
		double dX = pnt.x + getDeltaX();
		double dY = pnt.y + getDeltaY();
        if (!(this instanceof Falcon)) {
            dX -= CommandCenter.getInstance().getFalcon().getDeltaX();
            dY -= CommandCenter.getInstance().getFalcon().getDeltaY();
        }
        setCenter(new Point((int) dX, (int) dY));

		//this just keeps the sprite inside the bounds of the frame
		if (pnt.x > Game.DIMENSION_SCALAR * getDim().width) {
			setCenter(new Point(1, pnt.y));
		} else if (pnt.x < 0) {
			setCenter(new Point(Game.DIMENSION_SCALAR * getDim().width - 1, pnt.y));
		}

		if (pnt.y > Game.DIMENSION_SCALAR * getDim().height) {
			setCenter(new Point(pnt.x, 1));
		} else if (pnt.y < 0) {
			setCenter(new Point(pnt.x,  Game.DIMENSION_SCALAR * getDim().height - 1));
		}
	}

	public void setRandomVelocity() {
		int nDX = Game.R.nextInt(maxSpeed) + minSpeed;
		if(nDX % 2 ==0)
			nDX = -nDX;
		setDeltaX(nDX);

		//random delta-y
		int nDY = Game.R.nextInt(maxSpeed) + minSpeed;
		if(nDY % 2 == 0)
			nDY = -nDY;
		setDeltaY(nDY);
	}

	public void lockon(Point targetPnt, int speed) {
        double distX = targetPnt.getX() - getCenter().getX();
        double distY = targetPnt.getY() - getCenter().getY();
        double theta = Math.atan2(distY, distX);
        setDeltaY(speed * Math.sin(theta));
        setDeltaX(speed * Math.cos(theta));
    }

	public Sprite() {

	//you can override this and many more in the subclasses
		setDim(Game.DIM);
		setColor(Color.white);
		setCenter(new Point(Game.R.nextInt(Game.DIMENSION_SCALAR * Game.DIM.width),
				Game.R.nextInt(Game.DIMENSION_SCALAR * Game.DIM.height)));
		setSpriteScore(0);
	}

	public void drawImage(BufferedImage img, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.toRadians(getOrientation()), img.getWidth()/2, img.getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		img = op.filter(img, null);
		g2d.drawImage(img, getCenter().x - getRadius(), getCenter().y - getRadius(), null);
	}

	public void setExpire(int n) {
		nExpiry = n;
	}

	public double[] getLengths() {
		return this.dLengths;
	}

	public void setLengths(double[] dLengths) {
		this.dLengths = dLengths;
	}

	public double[] getDegrees() {
		return this.dDegrees;
	}

	public void setDegrees(double[] dDegrees) {
		this.dDegrees = dDegrees;
	}

	public Color getColor() {
		return col;
	}

	public void setColor(Color col) {
		this.col = col;

	}

	public int points() {
		//default is zero
		return 0;
	}

	public int getExpire() {
		return nExpiry;
	}

	public int getOrientation() {
		return nOrientation;
	}

	public void setOrientation(int n) {
		nOrientation = n;
	}

	public void setDeltaX(double dSet) {
		dDeltaX = dSet;
	}

	public void setDeltaY(double dSet) {
		dDeltaY = dSet;
	}

	public double getDeltaY() {
		return dDeltaY;
	}

	public double getDeltaX() {
		return dDeltaX;
	}

	public int getRadius() {
		return nRadius;
	}

	public void setRadius(int n) {
		nRadius = n;
	}

	public Dimension getDim() {
		return dim;
	}

	public void setDim(Dimension dim) {
		this.dim = dim;
	}

	public Point getCenter() {
		return pntCenter;
	}

	public void setCenter(Point pntParam) {
		pntCenter = pntParam;
	}


	public void setYcoord(int nValue, int nIndex) {
		nYCoords[nIndex] = nValue;
	}

	public void setXcoord(int nValue, int nIndex) {
		nXCoords[nIndex] = nValue;
	}
	
	
	public int getYcoord( int nIndex) {
		return nYCoords[nIndex];// = nValue;
	}

	public int getXcoord( int nIndex) {
		return nXCoords[nIndex];// = nValue;
	}

	public int[] getXcoords() {
		return nXCoords;
	}

	public int[] getYcoords() {
		return nYCoords;
	}
	
	
	public void setXcoords( int[] nCoords) {
		 nXCoords = nCoords;
	}

	public void setYcoords(int[] nCoords) {
		 nYCoords =nCoords;
	}

	protected double hypot(double dX, double dY) {
		return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
	}

	
	//utility function to convert from cartesian to polar
	//since it's much easier to describe a sprite as a list of cartesean points
	//sprites (except Asteroid) should use the cartesean technique to describe the coordinates
	//see Falcon or Bullet constructor for examples
	protected double[] convertToPolarDegs(ArrayList<Point> pntPoints) {

	   double[] dDegs = new double[pntPoints.size()];

		int nC = 0;
		for (Point pnt : pntPoints) {
			dDegs[nC++]=(Math.toDegrees(Math.atan2(pnt.y, pnt.x))) * Math.PI / 180 ;
		}
		return dDegs;
	}
	//utility function to convert to polar
	protected double[] convertToPolarLens(ArrayList<Point> pntPoints) {

		double[] dLens = new double[pntPoints.size()];

		//determine the largest hypotenuse
		double dL = 0;
		for (Point pnt : pntPoints)
			if (hypot(pnt.x, pnt.y) > dL)
				dL = hypot(pnt.x, pnt.y);

		int nC = 0;
		for (Point pnt : pntPoints) {
			if (pnt.x == 0 && pnt.y > 0) {
				dLens[nC] = hypot(pnt.x, pnt.y) / dL;
			} else if (pnt.x < 0 && pnt.y > 0) {
				dLens[nC] = hypot(pnt.x, pnt.y) / dL;
			} else {
				dLens[nC] = hypot(pnt.x, pnt.y) / dL;
			}
			nC++;
		}

		// holds <thetas, lens>
		return dLens;

	}

	protected void assignPolarPoints(ArrayList<Point> pntCs) {
		setDegrees(convertToPolarDegs(pntCs));
		setLengths(convertToPolarLens(pntCs));

	}

	@Override
    public void draw(Graphics g) {
        nXCoords = new int[dDegrees.length];
        nYCoords = new int[dDegrees.length];
        //need this as well
        pntCoords = new Point[dDegrees.length];

        for (int nC = 0; nC < dDegrees.length; nC++) {
            nXCoords[nC] =    (int) (getCenter().x + getRadius()
                    * dLengths[nC]
                    * Math.sin(Math.toRadians(getOrientation()) + dDegrees[nC]));
            nYCoords[nC] =    (int) (getCenter().y - getRadius()
                    * dLengths[nC]
                    * Math.cos(Math.toRadians(getOrientation()) + dDegrees[nC]));


            //need this line of code to create the points which we will need for debris
            pntCoords[nC] = new Point(nXCoords[nC], nYCoords[nC]);
        }

        g.setColor(getColor());
        g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
    }

	public Point[] getObjectPoints() {
		return pntCoords;
	}
	
	public void setObjectPoints(Point[] pntPs) {
		 pntCoords = pntPs;
	}
	
	public void setObjectPoint(Point pnt, int nIndex) {
		 pntCoords[nIndex] = pnt;
	}

	public int getFadeValue() {
		return nFade;
	}

	public void setFadeValue(int n) {
		nFade = n;
	}

	public int getSpriteScore() {
		return spriteScore;
	}

	public void setSpriteScore(int spriteScore) {
		this.spriteScore = spriteScore;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public int getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(int minSpeed) {
		this.minSpeed = minSpeed;
	}
}
