package edu.uchicago.gerber.raster_asteroids.view;

import edu.uchicago.gerber.raster_asteroids.controller.Game;
import edu.uchicago.gerber.raster_asteroids.model.CommandCenter;
import edu.uchicago.gerber.raster_asteroids.model.Movable;
import edu.uchicago.gerber.raster_asteroids.model.PolarPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class GamePanel extends Panel {

    // ==============================================================
    // FIELDS
    // ==============================================================
    private final Font fnt = new Font("SansSerif", Font.BOLD, 12);
    private final Font fntBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
    private FontMetrics fmt;
    private int fontWidth;
    private int fontHeight;

    private final Point[] pntShipsRemaining;


    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================

    public GamePanel(Dimension dim) {
        GameFrame gmf = new GameFrame();
        gmf.getContentPane().add(this);

        // Robert Alef's awesome falcon design
        List<Point> listShip = new ArrayList<>();
        listShip.add(new Point(0,9));
        listShip.add(new Point(-1, 6));
        listShip.add(new Point(-1,3));
        listShip.add(new Point(-4, 1));
        listShip.add(new Point(4,1));
        listShip.add(new Point(-4,1));
        listShip.add(new Point(-4, -2));
        listShip.add(new Point(-1, -2));
        listShip.add(new Point(-1, -9));
        listShip.add(new Point(-1, -2));
        listShip.add(new Point(-4, -2));
        listShip.add(new Point(-10, -8));
        listShip.add(new Point(-5, -9));
        listShip.add(new Point(-7, -11));
        listShip.add(new Point(-4, -11));
        listShip.add(new Point(-2, -9));
        listShip.add(new Point(-2, -10));
        listShip.add(new Point(-1, -10));
        listShip.add(new Point(-1, -9));
        listShip.add(new Point(1, -9));
        listShip.add(new Point(1, -10));
        listShip.add(new Point(2, -10));
        listShip.add(new Point(2, -9));
        listShip.add(new Point(4, -11));
        listShip.add(new Point(7, -11));
        listShip.add(new Point(5, -9));
        listShip.add(new Point(10, -8));
        listShip.add(new Point(4, -2));
        listShip.add(new Point(1, -2));
        listShip.add(new Point(1, -9));
        listShip.add(new Point(1, -2));
        listShip.add(new Point(4,-2));
        listShip.add(new Point(4, 1));
        listShip.add(new Point(1, 3));
        listShip.add(new Point(1,6));
        listShip.add(new Point(0,9));

        //this just displays the ships remaining
        pntShipsRemaining = CommandCenter.pointsListToArray(listShip);

        gmf.pack();
        initView();
        gmf.setSize(dim);
        gmf.setTitle("Game Base");
        gmf.setResizable(false);
        gmf.setVisible(true);
        setFocusable(true);
    }


    // ==============================================================
    // METHODS
    // ==============================================================

    private void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(fnt);
        if (CommandCenter.getInstance().getScore() > 0) {
            g.drawString("SCORE :  " + CommandCenter.getInstance().getScore(), fontWidth, fontHeight);
        } else {
            g.drawString("NO SCORE", fontWidth, fontHeight);
        }
    }
    //this is used for development, you can remove it from your final game
    private void drawNumFrame(Graphics g) {
        g.setColor(Color.white);
        g.setFont(fnt);
        g.drawString("FRAME :  " + CommandCenter.getInstance().getFrame(), fontWidth,
                Game.DIM.height  - (fontHeight + 22));

    }


    public void update(Graphics g) {
        //create an image off-screen
        // The following "off" vars are used for the off-screen double-buffered image.
        Image imgOff = createImage(Game.DIM.width, Game.DIM.height);
        //get its graphics context
        Graphics grpOff = imgOff.getGraphics();

        //Fill the off-screen image background with black.
        grpOff.setColor(Color.black);
        grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);

        drawScore(grpOff);
        drawNumFrame(grpOff);

        if (CommandCenter.getInstance().isGameOver()) {
            displayTextOnScreen(grpOff,
                    "GAME OVER",
                    "use the arrow keys to turn and thrust",
                    "use the space bar to fire",
                    "'S' to Start",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music"

            );
        } else if (CommandCenter.getInstance().isPaused()) {

            displayTextOnScreen(grpOff, "Game Paused");

        }

        //playing and not paused!
        else {

            processMovables(grpOff,
                    CommandCenter.getInstance().getMovDebris(),
                    CommandCenter.getInstance().getMovFloaters(),
                    CommandCenter.getInstance().getMovFoes(),
                    CommandCenter.getInstance().getMovFriends());


            drawNumberShipsRemaining(grpOff);


        }

        //after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
        // of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
        // without the use of a double-buffered off-screen image, you will see flickering.
        g.drawImage(imgOff, 0, 0, this);
    }


    //this method causes all sprites to move and draw themselves
    @SafeVarargs
    private final void processMovables(final Graphics g, List<Movable>... arrayOfListMovables) {

        BiConsumer<Graphics, Movable> moveDraw = (grp, mov) -> {
            mov.move();
            mov.draw(grp);
        };

        //we use flatMap to flatten the List<Movable>[] passed-in above into a single stream of Movables
        Arrays.stream(arrayOfListMovables) //Stream<List<Movable>>
                .flatMap(Collection::stream) //Stream<Movable>
                .forEach(m -> moveDraw.accept(g, m));


    }


    private void drawNumberShipsRemaining(Graphics g) {
        int numFalcons = CommandCenter.getInstance().getNumFalcons();
        while (numFalcons > 0) {
            drawOneShipRemaining(g, numFalcons--);
        }
    }

    // Draw the number of falcons remaining on the bottom-right of the screen.
    private void drawOneShipRemaining(Graphics g, int offSet) {

        g.setColor(Color.ORANGE);

        final double DEGREES = 90.0;
        final int SIZE = 15, X_POS = 27, Y_POS = 45;
        //rotate raw polars given the orientation, Then convert back to cartesians.
        Function<PolarPoint, Point> rotateFalcon90 =
                pp -> new Point(
                        (int)  (pp.getR() * SIZE
                                * Math.sin(Math.toRadians(DEGREES)
                                + pp.getTheta())),

                        (int)  (pp.getR() * SIZE
                                * Math.cos(Math.toRadians(DEGREES)
                                + pp.getTheta())));



        g.drawPolygon(

                CommandCenter.cartesianToPolar(Arrays.asList(pntShipsRemaining)).stream()
                        .map(rotateFalcon90)
                        .map(pnt -> pnt.x + Game.DIM.width - (X_POS * offSet))
                        .mapToInt(Integer::intValue)
                        .toArray(),

                CommandCenter.cartesianToPolar(Arrays.asList(pntShipsRemaining)).stream()
                        .map(rotateFalcon90)
                        .map(pnt -> pnt.y + Game.DIM.height - Y_POS)
                        .mapToInt(Integer::intValue)
                        .toArray(),

                pntShipsRemaining.length);


    }

    private void initView() {
        Graphics g = getGraphics();            // get the graphics context for the panel
        g.setFont(fnt);                        // take care of some simple font stuff
        fmt = g.getFontMetrics();
        fontWidth = fmt.getMaxAdvance();
        fontHeight = fmt.getHeight();
        g.setFont(fntBig);                    // set font info
    }


    // This method draws some text to the middle of the screen before/after a game
    private void displayTextOnScreen(final Graphics graphics, String... lines) {

        final AtomicInteger spacer = new AtomicInteger(0);
        Arrays.stream(lines)
                .forEach(s ->
                            graphics.drawString(s, (Game.DIM.width - fmt.stringWidth(s)) / 2,
                                    Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40))

                );


    }


}
