package edu.uchicago.gerber.galaga.view;


import edu.uchicago.gerber.galaga.controller.Game;
import edu.uchicago.gerber.galaga.model.CommandCenter;
import edu.uchicago.gerber.galaga.model.Falcon;
import edu.uchicago.gerber.galaga.model.Movable;

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

	// The following "off" vars are used for the off-screen double-buffered image.
	private Image imgOff;
	private Graphics grpOff;

	private GameFrame gmf;
	private Font fnt = new Font("SansSerif", Font.BOLD, 12);
	private Font fntBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
	private FontMetrics fmt;
	private int fontWidth;
	private int fontHeight;
	private String strDisplay = "";


	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================

	public GamePanel(Dimension dim) {
		gmf = new GameFrame();
		gmf.getContentPane().add(this);
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
		if (CommandCenter.getInstance().getScore() != 0) {
			g.drawString("SCORE :  " + CommandCenter.getInstance().getScore(), fontWidth, fontHeight);
		} else {
			g.drawString("NO SCORE", fontWidth, fontHeight);
		}
	}

	@SuppressWarnings("unchecked")
	public void update(Graphics g) {
		//create an image off-screen
		imgOff = createImage(Game.DIM.width, Game.DIM.height);
		//get its graphics context
		grpOff = imgOff.getGraphics();

		//Fill the off-screen image background with black.
		grpOff.setColor(Color.black);
		grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);

		drawScore(grpOff);

		if (CommandCenter.getInstance().isGameOver()) {
			displayTextOnScreen(grpOff,
					"GAME OVER",
					"use the arrow keys to turn and thrust",
					"use the space bar to fire",
					"'S' to Start",
					"'P' to Pause",
					"'Q' to Quit",
					"left pinkie on 'A' for Shield",
					"'Numeric-Enter' for Hyperspace"

			);
		} else if (CommandCenter.getInstance().isPaused()) {
			strDisplay = "Game Paused";
			grpOff.drawString(strDisplay,
					(Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4);
		}

		//playing and not paused!
		else {

			iterateMovables(grpOff,
					CommandCenter.getInstance().getMovDebris(),
					CommandCenter.getInstance().getMovFloaters(),
					CommandCenter.getInstance().getMovFoes(),
					CommandCenter.getInstance().getMovFriends());


			drawNumberShipsLeft(grpOff);


		}

		//after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
		// of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
		// without the use of a double-buffered off-screen image, you will see flickering.
		g.drawImage(imgOff, 0, 0, this);
	}


	@SafeVarargs
	private final void iterateMovables(final Graphics g, List<Movable>... arrayOfListMovables) {

		BiConsumer<Graphics, Movable> moveDraw = (grp, mov) -> {
			mov.move();
			mov.draw(grp);
		};

		//we use flatMap to flatten the List<Movable>[] passed-in above into a single stream of Movables
		Arrays.stream(arrayOfListMovables) //Stream<List<Movable>>
				.flatMap(Collection::stream) //Stream<Movable>
				.forEach(m -> moveDraw.accept(g, m));


	}


	private void drawNumberShipsLeft(Graphics g) {
		int numFalcons = CommandCenter.getInstance().getNumFalcons();
		while (numFalcons > 0) {
			drawOneShipLeft(g, numFalcons--);
		}
	}

	// Draw the number of falcons left on the bottom-right of the screen. Upside-down, but ok.
	private void drawOneShipLeft(Graphics g, int offSet) {
		Falcon falcon = CommandCenter.getInstance().getFalcon();

		g.setColor(falcon.getColor());

		g.drawPolygon(
				Arrays.stream(falcon.getCartesians())
						.map(pnt -> pnt.x + Game.DIM.width - (20 * offSet))
						.mapToInt(Integer::intValue)
						.toArray(),

				Arrays.stream(falcon.getCartesians())
						.map(pnt -> pnt.y + Game.DIM.height - 40)
						.mapToInt(Integer::intValue)
						.toArray(),

				falcon.getCartesians().length);


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

		AtomicInteger spacer = new AtomicInteger(0);
		Arrays.stream(lines)
				.forEach(s -> {
							graphics.drawString(s, (Game.DIM.width - fmt.stringWidth(s)) / 2,
									Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40));
						}
				);


	}


}
