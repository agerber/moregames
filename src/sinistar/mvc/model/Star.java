package sinistar.mvc.model;

import sinistar.mvc.controller.Game;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by michaelvasiliou on 11/29/16.
 */
public class Star extends Sprite{
    private static final int STAR_RADIUS = 1;
    public static final int NUM_STARS = 400;

    public Star() {
        super();
        setTeam(Team.DEBRIS);
        ArrayList<Point> pntCs = new ArrayList<>();
        pntCs.add(new Point(1, 1));
        pntCs.add(new Point(1, -1));
        pntCs.add(new Point(-1, -1));
        pntCs.add(new Point(-1, 1));
        setRadius(STAR_RADIUS);
        assignPolarPoints(pntCs);
        double starColorProb = Game.R.nextDouble();
        if (starColorProb < .25){
            setColor(Color.RED);
        }
        else if (.25 <= starColorProb && starColorProb < .50){
            setColor(Color.YELLOW);
        }
        else if (.50 <= starColorProb && starColorProb < .75){
            setColor(Color.CYAN);
        }
        else if (.75 <= starColorProb && starColorProb < 1){
            setColor(Color.ORANGE);
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        //fill this polygon (with whatever color it has)
        g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
        //now draw a white border
        g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
    }

}
