package edu.uchicago.gerber.g1941.mvc.controller;

import edu.uchicago.gerber.g1941.mvc.model.Movable;

import java.util.concurrent.LinkedBlockingDeque;

// -------------------------------------------------------------
// No change in this file
// -------------------------------------------------------------


/**
 * Effectively a Queue that enqueues and dequeues Game Operations (add/remove).
 * enqueue() may be called by main and animation threads simultaneously, therefore we
 * use a data structure from the java.util.concurrent package.
 */
public class GameOpsQueue extends LinkedBlockingDeque<GameOp> {

    public void enqueue(Movable mov, GameOp.Action action) {
        addLast(new GameOp(mov, action));
    }

    public GameOp dequeue() {
        return removeFirst();
    }



}
