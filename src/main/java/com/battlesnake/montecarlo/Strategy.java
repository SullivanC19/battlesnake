package com.battlesnake.montecarlo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Strategy {

    private static final Logger LOG = LoggerFactory.getLogger(Strategy.class);

    public static void start() {

    }

    public static void end() {

    }

    private static List<Direction> scrambledDirections() {
        List<Direction> dirs = Arrays.asList(Direction.values());
        Collections.shuffle(dirs);
        return dirs;
    }

    private static Direction[] getRandomValidDirectionsArray() {
        Direction[] directions = new Direction[Game.getNumSnakes()];
        for (int snakeIdx = 0; snakeIdx < Game.getNumSnakes(); snakeIdx++) {
            directions[snakeIdx] = Direction.RIGHT; // default;
            for (Direction dir : scrambledDirections()) {
                Position nextPos = Game.getHeadPos(snakeIdx).move(dir);
                if (Game.canMoveOnto(nextPos)) {
                    directions[snakeIdx] = dir;
                    break;
                }
            }
        }

        return directions;
    }

    public static String move() {
        long start = System.currentTimeMillis();

        int bestSum = Integer.MIN_VALUE;
        String move = "right";
        for (Direction dir : scrambledDirections()) {
            if (!Game.canMoveOnto(Game.getHeadPos().move(dir))) continue;

            int sum = 0;
            for (int i = 0; i < 300; i++) {
                Direction[] directions = getRandomValidDirectionsArray();
                directions[Game.getMySnakeIdx()] = dir;
                Game.step(directions);
                sum += evalRandomPath(1, 20);
                Game.backtrack();
            }
            if (sum > bestSum) {
                bestSum = sum;
                move = dir.getMove();
            }
        }

        LOG.info("time: " + (System.currentTimeMillis() - start));

        return move;
    }

    public static int evalRandomPath(int depth, int maxDepth) {
        if (Game.isOver()) {
          return Game.isAlive(Game.getMySnakeIdx()) ? 500 - depth : -500 + depth;
        }

        if (depth == maxDepth) {
            return eval();
        }

        Direction[] dir = getRandomValidDirectionsArray();
        Game.step(dir);
        int eval = evalRandomPath(depth + 1, maxDepth);
        Game.backtrack();

        return eval;
    }

    public static int eval() {
        int length = Game.getLength();
        return length;
    }
}

