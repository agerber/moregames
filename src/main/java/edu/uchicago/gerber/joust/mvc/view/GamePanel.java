package joust.mvc.view;

import joust.mvc.controller.Game;
import joust.mvc.model.CommandCenter;
import joust.mvc.model.Movable;
import joust.mvc.model.Sprite;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GamePanel extends Panel {

    // ==============================================================
    // FIELDS
    // ==============================================================
    private Dimension dimOff;
    private Image imgOff;
    private Graphics grpOff;

    private GameFrame gmf;

    private Font blockyFont;
    public static String fontDir = System.getProperty("user.dir") + File.separator + "src"
            + File.separator + "_08final" + File.separator + "fonts" + File.separator;

    private Image titleScreen = new ImageIcon(Sprite.getImgDir() + "balloonFightTitle.gif").getImage();
    private Image bonusBackground = new ImageIcon(Sprite.getImgDir() + "bonusBackground.png").getImage();
    private Image battleBackground = new ImageIcon(Sprite.getImgDir() + "battleBackground.png").getImage();
    private Image battleForeground = new ImageIcon(Sprite.getImgDir() + "battleForeground.png").getImage();

    private Image[] bKidStatL2 = { new ImageIcon(Sprite.getImgDir() + "bKidStatL2_0.png").getImage(),
                                    new ImageIcon(Sprite.getImgDir() + "bKidStatL2_1.png").getImage(),
                                    new ImageIcon(Sprite.getImgDir() + "bKidStatL2_2.png").getImage() };
    private Image bKidSitL2 = new ImageIcon(Sprite.getImgDir() + "bKidSitL2.png").getImage();
    private Image lifeBalloon = new ImageIcon(Sprite.getImgDir() + "lifeBalloon.png").getImage();

    // for movement on title screen (not implemented)
    public final int ORGANIC_FRAMERATE = 20;        // framerate for organic movement animation
    private int animTicker;


    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================

    public GamePanel(Dimension dim){
        gmf = new GameFrame();
        gmf.getContentPane().add(this);
        gmf.pack();

        gmf.setSize(dim);
        gmf.setTitle("Balloon Fight");
        gmf.setResizable(false);
        gmf.setVisible(true);
        this.setFocusable(true);

        // got createFont code from http://www.java2s.com/Tutorials/Java/Graphics_How_to/Font/Load_font_from_ttf_file.htm
        // not functioning
        try { blockyFont = Font.createFont(Font.TRUETYPE_FONT,new File(fontDir + "blocky.ttf")); }
        catch (IOException | FontFormatException e) { System.out.println("Font file not found!"); }
    }

    @SuppressWarnings("unchecked")
    public void update(Graphics g) {
        if (grpOff == null || Game.DIM.width != dimOff.width
                || Game.DIM.height != dimOff.height) {
            dimOff = Game.DIM;
            imgOff = createImage(Game.DIM.width, Game.DIM.height);
            grpOff = imgOff.getGraphics();
        }

        // Fill in background with black.
        grpOff.setColor(Color.black);
        grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);
        if (!CommandCenter.getInstance().isTransition() && !CommandCenter.getInstance().isGameOver() && !CommandCenter.getInstance().isGameWon()) {
            // during gameplay
            if (CommandCenter.getInstance().isPlaying()) {
                drawBackground(grpOff);
                iterateMovables(grpOff,
                        (ArrayList<Movable>)  CommandCenter.getInstance().getClouds(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getBonusBalloons(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getPlatforms(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getBolts(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getScorePops(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getbKids(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getbFoxes(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getFishes(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getBubbles(),
                        (ArrayList<Movable>)  CommandCenter.getInstance().getDebris());
                if (CommandCenter.getInstance().getLevel() != Game.BONUS_LEVEL) { drawForeground(grpOff); }
                drawLives(grpOff);
                drawScore(grpOff);
                drawHighScore(grpOff);
            }
            else showTitleScreen(grpOff);
        }
        // level transition screen
        else if (CommandCenter.getInstance().isTransition()) {
            int level = CommandCenter.getInstance().getLevel();
            String levelDisp;
            if (level == Game.BONUS_LEVEL) { levelDisp = "BONUS"; }
            else if (level == Game.GAME_WON_LEVEL-1) { levelDisp = "FINAL LEVEL"; }
            else { levelDisp = "Level " + level; }
            drawTransitionScreen(grpOff, levelDisp);
        }
        else if (CommandCenter.getInstance().isGameOver()) drawTransitionScreen(grpOff, "GAME OVER");
        else if (CommandCenter.getInstance().isGameWon()) drawTransitionScreen(grpOff, "YOU WON");

        animTick();
        g.drawImage(imgOff, 0, 0, this);
    }

    public void iterateMovables(Graphics g, ArrayList<Movable>...movableLists) {
        for (ArrayList<Movable> movableList : movableLists) {
            for (Movable movable : movableList) {
                if (!CommandCenter.getInstance().isPaused()) movable.move();        // nothing moves if paused
                movable.draw(g);
            }
        }
    }

    public void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(CommandCenter.getInstance().getLevel() != Game.BONUS_LEVEL ? battleBackground : bonusBackground, 0,0,null);
    }

    public void drawForeground(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(battleForeground, 0, Game.DIM.height - (int)(2.4*battleForeground.getHeight(null)), null);
    }

    public void drawLives(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        for (int i = 1; i <= CommandCenter.getInstance().getLives(); i++) {
            g2d.drawImage(lifeBalloon, Game.DIM.width - (int)(lifeBalloon.getWidth(null)*1.5*i), 10, null);
        }
    }

    public void drawScore(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        //g2d.setFont(blockyFont);
        g2d.setColor(Color.WHITE);
        String strScore = String.format("%07d", CommandCenter.getInstance().getScore());
        g2d.drawString("SCORE " + strScore, 10,20);
    }

    public void drawHighScore(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        //g2d.setFont(blockyFont);
        g2d.setColor(Color.WHITE);
        String strHighScore = String.format("%07d", CommandCenter.getInstance().getHighScore());
        g2d.drawString("HIGH SCORE " + strHighScore, 278,20);
    }

    public void drawTransitionScreen(Graphics g, String str) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.setColor(Color.WHITE);
        g2d.drawString(str, Game.DIM.width/2 - 2*str.length(), Game.DIM.height/3);
    }

    public void showTitleScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g);
        g2d.drawImage(titleScreen,dimOff.width/2-titleScreen.getWidth(null)/2,
                                    dimOff.height/3-titleScreen.getHeight(null)/2,null);
        g2d.drawImage(bKidSitL2, 138, 80, null);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Press SPACE to Start", 280, 400);
    }

    public void animTick() {
        animTicker += 1;
    }

    public int getLoopingAnimFrame(int numFrames, int rate) {
        return (animTicker/rate)%numFrames;
    }
}
