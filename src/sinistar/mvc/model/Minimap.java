package sinistar.mvc.model;

import sinistar.mvc.controller.Game;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by michaelvasiliou on 11/30/16.
 */
public class Minimap {
    private static final int MAP_MARGIN = 20;
    private static final int WIDTH_FACTOR = 5;
    private static final int HEIGHT_FACTOR = 4;
    private ArrayList<Point> asteroids;
    private ArrayList<Point> workers;
    private ArrayList<Point> turrets;
    private ArrayList<Point> crystals;
    private Point sinistar;
    private Point falcon;

    public void move() {
        asteroids = new ArrayList<>();
        workers = new ArrayList<>();
        turrets = new ArrayList<>();
        crystals = new ArrayList<>();
        sinistar = null;
        falcon = null;

        for (Movable mov : CommandCenter.getInstance().getMov()) {
            if (!(mov instanceof Star) && !(mov instanceof Bullet) && !(mov instanceof Explosion)){
                Point movCenter = mov.getCenter();
                if (((0 <= movCenter.x && movCenter.x <= (2 * Game.DIM.width))
                            || ((Game.DIMENSION_SCALAR * Game.DIM.width - Game.DIM.width) <= movCenter.x
                                && movCenter.x <= Game.DIMENSION_SCALAR * Game.DIM.width))
                        && ((0 <= movCenter.y && movCenter.y <= (2 * Game.DIM.height))
                            || (Game.DIMENSION_SCALAR * Game.DIM.height - Game.DIM.height) <= movCenter.y
                                && movCenter.y <= Game.DIMENSION_SCALAR * Game.DIM.height))
                {
                    int x = movCenter.x + Game.DIM.width;
                    if (x >= Game.DIM.width * Game.DIMENSION_SCALAR){
                        x -= (Game.DIM.width * Game.DIMENSION_SCALAR);
                    }

                    int y = movCenter.y + Game.DIM.height;
                    if (y >= Game.DIM.height * Game.DIMENSION_SCALAR){
                        y -= (Game.DIM.height * Game.DIMENSION_SCALAR);
                    }

                    x = x / (3 * WIDTH_FACTOR);
                    y = y / (3 * HEIGHT_FACTOR);

                    x = x + (Game.DIM.width - (Game.DIM.width / WIDTH_FACTOR) - MAP_MARGIN);
                    y = y + MAP_MARGIN;

                    Point point = new Point(x, y);

                    if (mov instanceof Asteroid){
                        asteroids.add(point);
                    }
                    else if (mov instanceof WorkerShip){
                        workers.add(point);
                    }
                    else if (mov instanceof TurretShip){
                        turrets.add(point);
                    }
                    else if (mov instanceof Crystal){
                        crystals.add(point);
                    }
                    else if (mov instanceof Sinistar){
                        sinistar = point;
                    }
                    else if (mov instanceof Falcon){
                        falcon = point;
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(Game.DIM.width - (Game.DIM.width / WIDTH_FACTOR) - MAP_MARGIN,
                MAP_MARGIN,
                Game.DIM.width / WIDTH_FACTOR + 4,
                Game.DIM.height / HEIGHT_FACTOR + 4);
        g.setColor(Color.BLUE);

        g.drawRect(Game.DIM.width - (Game.DIM.width / WIDTH_FACTOR) - MAP_MARGIN,
                   MAP_MARGIN,
                   Game.DIM.width / WIDTH_FACTOR + 4,
                   Game.DIM.height / HEIGHT_FACTOR + 4);

        g.drawRect(Game.DIM.width - (2 * Game.DIM.width / (3 *WIDTH_FACTOR)) - MAP_MARGIN,
                   MAP_MARGIN + (Game.DIM.height / (3* HEIGHT_FACTOR)),
                   Game.DIM.width / (3 * WIDTH_FACTOR),
                   Game.DIM.height / (3 * HEIGHT_FACTOR));

        g.setColor(Color.GRAY);
        for (Point point : asteroids) {
            g.drawLine(point.x, point.y, point.x, point.y);
        }
        g.setColor(Color.RED);
        for (Point point : workers) {
            g.drawOval(point.x, point.y, 3, 3);
            g.fillOval(point.x, point.y, 3, 3);
        }
        g.setColor(Color.BLUE);
        for (Point point : turrets) {
            g.drawOval(point.x, point.y, 4, 4);
            g.fillOval(point.x, point.y, 4, 4);
        }
        g.setColor(Color.WHITE);
        for (Point point : crystals) {
            g.drawOval(point.x, point.y, 5, 5);
            g.fillOval(point.x, point.y, 5, 5);
        }
        if (sinistar != null){
            g.setColor(Color.YELLOW);
            g.drawOval(sinistar.x, sinistar.y, 5, 5);
            g.fillOval(sinistar.x, sinistar.y, 5, 5);
        }
        if (falcon != null){
            g.setColor(Color.MAGENTA);
            g.drawOval(falcon.x, falcon.y, 5, 5);
            g.fillOval(falcon.x, falcon.y, 5, 5);
        }
    }
}