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

    private static Direction[] getRandomValidDirectionsArray(Game game) {
        Direction[] directions = new Direction[game.getNumSnakes()];
        for (int snakeIdx = 0; snakeIdx < game.getNumSnakes(); snakeIdx++) {
            directions[snakeIdx] = Direction.RIGHT; // default;
            for (Direction dir : scrambledDirections()) {
                Position nextPos = game.getHeadPos(snakeIdx).move(dir);
                if (game.canMoveOnto(nextPos)) {
                    directions[snakeIdx] = dir;
                    break;
                }
            }
        }

        return directions;
    }

    public static String move(MoveRequest moveRequest) {
        Game game = moveRequest.getGame();
        Pathfinder pathfinder = new Pathfinder(game);

        long start = System.currentTimeMillis();

        int bestSum = Integer.MIN_VALUE;
        String move = "right";
        for (Direction dir : scrambledDirections()) {
            int sum = 0;
            for (int i = 0; i < 300; i++) {
                Direction[] directions = getRandomValidDirectionsArray(game);
                directions[game.getMySnakeIdx()] = dir;
                game.step(directions);
                sum += evalRandomPath(game, 1, 20);
                game.backtrack();
            }
            if (sum > bestSum) {
                bestSum = sum;
                move = dir.getMove();
            }
        }

        LOG.info("time: " + (System.currentTimeMillis() - start));

        return move;
    }

    public static int evalRandomPath(Game game, int depth, int maxDepth) {
        if (game.isOver()) {
          return game.isAlive(game.getMySnakeIdx()) ? 500 - depth : -500 + depth;
        }
        if (depth == maxDepth) return eval(game);

        Direction[] dir = getRandomValidDirectionsArray(game);
        game.step(dir);
        int eval = evalRandomPath(game, depth + 1, maxDepth);
        game.backtrack();

        return eval;
    }

    public static int eval(Game game) {
        int length = game.getLength();
        return length;
    }
}

