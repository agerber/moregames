package edu.uchicago.gerber.g1941.mvc.view;

import edu.uchicago.gerber.g1941.mvc.controller.CommandCenter;
import edu.uchicago.gerber.g1941.mvc.controller.Game;
import edu.uchicago.gerber.g1941.mvc.model.Movable;
import edu.uchicago.gerber.g1941.mvc.model.Sprite;
import edu.uchicago.gerber.g1941.mvc.model.airplane.Aircraft;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    private final Font fontBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
    private FontMetrics fontMetrics;
    private int fontWidth;
    private int fontHeight;
    private Image imgOff;
    private Graphics grpOff;


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
        gameFrame.setTitle("Game Base");
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
        setFocusable(true);
    }


    // ==============================================================
    // METHODS
    // ==============================================================

    private void drawFalconStatus(final Graphics graphics){
        graphics.setColor(Color.white);
        graphics.setFont(fontNormal);
        graphics.drawString("Score :  " + CommandCenter.getInstance().getScore(), fontWidth, fontHeight);
        String levelText = "Level: " + CommandCenter.getInstance().getLevel();
        graphics.drawString(levelText, 20, 30);
        graphics.drawString("HP : ", 20, 50);
        graphics.drawString("Missile : ", 20, 70);
        drawNumMissile(graphics);
        //build the status string array with possible messages in middle of screen
        List<String> statusArray = new ArrayList<>();
        if (CommandCenter.getInstance().getAircraftFriend().getShowLevel() > 0) statusArray.add(levelText);
        if (CommandCenter.getInstance().getAircraftFriend().isMaxSpeedAttained()) statusArray.add("WARNING - SLOW DOWN");
        if (CommandCenter.getInstance().getAircraftFriend().getNukeMeter() > 0) statusArray.add("PRESS N for NUKE");
        //draw the statusArray strings to middle of screen
        if (statusArray.size() > 0)
            displayTextOnScreen(graphics, statusArray.toArray(new String[0]));
    }


    public void drawNumMissile(Graphics g) {
        int numMissile = CommandCenter.getInstance().getAircraftFriend().getRemainNumMissile();
        while (numMissile > 0) {
            drawOneMissile(g, numMissile--);
        }
    }


    // W.
    // This method is nearly same as the renderRaster from Sprite
    // But I need change some minor parts of this method
    public void drawOneMissile(Graphics g, int offSet) {
        Graphics2D g2d = (Graphics2D) g;
        BufferedImage bufferedImage = Sprite.loadGraphic("/imgs/fal/missile.png");
        if (bufferedImage ==  null) return;
        int centerX = 5 + offSet * 20;
        int centerY = 90;
        int width = 40;
        int height = 30;
        double angleRadians = Math.toRadians(180);
        AffineTransform oldTransform = g2d.getTransform();
        try {
            double scaleX = width * 1.0 / bufferedImage.getWidth();
            double scaleY = height * 1.0 / bufferedImage.getHeight();
            AffineTransform affineTransform = new AffineTransform( oldTransform );
            affineTransform.translate(centerX, centerY);
            affineTransform.scale( scaleX, scaleY );
            if ( angleRadians != 0 ) {
                affineTransform.rotate( angleRadians );
            }
            affineTransform.translate( -bufferedImage.getWidth() / 2.0, -bufferedImage.getHeight() / 2.0 );
            g2d.setTransform( affineTransform );
            g2d.drawImage( bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null );
        } finally {
            g2d.setTransform( oldTransform );
        }
    }


    @Override
    public void update(Graphics g) {
        // The following "off" vars are used for the off-screen double-buffered image.
        imgOff = createImage(Game.DIM.width, Game.DIM.height);
        //get its graphics context
        grpOff = imgOff.getGraphics();

        //Fill the off-screen image background with black.
        grpOff.setColor(Color.BLACK);
        grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
        // W.
        grpOff.setColor(Color.white);
        grpOff.setFont(fontNormal);
        // W.
        if (CommandCenter.getInstance().isGameOver() &&
                !CommandCenter.getInstance().isShowMenu() &&
                !CommandCenter.getInstance().isPassed() &&
                !CommandCenter.getInstance().isFailed()) {
            displayTextOnScreen(grpOff,
                    "GAME OVER",
                    "use the arrow keys to turn and thrust",
                    "use the space bar to fire",
                    "'S' to Start",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music"
            );
        }
        else if (CommandCenter.getInstance().isGameOver() &&
                CommandCenter.getInstance().isShowMenu()){
            String map2Status = CommandCenter.getInstance().getMapLocked().get(2) ?  "The current map is locked" : "Press '2' for Map 2";
            String map3Status = CommandCenter.getInstance().getMapLocked().get(3) ?  "The current map is locked" : "Press '3' for Map 3";
            displayTextOnScreen(grpOff,
                    "Please select a map",
                    "Press '1' for Map 1",
                    map2Status,
                    map3Status
            );
        } else if (CommandCenter.getInstance().isGameOver() &&
                !CommandCenter.getInstance().isShowMenu() &&
                CommandCenter.getInstance().isPassed()) {
            displayTextOnScreen(grpOff,
                    "CONGRATULATIONS !!!",
                    "Press b Back To The Map Menu"
            );
        } else if (CommandCenter.getInstance().isGameOver() &&
                !CommandCenter.getInstance().isShowMenu() &&
                CommandCenter.getInstance().isFailed()) {
            displayTextOnScreen(grpOff,
                    "FAILED",
                    "Press b Back To The Map Menu"
            );
        }
        else if (CommandCenter.getInstance().isPaused()) {
            displayTextOnScreen(grpOff, "Game Paused");
        }
        //playing and not paused!
        else {
            moveDrawMovables(grpOff,
                    CommandCenter.getInstance().getMovDebris(),
                    CommandCenter.getInstance().getMovFloaters(),
                    CommandCenter.getInstance().getMovFoes(),
                    CommandCenter.getInstance().getMovFriends());
//            drawNumberShipsRemaining(grpOff);
//            drawMeters(grpOff);
            drawFalconStatus(grpOff);
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
            if (mov instanceof Aircraft) {
                ((Aircraft) mov).drawHp(grp);
            }
        };
        Arrays.stream(teams) //Stream<List<Movable>>
                //we use flatMap to flatten the teams (List<Movable>[]) passed-in above into a single stream of Movables
                .flatMap(Collection::stream) //Stream<Movable>
                .forEach(m -> moveDraw.accept(m, g));
    }


    private void initFontInfo() {
        Graphics g = getGraphics(); // get the graphics context for the panel
        g.setFont(fontNormal); // take care of some simple font stuff
        fontMetrics = g.getFontMetrics();
        fontWidth = fontMetrics.getMaxAdvance();
        fontHeight = fontMetrics.getHeight();
        g.setFont(fontBig); // set font info
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
