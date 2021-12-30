package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Strategy {

    private static final long CUTOFF_TIME_ELAPSED = 200;
    private static final Logger LOG = LoggerFactory.getLogger(Strategy.class);

    private static long moveStartTime;
    private static Map<Long, Integer> estimatedValueOfPath;

    public static void start(JsonNode startRequestObj) {

    }

    public static void end() {

    }

    public static String move(JsonNode moveRequestObj) {
        startMoveTimer();

        Game game = new Game(moveRequestObj);
        Pathfinder pathfinder = new Pathfinder(game);
        estimatedValueOfPath = new HashMap<>();
        Direction[] dirs = new Direction[game.getNumSnakes()];

        int maxDepth = 4;
        String move = "right";
        int bestEval = Integer.MIN_VALUE;

        Outer:
        while (true) {
          bestEval = Integer.MIN_VALUE;
          String bestMove = "right";

          for (Direction dir : Direction.values()) {
              if (!game.canMoveOnto(game.getHeadPos(0).move(dir))) continue;

              dirs[0] = dir;
              int eval = evalMinMax(dirs,
                      1,
                      1,
                      maxDepth,
                      bestEval,
                      Integer.MAX_VALUE,
                      dir.index(),
                      game);

              if (eval > bestEval) {
                  bestEval = eval;
                  bestMove = dir.getMove();
              }

              if (getTimeElapsed() > CUTOFF_TIME_ELAPSED) {
                  break Outer;
              }
          }

          move = bestMove;
          maxDepth++;
        }
        
        LOG.info("eval: " + bestEval);
        LOG.info("maxDepth: " + maxDepth);
        LOG.info("time: " + getTimeElapsed());

        return move;
    }

    public static int evalMinMax(Direction[] dirs,
                                 int snakeIdx,
                                 int depth,
                                 int maxDepth,
                                 int alpha,
                                 int beta,
                                 long pathHashCode,
                                 Game game) {
        if (snakeIdx == 0) {
            game.step(dirs);

            if (game.getCurrentGameState() != Game.GAME_STATE_IN_PROGRESS || depth == maxDepth) {
                int value = eval(game, depth);
                estimatedValueOfPath.put(pathHashCode, value);
                game.backtrack();
                return value;
            }

            depth++;
        }

        Direction prevDir = dirs[snakeIdx];
        List<Direction> orderedDirections = getOrderedDirections(
                game.getHeadPos(snakeIdx),
                pathHashCode,
                snakeIdx == 0,
                game);

        if (snakeIdx == 0) {
          alpha = Math.max(alpha, Integer.MIN_VALUE + depth);
        } else {
          beta = Math.min(beta, Integer.MAX_VALUE - depth);
        }

        for (Direction dir : orderedDirections) {
            dirs[snakeIdx] = dir;
            int eval = evalMinMax(dirs,
                    (snakeIdx + 1) % game.getNumSnakes(),
                    depth,
                    maxDepth,
                    alpha,
                    beta,
                    pathHashCode * 4 + dir.index(),
                    game);
            dirs[snakeIdx] = prevDir;

            if (snakeIdx == 0 && eval > alpha) {
                alpha = eval;
            }

            if (snakeIdx != 0 && eval < beta) {
                beta = eval;
            }

            if (alpha >= beta || getTimeElapsed() > CUTOFF_TIME_ELAPSED) {
                break;
            }
        }

        if (snakeIdx == 0) {
            game.backtrack();
        }

        int value = snakeIdx == 0 ? alpha : beta;
        estimatedValueOfPath.put(pathHashCode, value);

        return value;
    }

    private static int eval(Game game, int depth) {
        byte gameState = game.getCurrentGameState();

        if (gameState == Game.GAME_STATE_OVER_DRAW) return 0;
        if (gameState == Game.GAME_STATE_OVER_LOSS) return Integer.MIN_VALUE + depth;
        if (gameState == Game.GAME_STATE_OVER_WIN) return Integer.MAX_VALUE - depth;

        // game in progress
        return 0;
    }

    private static void startMoveTimer() {
        moveStartTime = System.currentTimeMillis();
    }

    private static long getTimeElapsed() {
        return System.currentTimeMillis() - moveStartTime;
    }

    private static List<Direction> getOrderedDirections(Position headPos, long pathHashCode, boolean descending, Game game) {
        return Arrays.stream(Direction.values())
                .filter(dir -> game.canMoveOnto(headPos.move(dir)))
                .sorted((dir1, dir2) -> {
                    int path1Value = estimatedValueOfPath.getOrDefault(
                            4 * pathHashCode + dir1.index(), 0);
                    int path2Value = estimatedValueOfPath.getOrDefault(
                            4 * pathHashCode + dir2.index(), 0);

                    return descending ? path2Value - path1Value : path1Value - path2Value;
                })
                .collect(Collectors.toList());
    }
}

