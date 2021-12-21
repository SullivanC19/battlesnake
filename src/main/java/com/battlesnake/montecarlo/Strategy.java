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
        for (int snakeIdx = 1; snakeIdx < game.getNumSnakes(); snakeIdx++) {
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
            for (int i = 0; i < 100; i++) {
                Direction[] directions = getRandomValidDirectionsArray(game);
                directions[0] = dir;
                game.step(directions);
                sum += evalRandomPath(game, pathfinder, 1, 20);
                game.backtrack();
            }
            if (sum > bestSum) {
                bestSum = sum;
                move = dir.getMove();
            }
        }

        LOG.info("" + (System.currentTimeMillis() - start));

        return move;
    }

    public static int evalRandomPath(Game game, Pathfinder pathfinder, int depth, int maxDepth) {
        if (!game.isAlive(0)) return -500;
        if (depth == maxDepth) return eval(game, pathfinder);

        game.step(getRandomValidDirectionsArray(game));
        int eval = evalRandomPath(game, pathfinder, depth + 1, maxDepth);
        game.backtrack();

        return eval;
    }

    public static int eval(Game game, Pathfinder pathfinder) {
        int health = game.getHealth();
        int length = game.getLength();

        pathfinder.setSource(game.getHeadPos());

        pathfinder.setFloodFill(true);
        pathfinder.execute();
        int territory = pathfinder.getAreaCovered();

        pathfinder.setFloodFill(false);
        pathfinder.setDest(game.getTailPos());
        pathfinder.execute();
        boolean safePath = pathfinder.canReachTarget();

        return (health < 10 ? 0 : -length)
                + territory
                + (safePath ? 100 : 0)
                + -50 * game.getNumLivingSnakes();
    }
}

