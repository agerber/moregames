package mario.mvc.model;

import mario.sounds.Sound;

import javax.swing.*;
import java.awt.*;

/**
 * Class represents Mario from Super Mario
 */
public class Mario extends Sprite {

    // Mario Right facing images
    private Image imgMarioStillRight = new ImageIcon(Sprite.strImageDir + "Mario_Still_Right.gif").getImage();
    private Image imgMarioJumpRight = new ImageIcon(Sprite.strImageDir + "Mario_Jump_Right.gif").getImage();
    private Image imgMarioWalkRight1 = new ImageIcon(Sprite.strImageDir + "Mario_Walk_Right1.gif").getImage();
    private Image imgMarioWalkRight2 = new ImageIcon(Sprite.strImageDir + "Mario_Walk_Right2.gif").getImage();
    private Image imgMarioWalkRight3 = new ImageIcon(Sprite.strImageDir + "Mario_Walk_Right3.gif").getImage();

    // Mario Left facing images
    private Image imgMarioStillLeft = new ImageIcon(Sprite.strImageDir + "Mario_Still_Left.gif").getImage();
    private Image imgMarioJumpLeft = new ImageIcon(Sprite.strImageDir + "Mario_Jump_Left.gif").getImage();
    private Image imgMarioWalkLeft1 = new ImageIcon(Sprite.strImageDir + "Mario_Walk_Left1.gif").getImage();
    private Image imgMarioWalkLeft2 = new ImageIcon(Sprite.strImageDir + "Mario_Walk_Left2.gif").getImage();
    private Image imgMarioWalkLeft3 = new ImageIcon(Sprite.strImageDir + "Mario_Walk_Left3.gif").getImage();

    private Image imgMarioDead = new ImageIcon(Sprite.strImageDir + "Mario_Dead.gif").getImage();
    private Image imgMarioClimb = new ImageIcon(Sprite.strImageDir + "Mario_Climb.gif").getImage();

    private Image imgMario;
    private boolean bInit = true;
    private boolean bMarioAscend, bMarioDescend , bLookingRight;
    private int nMarioDeadTimeLeft = 0;

    private int nJumpIncrCount = 0;
    private int nMoveRightCount = 0;
    private int nMoveLeftCount = 0;

    private int nDeltaY = -4; // Delta during Mario Spawn to ensure Mario comes out of pipe slowly
    public static final int VERTICAL_LIMIT = 150;
    public static final int DEFAULT_ASCEND_SPEED = +15; // Pixels per frame
    public static final int DEFAULT_DESCEND_SPEED = +30; // Pixels per frame
    public static final int DEFAULT_VERTICAL_STEPS = +13;
    public static final int DEFAULT_HORIZONTAL_SPEED = 9; // Pixels per step
    public static final int DEFAULT_HORIZONTAL_STEPS = +9;
    public static final int SCREEN_LEFT_LIMIT = 150;
    public static final int SCREEN_RIGHT_LIMIT = 700;
    private int nDeltaJumpDownY = DEFAULT_DESCEND_SPEED;


    public Mario(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.FRIEND);
        setCenter(new Point(nCenterX, nCenterY));
        setHeight(32);
        setWidth(26);
        setDeltaY(nDeltaY);
        imgMario = imgMarioStillRight;
        bMarioAscend = false;
        bMarioDescend = false;
        bLookingRight = true;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(imgMario,getCenter().x,getCenter().y,null);
    }

    public void jump() {
        if (nJumpIncrCount == 0 && !bMarioDescend && !this.isDead() && !bMarioAscend) { // To avoid air jump
            bMarioAscend = true;
            nJumpIncrCount = DEFAULT_VERTICAL_STEPS;
            Sound.playSound("Mario_Jump.wav");
        }
    }

    public boolean getMarioAscend() {
        if (nJumpIncrCount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void stopMarioAscend() {
        bMarioAscend = false;
        nJumpIncrCount =0;
    }

    public void setMarioDescend() {
        bMarioDescend = true;
    }

    public void stopMarioDescend() {
        bMarioDescend = false;
        setDeltaJumpDownY(Mario.DEFAULT_DESCEND_SPEED);
    }

    public void stopMarioHorizontalMove() {
        nMoveRightCount = 0;
        nMoveLeftCount = 0;
    }

    public int getMoveRightCount() {
        return nMoveRightCount;
    }

    public int getMoveLeftCount() {
        return nMoveLeftCount;
    }

    public void setDeltaJumpDownY(int nSpeed) {
        nDeltaJumpDownY = nSpeed;
    }

    public int getDeltaJumpDownY() {
        return nDeltaJumpDownY;
    }

    public void moveLeft() {
        nMoveLeftCount = DEFAULT_HORIZONTAL_STEPS;
        bLookingRight = false;
    }

    public void moveRight() {
        nMoveRightCount = DEFAULT_HORIZONTAL_STEPS;
        bLookingRight = true;
    }

    public void setDead() {
        super.setDead();
        nMarioDeadTimeLeft = 67;
    }


    public int getMarioDeadTimeLeft() {
        return nMarioDeadTimeLeft;
    }

    @Override
    public void move() {

        // Call method to set Mario's image
        setMarioImage();

        if (nJumpIncrCount == 0) {
            bMarioAscend = false;
        }

        // Code to make Mario come out of pipe
        if (bInit) {
            getCenter().y += getDeltaY();
            if (getCenter().y <= 616) {
                bInit = false;
            }
            // Logic to keep Mario stuck to flag when level is being cleared
        } else if (CommandCenter.getInstance().isLevelClear()) {
            getCenter().x = CommandCenter.getInstance().getPntFlagCenterTracker().x - 10;
            getCenter().y = CommandCenter.getInstance().getPntFlagCenterTracker().y;

            // Logic to change Mario's position vertically when he jumps
        } else if (nJumpIncrCount > 0) {
            getCenter().y+= -DEFAULT_ASCEND_SPEED;
            nJumpIncrCount--;
        } else if (bMarioDescend) {
            if (nDeltaJumpDownY < 0) {
                setDeltaJumpDownY(Mario.DEFAULT_DESCEND_SPEED);
            }
            getCenter().y+= nDeltaJumpDownY;

            // To keep Mario within frame
            if (getCenter().y + this.getHeight() > Ground.nCenterY || getCenter().y < 100) {
                getCenter().y = Ground.nCenterY - this.getHeight();
            }
        }

        if (nMoveRightCount > 0) {
            setCenter(new Point(getCenter().x + DEFAULT_HORIZONTAL_SPEED, getCenter().y));
            nMoveRightCount--;
        } else if (nMoveLeftCount > 0){
            setCenter(new Point(getCenter().x - DEFAULT_HORIZONTAL_SPEED, getCenter().y));
            nMoveLeftCount--;
        }
    }

    // Logic to set Mario's image based on which direction he is walking and if still, based on side
    private void setMarioImage() {
        if (this.isDead()) {
            imgMario = imgMarioDead;
            nMarioDeadTimeLeft--;
        } else if(CommandCenter.getInstance().isLevelClear()) {
            imgMario = imgMarioClimb;
        } else if (nJumpIncrCount > 0 || bMarioDescend) {
            if (bLookingRight) {
                imgMario = imgMarioJumpRight;
            } else  {
                imgMario = imgMarioJumpLeft;
            }
        } else if (nMoveRightCount > 0 && nMoveRightCount /3 == 0 || (CommandCenter.getInstance().getMoveCountX()/3 == 1 && CommandCenter.getInstance().getDeltaX() < 0)) {
            imgMario = imgMarioWalkRight1;
        } else if (nMoveRightCount /3 == 1 || (CommandCenter.getInstance().getMoveCountX()/3 == 2 && CommandCenter.getInstance().getDeltaX() < 0)) {
            imgMario = imgMarioWalkRight2;
        } else if (nMoveRightCount /3 == 2 || (CommandCenter.getInstance().getMoveCountX()/3 == 3 && CommandCenter.getInstance().getDeltaX() < 0)) {
            imgMario = imgMarioWalkRight3;
        } else if (nMoveLeftCount > 0 && nMoveLeftCount /3 == 0 || (CommandCenter.getInstance().getMoveCountX()/3 == 1 && CommandCenter.getInstance().getDeltaX() > 0)) {
            imgMario = imgMarioWalkLeft1;
        } else if (nMoveLeftCount /3 == 1 || (CommandCenter.getInstance().getMoveCountX()/3 == 2 && CommandCenter.getInstance().getDeltaX() > 0)) {
            imgMario = imgMarioWalkLeft2;
        } else if (nMoveLeftCount /3 == 2 || (CommandCenter.getInstance().getMoveCountX()/3 == 3 && CommandCenter.getInstance().getDeltaX() > 0)) {
            imgMario = imgMarioWalkLeft3;
        } else {
            if (bLookingRight) {
                imgMario = imgMarioStillRight;
            } else {
                imgMario = imgMarioStillLeft;
            }
        }
    }

    public boolean getMarioDescending() {
        return bMarioDescend;
    }

    public int getDeltaMoveRightX() {
        return DEFAULT_HORIZONTAL_STEPS;
    }

    public int getDeltaMoveLeftX() {
        return -DEFAULT_HORIZONTAL_STEPS;
    }

    public boolean checkMarioMoving() {
        if (nMoveRightCount > 0 || nMoveLeftCount > 0) {
            return true;
        } else {
            return false;
        }
    }
}
