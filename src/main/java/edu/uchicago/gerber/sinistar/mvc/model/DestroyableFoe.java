package edu.uchicago.gerber.sinistar.mvc.model;

/**
 * Created by michaelvasiliou on 11/19/16.
 */
public class DestroyableFoe extends Sprite{
    private int health;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean mineCrystal(){
        return false;
    };
}
