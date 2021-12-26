package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Strategy {

    private static final Logger LOG = LoggerFactory.getLogger(Strategy.class);

    public static void start(JsonNode startRequestObj) {
        Game.init(startRequestObj);
        Pathfinder.init();
    }

    public static void end() {

    }

    private static List<Direction> scrambledDirections() {
        List<Direction> dirs = Arrays.asList(Direction.values());
        Collections.shuffle(dirs);
        return dirs;
    }

    public static String move(JsonNode moveRequestObj) {
        Game.update(moveRequestObj);

        long start = System.currentTimeMillis();

        Direction[] dirs = new Direction[Game.getNumSnakes()];
        byte mySnakeIdx = Game.getMySnakeIdx();
        byte nextSnakeIdx = (byte) (mySnakeIdx + 1 % Game.getNumSnakes());

        int bestEval = Integer.MIN_VALUE;
        String move = "right";
        for (Direction dir : scrambledDirections()) {
            if (!Game.canMoveOnto(Game.getHeadPos().move(dir))) continue;

            dirs[mySnakeIdx] = dir;
            int eval = evalMinMax(dirs, nextSnakeIdx, 1, 6, bestEval, Integer.MAX_VALUE);
            if (eval > bestEval) {
                bestEval = eval;
                move = dir.getMove();
            }
        }

        LOG.info("time: " + (System.currentTimeMillis() - start));

        return move;
    }

    public static int evalMinMax(Direction[] dirs, byte snakeIdx, int depth, int maxDepth, int alpha, int beta) {
        if (snakeIdx == Game.getMySnakeIdx()) {
            Game.step(dirs);

            if (Game.isOver() || depth == maxDepth) {
                int eval = eval(depth);
                Game.backtrack();
                return eval;
            }

            depth++;
        }

        for (Direction dir : scrambledDirections()) {
            if (!Game.canMoveOnto(Game.getHeadPos(snakeIdx).move(dir))) continue;

            dirs[snakeIdx] = dir;
            int eval = evalMinMax(dirs,
                    (byte) ((snakeIdx + 1) % Game.getNumSnakes()),
                    depth,
                    maxDepth,
                    alpha,
                    beta);

            if (snakeIdx == Game.getMySnakeIdx() && eval > alpha) {
                alpha = eval;
            }

            if (snakeIdx != Game.getMySnakeIdx() && eval < beta) {
                beta = eval;
            }

            if (alpha >= beta) break;
        }

        if (snakeIdx == Game.getMySnakeIdx()) {
            Game.backtrack();
        }

        return snakeIdx == Game.getMySnakeIdx() ? alpha : beta;
    }

    public static int eval(int depth) {
        byte mySnakeIdx = Game.getMySnakeIdx();

        if (Game.isOver()) {
            return Game.isAlive(mySnakeIdx) ? Integer.MAX_VALUE - depth : Integer.MIN_VALUE + depth;
        }

        int myLength = Game.getLength(mySnakeIdx);
        int oppLength = Game.getLength((byte) (mySnakeIdx + 1 % 2));
        return Math.min(1, myLength - oppLength);
    }
}

