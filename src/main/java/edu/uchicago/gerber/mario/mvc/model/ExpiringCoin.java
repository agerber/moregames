package mario.mvc.model;

import java.awt.*;

/**
 * Expiring coin inherits properties of a regular coin but has a expiry time on screen
 */
public class ExpiringCoin extends Coin {
    private static final int EXPIRY_LIMIT = 10;
    private static final int VERTICAL_SPEED = 2;
    private int nExpiryCounter;

    public ExpiringCoin(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        nExpiryCounter = EXPIRY_LIMIT;
    }

    public void decrExpiryCounter() {
        nExpiryCounter--;
    }

    public int getExpiryCounter() {
        return nExpiryCounter;
    }

    @Override
    public void move(){
        setCenter(new Point(getCenter().x,getCenter().y - VERTICAL_SPEED));
        super.move();
    }
}
