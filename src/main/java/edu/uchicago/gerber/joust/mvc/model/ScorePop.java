package edu.uchicago.gerber.joust.mvc.model;

import java.awt.*;

public class ScorePop extends Sprite {

    private int points;
    private int timeLeft;

    public ScorePop(Point pos, int points) {
        super(pos, Team.SCOREPOP);
        this.points = points;
        timeLeft = 12;
        if (points != CommandCenter.ONE_UP_POINTS) {
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + points);
        }
        else {
            CommandCenter.getInstance().setLives(CommandCenter.getInstance().getLives() + 1);
        }
    }

    @Override
    public void move() {
        if (!CommandCenter.getInstance().isPaused()) { timeLeft -= 1; }
        if (timeLeft <= 0) { CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE); }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString(points != CommandCenter.ONE_UP_POINTS ? "" + points : "1-UP", getX() + 20, getY());
    }

    public int getTimeLeft() { return timeLeft; }
    public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }
}
