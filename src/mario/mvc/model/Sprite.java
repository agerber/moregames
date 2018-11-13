package mario.mvc.model;

import java.awt.*;
import java.io.File;

public abstract class Sprite implements Movable {

	//the center-point of this sprite
	private Point pntCenter;
    private int nInitCenterX, nInitCenterY;

	//this causes movement; change in x and change in y
	private int nDeltaX, nDeltaY;

	//we need to know what team we're on
	private Team mTeam;


    // Default worth is zero.
    private final int WORTH = 0;

    // Height and width of the sprite
	private int nHeight;
	private int nWidth;


    // Expiry team for this sprite
    private int nDeadTimeLeft = 0;
    private  boolean bDead;

    // project directory with images
    public static String strImageDir  = System.getProperty("user.dir") + File.separator + "src"
                                        + File.separator + "mario" + File.separator + "images" + File.separator;

    // project directory with fonts
    public static String strFontDir  = System.getProperty("user.dir") + File.separator + "src"
            + File.separator + "mario" + File.separator + "fonts" + File.separator;



    public Sprite(int nCenterX, int nCenterY) {
        nInitCenterX = nCenterX;
        nInitCenterY = nCenterY;
        setCenter(new Point(nCenterX,nCenterY));
        bDead = false;
    }


	@Override
	public Team getTeam() {
	  return mTeam;
	}

	public void setTeam(Team team){
		mTeam = team;
	}

    // This move method is primarily used by non-moving objects like platform when the screen moves
    // Other moving objects override to implement their own logic but also call this method
    public void move(){
        if (CommandCenter.getInstance().getMoveCountX() != 0) {
            pntCenter.x+= CommandCenter.getInstance().getDeltaX();
        }
    }

    // Set initial position for this sprite to be used when Mario is respawned in the current level
    public void initPos() {
        pntCenter.x = nInitCenterX;
        pntCenter.y = nInitCenterY;
    }


	public void setDeltaX(int nSet) {
		nDeltaX = nSet;
	}

	public void setDeltaY(int nSet) {
		nDeltaY = nSet;
	}

	public int getDeltaY() {
		return nDeltaY;
	}

	public int getDeltaX() {
		return nDeltaX;
	}


	public Point getCenter() {
		return pntCenter;
	}

	public void setCenter(Point pntParam) {
		pntCenter = pntParam;
	}

	@Override
    public void draw(Graphics g) {
    }

    public void setHeight(int nHeight) {
        this.nHeight = nHeight;
    }

    public void setWidth(int nWidth) {
        this.nWidth = nWidth;
    }

    public int getHeight() {
        return nHeight;
    }

    public int getWidth() {
        return nWidth;
    }

	public void setLeftDirection() {

	};
	public void setRightDirection() {

	};

    public void setDead() {
        this.bDead = true;
    };

    public boolean isDead() {
        return bDead;
    }


    public int getDeadTimeLeft() {
        return nDeadTimeLeft;
    }

    public int getWorth() {
        return WORTH;
    }

}
