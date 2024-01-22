package edu.uchicago.gerber.g1941.mvc.model.weapon;

import edu.uchicago.gerber.g1941.mvc.model.Sprite;

import java.awt.*;

public abstract class Bullet extends Sprite {
    // The bullet abstract class

    public int fireForce; // Decrease AircraftFriend's HP by fireForce
    private String imagePath;

    public void setImagePath(String imagePath) {this.imagePath = imagePath;}
    public String getImagePath() {return this.imagePath;}
    public void setFireForce(int fireForce) {this.fireForce = fireForce;}
    public int getFireForce() {return this.fireForce;}


    @Override
    public void draw(Graphics g) {
           renderVector(g);
    }



}
