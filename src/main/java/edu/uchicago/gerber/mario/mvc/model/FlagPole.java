package mario.mvc.model;

import java.awt.*;

/**
 * Class to draw a line to represent a flag pole but extends Sprite so that it is movable.
 */
public class FlagPole extends Sprite {

    public FlagPole(int nCenterX, int nCenterY) {
        super(nCenterX,nCenterY);
        setTeam(Team.PLATFORM);
        setHeight(395);
        setWidth(10);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(new Color(108, 215, 10));
        g2d.setStroke(new BasicStroke(getWidth()));
        g2d.drawLine(getCenter().x,getCenter().y,getCenter().x,getCenter().y + getHeight());
        g2d.setColor(Color.WHITE);
    }
}
