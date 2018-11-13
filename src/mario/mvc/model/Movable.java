package mario.mvc.model;

import java.awt.*;

public interface Movable {

	public static enum Team {
		FRIEND, FOE, PLATFORM, BACKGROUND
	}

	//for the game to move and draw movable objects
	public void move();
	public void setLeftDirection();
    public void setRightDirection();
    public void setDead();
    public boolean isDead();
    public int getDeadTimeLeft();
	public void draw(Graphics g);
    public void initPos();

	//for collision detection
	public Point getCenter();
	public Team getTeam();
	public int getHeight();
	public int getWidth();
    public int getWorth();

} //end Movable
