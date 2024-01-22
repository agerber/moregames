package edu.uchicago.gerber.g1941.mvc.model.airplane;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.controller.GameOp;
import edu.uchicago.gerber.g1941.mvc.controller.Sound;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.weapon.BulletFriend;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;

@Data
public class AircraftFriend extends Aircraft {
	// This is player's Aircraft

	public final static int TURN_STEP = 11;
	public static final int INITIAL_SPAWN_TIME = 46; //number of frames that the falcon will be protected after a spawn
	public static final int MIN_RADIUS = 28;
	public static final int HORIZONTAL_SHIFT = 20;
    public static final int VERTICAL_SHIFT = 20;
	public static final int MAX_NUM_MISSILE = 6; // W.
	private int shield; //instance fields (getters/setters provided by Lombok @Data above)
	private int nukeMeter;
	private int invisible;
	private boolean maxSpeedAttained;
	//showLevel is not germane to the Falcon. Rather, it controls whether the level is shown in the middle of the
	// screen. However, given that the Falcon reference is never null, and that a Falcon is a Movable whose move/draw
	// methods are being called every ~40ms, this is a very convenient place to store this variable.
	private int showLevel;
	private int bulletLevel;
	private int remainNumMissile;
    public enum TurnState {IDLE, LEFT, RIGHT, UP, DOWN}
	private TurnState turnState = TurnState.IDLE;
	private int maxBulletLevel;

	
	public AircraftFriend() {
		setBulletLevel(1);
		setTeam(Team.FRIEND);
		setRadius(MIN_RADIUS);
		setImagePath("/imgs/fal/aircraft_01.png");
		setMaxBulletLevel(1);
	}


	// Move
	// Make sure player's Aircraft stay in the screen
	// It will not go to the left edge of the screen if it reaches the right edge of the screen
	@Override
	public void move() {
		Point center = getCenter();
		if (center.x > Game.DIM.width - getRadius()) {
			setCenter(new Point(Game.DIM.width - getRadius(), center.y));
		} else if (center.x < getRadius()) {
			setCenter(new Point(getRadius(), center.y));
		} else if (center.y > Game.DIM.height - getRadius()) {
			setCenter(new Point(center.x, Game.DIM.height - getRadius()));
		} else if (center.y < getRadius()) {
			setCenter(new Point(center.x, getRadius()));
		}
		if (invisible > 0) invisible--;
		if (shield > 0) shield--;
		if (nukeMeter > 0) nukeMeter--;
		//The falcon is a convenient place to decrement the showLevel variable as the falcon
		//move() method is being called every frame (~40ms); and the falcon reference is never null.
		if (showLevel > 0) showLevel--;
		switch (turnState){
			case LEFT:
				moveLeft();
				break;
			case RIGHT:
				moveRight();
				break;
			case DOWN:
				moveDown();
				break;
			case UP:
				moveUp();
				break;
			case IDLE:
			default:
				//do nothing
		}
		if (getBulletLevel() == 1) {
			fireBulletLevel1();
		} else if (getBulletLevel() == 2){
			fireBulletLevel2();
		} else {
			fireBulletLevel3();
		}
	}


	// Fire one bullet (Class BulletFriend)
	private void fireBulletLevel1() {
		if (CommandCenter.getInstance().getFrame() % 8 == 0) {
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(getCenter(), getOrientation(), getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
		}
	}


	// Fire two bullets (Class BulletFriend)
	private void fireBulletLevel2() {
		int shift = 10;
		Point BulletCenterLeft = new Point(getCenter().x - shift, getCenter().y);
		Point BulletCenterRight = new Point(getCenter().x + shift, getCenter().y);
		if (CommandCenter.getInstance().getFrame() % 8 == 0) {
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(BulletCenterLeft, getOrientation(), getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(BulletCenterRight, getOrientation(), getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
		}
	}


	// Fire four bullets (Class BulletFriend)
	private void fireBulletLevel3() {
		int shift = 10;
		Point BulletCenterLeft = new Point(getCenter().x - shift, getCenter().y);
		Point BulletCenterRight = new Point(getCenter().x + shift, getCenter().y);
		Point BulletCenterUpperLeft = new Point(getCenter().x - shift * 2, getCenter().y);
		Point BulletCenterUpperRight = new Point(getCenter().x + shift * 2, getCenter().y);
		if (CommandCenter.getInstance().getFrame() % 8 == 0) {
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(BulletCenterLeft, getOrientation(), getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(BulletCenterRight, getOrientation(), getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(BulletCenterUpperLeft, getOrientation() - 10, getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
			CommandCenter.getInstance().getOpsQueue().enqueue(
					new BulletFriend(BulletCenterUpperRight, getOrientation() + 10, getDeltaX(), getDeltaY()),
					GameOp.Action.ADD);
		}
	}


	// Move to the left side
	public void moveLeft() {
		setCenter(new Point (getCenter().x - HORIZONTAL_SHIFT, getCenter().y));
	}


	// Move to the right side
	public void moveRight() {
		setCenter(new Point (getCenter().x + HORIZONTAL_SHIFT, getCenter().y));
	}


	// Move forward
	public void moveUp() {
		setCenter(new Point (getCenter().x, getCenter().y - VERTICAL_SHIFT));
	}


	// Move downward
	public void moveDown() {
		setCenter(new Point (getCenter().x, getCenter().y + VERTICAL_SHIFT));
	}


	@Override
	public void remove(LinkedList<Movable> list) {
		if (getHp() <= 0) decrementFalconNumAndSpawn();
	}


	public void decrementFalconNumAndSpawn(){
		CommandCenter.getInstance().setNumFalcons(CommandCenter.getInstance().getNumFalcons() -1);
		if (CommandCenter.getInstance().isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		setShield(AircraftFriend.INITIAL_SPAWN_TIME);
		setInvisible(AircraftFriend.INITIAL_SPAWN_TIME/4);
		setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2)); //put falcon in the middle of the game-space
		setOrientation(-90);
		setDeltaX(0);
		setDeltaY(0);
		setRadius(AircraftFriend.MIN_RADIUS);
		setMaxSpeedAttained(false);
		setNukeMeter(0);
	}


	// Add more Missiles (Class BulletMissiles)
	// Input numMissileToBeAdded is the number of Missiles to be added
	// The total amount of Missiles can not exceed the max amount of missiles, which is six in the current game version
	public void addMissile(int numMissileToBeAdded) {
		if (this.getRemainNumMissile() < MAX_NUM_MISSILE) {
			this.setRemainNumMissile(Math.min(this.getRemainNumMissile() + numMissileToBeAdded, MAX_NUM_MISSILE));
		}
	}


	// Add more HP
	// Input moreHp is the amount of HP to be added
	// The HP can not exceed the total amount of HP
	public void addHp(int moreHp) {
		if (this.getHp() < this.getTotalHp()) {
			this.setHp(Math.min(this.getHp() + moreHp, this.getTotalHp()));
		}
	}


	// Draw HP bar of player's Aircraft
	// This method override because it will draw the HP bar in the top left corner
	@Override
	public void drawHp(Graphics g) {
		int HpBarWidth = 100;
		int HpBarHeight = 10;
		int xVal = 45;
		int yVal = 40;
		g.setColor(Color.BLUE);
		g.drawRect(xVal, yVal, HpBarWidth, HpBarHeight);
		int percent = (int) ((double) this.getHp() / (double) this.getTotalHp() * HpBarWidth);
		g.setColor(Color.CYAN);
		g.fillRect(xVal, yVal, percent, HpBarHeight);
	}



}
