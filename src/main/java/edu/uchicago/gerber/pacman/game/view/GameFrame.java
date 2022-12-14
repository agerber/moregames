package edu.uchicago.gerber.pacman.game.view;
import java.awt.*;
import java.awt.event.WindowEvent;
import javax.swing.*;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

	private JPanel contentPane;
	private BorderLayout borderLayout1 = new BorderLayout();

	public GameFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Component initialization
	private void initialize() throws Exception {
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
	}

	@Override
	//Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}
}
