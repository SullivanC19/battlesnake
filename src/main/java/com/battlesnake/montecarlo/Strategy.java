package com.battlesnake.montecarlo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Strategy {

    private static final Logger LOG = LoggerFactory.getLogger(Strategy.class);

    public static List<String> allMoves = Arrays.asList("up", "down", "left", "right");

    public static void start() {

    }

    public static void end() {

    }

    public static String move(MoveRequest moveRequest) {
        Game game = moveRequest.getGame();
        Pathfinder pathfinder = new Pathfinder(game);


        long start = System.currentTimeMillis();

        List<Direction> moves = Arrays.asList(Direction.values());
        Collections.shuffle(moves);
        int bestEval = Integer.MIN_VALUE;
        String move = "right";
        for (Direction dir : moves) {
            if (!game.step(dir)) continue;
            pathfinder.setSource(game.getHeadPos());
            pathfinder.setFloodFill(true);
            pathfinder.execute();
            int eval = pathfinder.getAreaCovered() - (game.getHealth() < 5 ? 0 : game.getLength());
            LOG.info(eval + "");
            if (eval > bestEval) {
                bestEval = eval;
                move = dir.getMove();
            }
            game.backtrack();
        }

        LOG.info("" + (System.currentTimeMillis() - start));

        return move;
    }
}

