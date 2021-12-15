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

        int bestEval = 0;
        String move = "right";
        for (Direction dir : Direction.values()) {
            game.step(dir);
            pathfinder.setSource(game.getHeadPos());
            pathfinder.setFloodFill(true);
            pathfinder.execute();
            int eval = pathfinder.getAreaCovered();
            if (eval > bestEval) {
                bestEval = eval;
                move = dir.getMove();
            }
            game.backtrack();
        }

        return move;
    }
}

