package edu.uchicago.gerber.fox.mvc.view;

import edu.uchicago.gerber.fox.mvc.controller.CommandCenter;
import edu.uchicago.gerber.fox.mvc.controller.Game;
import edu.uchicago.gerber.fox.mvc.model.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


public class GamePanel extends Panel {

    // ==============================================================
    // FIELDS
    // ==============================================================
    private final Font fontNormal = new Font("SansSerif", Font.BOLD, 12);
    private final Font fontBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 48);
    private FontMetrics fontMetrics;
    private int fontWidth;
    private int fontHeight;


    //used for double-buffering
    private Image imgOff;
    private Graphics grpOff;

    private Color backgroundColor = new Color(79, 60, 144);


    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================

    public GamePanel(Dimension dim) {

        GameFrame gameFrame = new GameFrame();

        gameFrame.getContentPane().add(this);


        gameFrame.pack();
        initFontInfo();
        gameFrame.setSize(dim);
        //change the name of the game-frame to your game name
        gameFrame.setTitle("RUN FOX RUN");
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
        setFocusable(true);
    }


    // ==============================================================
    // METHODS
    // ==============================================================

    private void drawFoxStatus(final Graphics graphics){

        graphics.setColor(Color.white);
        graphics.setFont(fontNormal);

        //draw score always
        graphics.drawString("Score :  " + CommandCenter.getInstance().getScore(), 20, 20);
        graphics.drawString("Life Remaining:  " + CommandCenter.getInstance().getNumFoxes(), 20, 35);
        graphics.drawString("MultiJump Remaining:  " + CommandCenter.getInstance().getNumJumps(), 20, 50);

    }

    //this is used for development, you can remove it from your final game
    private void drawNumFrame(Graphics g) {
        g.setColor(Color.white);
        g.setFont(fontNormal);
        g.drawString("FRAME :  " + CommandCenter.getInstance().getFrame(), fontWidth,
                Game.DIM.height  - (fontHeight + 22));

    }


    public void update(Graphics g) {

        // The following "off" vars are used for the off-screen double-buffered image.
        imgOff = createImage(Game.DIM.width, Game.DIM.height);
        //get its graphics context
        grpOff = imgOff.getGraphics();

        //Fill the off-screen image background with black.
        grpOff.setColor(backgroundColor);
        grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);

        //this is used for development, you may remove drawNumFrame() in your final game.
        drawNumFrame(grpOff);

        if (CommandCenter.getInstance().isGameOver()) {
            displayTextOnScreen(grpOff,
                    "GAME OVER",
                    "use the space bar to jump",
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
            moveDrawMovables(grpOff,
                    CommandCenter.getInstance().getMovFriends(),//always draw the background first
                    CommandCenter.getInstance().getMovFoes(),
                    CommandCenter.getInstance().getMovCoins());
            drawFoxStatus(grpOff);


        }

        //after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
        // of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
        // without the use of a double-buffered off-screen image, you will see flickering.
        g.drawImage(imgOff, 0, 0, this);
    }


    //this method causes all sprites to move and draw themselves
    @SafeVarargs
    private final void moveDrawMovables(final Graphics g, List<Movable>... teams) {

        BiConsumer<Movable, Graphics> moveDraw = (mov, grp) -> {
            mov.move();
            mov.draw(grp);
        };


        Arrays.stream(teams) //Stream<List<Movable>>
                //we use flatMap to flatten the teams (List<Movable>[]) passed-in above into a single stream of Movables
                .flatMap(Collection::stream) //Stream<Movable>
                .forEach(m -> moveDraw.accept(m, g));


    }






    private void initFontInfo() {
        Graphics g = getGraphics();            // get the graphics context for the panel
        g.setFont(fontNormal);                        // take care of some simple font stuff
        fontMetrics = g.getFontMetrics();
        fontWidth = fontMetrics.getMaxAdvance();
        fontHeight = fontMetrics.getHeight();
        g.setFont(fontBig);                    // set font info
    }


    // This method draws some text to the middle of the screen
    private void displayTextOnScreen(final Graphics graphics, String... lines) {

        //AtomicInteger is safe to pass into a stream
        final AtomicInteger spacer = new AtomicInteger(0);
        Arrays.stream(lines)
                .forEach(str ->
                            graphics.drawString(str, (Game.DIM.width - fontMetrics.stringWidth(str)) / 2,
                                    Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40))

                );


    }


}
