package edu.uchicago.gerber.raster_asteroids.model;


import edu.uchicago.gerber.raster_asteroids.controller.Game;

import java.awt.*;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class Asteroid extends Sprite {

	//radius of a large asteroid
	private final int LARGE_RADIUS = 100;

	//size determines if the Asteroid is Large (0), Medium (1), or Small (2)
	//when you explode a Large asteroid, you should spawn 2 or 3 medium asteroids
	//same for medium asteroid, you should spawn small asteroids
	//small asteroids get blasted into debris, but do not spawn anything
	public Asteroid(int size){

		//a size of zero is a big asteroid
		//a size of 1 or 2 is med or small asteroid respectively. See getSize() method.
		if (size == 0) setRadius(LARGE_RADIUS);
		else setRadius(LARGE_RADIUS/(size * 2));

		//Asteroid is FOE
		setTeam(Team.FOE);
		setColor(Color.WHITE);

		//the spin will be either plus or minus 0-9
		setSpin(somePosNegValue(10));
		//random delta-x
		setDeltaX(somePosNegValue(10));
		//random delta-y
		setDeltaY(somePosNegValue(10));

		setCartesians(genRandomVertices());

	}



	//overloaded so we can spawn smaller asteroids from an exploding one
	public Asteroid(Asteroid astExploded){
		//calls the other constructor: Asteroid(int size)
		this(astExploded.getSize() + 1);
		setCenter(astExploded.getCenter());
		int newSmallerSize = astExploded.getSize() + 1;
		//random delta-x : inertia + the smaller the asteroid, the faster its possible speed
		setDeltaX(astExploded.getDeltaX() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));
		//random delta-y : inertia + the smaller the asteroid, the faster its possible speed
		setDeltaY(astExploded.getDeltaY() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));

	}

	public int getSize(){
		switch (getRadius()) {
			case LARGE_RADIUS:
				return 0;
			case LARGE_RADIUS / 2:
				return 1;
			case LARGE_RADIUS / 4:
				return 2;
			default:
				return 0;
		}
	}



	  private Point[] genRandomVertices(){

		  //6.283 is the max radians
		  final int MAX_RADIANS_X1000 =6283;
		  //when casting from double to int, we truncate and lose precision, so best to be generous with multiplier
		  final int PRECISION_MULTIPLIER = 1000;

		  Supplier<PolarPoint> polarPointSupplier = () -> {
			  double r = (750 + Game.R.nextInt(200)) / 1000.0; //number between 0.75 and 0.999
			  double theta = Game.R.nextInt(MAX_RADIANS_X1000) / 1000.0; // number between 0 and 6.282
		  	  return new PolarPoint(r,theta);
		  };

		  BiFunction<PolarPoint, Sprite, Point> polarToCartTransform = (pp, spr) -> new Point(
				  (int) (spr.getCenter().x + pp.getR() * spr.getRadius() * PRECISION_MULTIPLIER
						  * Math.sin(Math.toRadians(spr.getOrientation())
						  + pp.getTheta())),
				  (int) (spr.getCenter().y - pp.getR() * spr.getRadius() * PRECISION_MULTIPLIER
						  * Math.cos(Math.toRadians(spr.getOrientation())
						  + pp.getTheta())));

		 //random number of vertices
		 final int VERTICES = Game.R.nextInt( 7 ) + 25;

		 return Stream.generate(polarPointSupplier)
				 .limit(VERTICES)
				 //I used the 'new' keyword to generate the anon-inner class; you can convert to lambda.
				 .sorted(new Comparator<PolarPoint>() {
							@Override
							public int compare(PolarPoint pp1, PolarPoint pp2) {
								return  pp1.getTheta().compareTo(pp2.getTheta());
							}
						})
				 .map(pp -> polarToCartTransform.apply(pp, this))
				 .toArray(Point[]::new);


	  }

	@Override
	public void draw(Graphics g) {
		renderVector(g);
	}



}
