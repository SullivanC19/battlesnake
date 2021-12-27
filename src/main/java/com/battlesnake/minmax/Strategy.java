package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Strategy {

    private static final long CUTOFF_TIME_ELAPSED = 400;
    private static final Logger LOG = LoggerFactory.getLogger(Strategy.class);

    private static Map<Long, Integer> estimatedValueOfPath;

    public static void start(JsonNode startRequestObj) {
        Game.init(startRequestObj);
        Pathfinder.init();
    }

    public static void end() {

    }

    private static List<Direction> getOrderedDirections(Position headPos, long pathHashCode, boolean descending) {
        return Arrays.stream(Direction.values())
                .filter(dir -> Game.canMoveOnto(headPos.move(dir)))
                .sorted((dir1, dir2) -> {
                    int path1Value = estimatedValueOfPath.getOrDefault(
                            4 * pathHashCode + dir1.hashCode(), 0);
                    int path2Value = estimatedValueOfPath.getOrDefault(
                            4 * pathHashCode + dir2.hashCode(), 0);

                    return descending ? path2Value - path1Value : path1Value - path2Value;
                })
                .collect(Collectors.toList());
    }

    public static String move(JsonNode moveRequestObj) {
        Game.update(moveRequestObj);
        Game.startTimer();

        Direction[] dirs = new Direction[Game.getNumSnakes()];
        byte mySnakeIdx = Game.getMySnakeIdx();
        byte nextSnakeIdx = (byte) ((mySnakeIdx + 1) % Game.getNumSnakes());

        estimatedValueOfPath = new HashMap<>();

        int maxDepth = 4;
        String move = "right";

        Outer:
        while (true) {
          int bestEval = Integer.MIN_VALUE;
          String bestMove = "right";

          for (Direction dir : Direction.values()) {
              if (!Game.canMoveOnto(Game.getHeadPos().move(dir))) continue;

              dirs[mySnakeIdx] = dir;
              int eval = evalMinMax(dirs,
                      nextSnakeIdx,
                      1,
                      maxDepth,
                      bestEval,
                      Integer.MAX_VALUE,
                      0);

              if (eval > bestEval) {
                  bestEval = eval;
                  bestMove = dir.getMove();
              }

              if (Game.getTimeElapsed() > CUTOFF_TIME_ELAPSED) {
                  break Outer;
              }
          }

          move = bestMove;
          maxDepth++;
        }
        
        LOG.info("maxDepth: " + maxDepth);
        LOG.info("time: " + Game.getTimeElapsed());

        return move;
    }

    public static int evalMinMax(Direction[] dirs,
                                 byte snakeIdx,
                                 int depth,
                                 int maxDepth,
                                 int alpha,
                                 int beta,
                                 long pathHashCode) {
        if (snakeIdx == Game.getMySnakeIdx()) {
            Game.step(dirs);

            if (Game.isOver() || depth == maxDepth) {
                int value = eval(depth);
                estimatedValueOfPath.put(pathHashCode, value);
                Game.backtrack();
                return value;
            }

            depth++;
        }

        Direction prevDir = dirs[snakeIdx];
        List<Direction> orderedDirections = getOrderedDirections(
                Game.getHeadPos(snakeIdx), pathHashCode, snakeIdx == Game.getMySnakeIdx());
        for (Direction dir : orderedDirections) {
            dirs[snakeIdx] = dir;
            int eval = evalMinMax(dirs,
                    (byte) ((snakeIdx + 1) % Game.getNumSnakes()),
                    depth,
                    maxDepth,
                    alpha,
                    beta,
                    pathHashCode * 4 + dir.hashCode());
            dirs[snakeIdx] = prevDir;

            if (snakeIdx == Game.getMySnakeIdx() && eval > alpha) {
                alpha = eval;
            }

            if (snakeIdx != Game.getMySnakeIdx() && eval < beta) {
                beta = eval;
            }

            if (alpha >= beta || Game.getTimeElapsed() > CUTOFF_TIME_ELAPSED) break;
        }

        if (snakeIdx == Game.getMySnakeIdx()) {
            Game.backtrack();
        }

        int value = snakeIdx == Game.getMySnakeIdx() ? alpha : beta;
        estimatedValueOfPath.put(pathHashCode, value);

        return value;
    }

    public static int eval(int depth) {
        byte mySnakeIdx = Game.getMySnakeIdx();
        byte oppSnakeIdx = (byte) ((mySnakeIdx + 1) % 2);

        if (Game.isOver()) {
            return Game.isAlive(mySnakeIdx) ? Integer.MAX_VALUE - depth : Integer.MIN_VALUE + depth;
        }

        int myLength = Game.getLength(mySnakeIdx);
        int oppLength = Game.getLength(oppSnakeIdx);

        Position myHeadPos = Game.getHeadPos(mySnakeIdx);
        Position oppHeadPos = Game.getHeadPos(oppSnakeIdx);

        int myOnEdge = Game.onEdge(myHeadPos) ? 1 : 0;
        int oppOnEdge = Game.onEdge(oppHeadPos) ? 1 : 0;

        return (myLength - oppLength) - 10 * (myOnEdge - oppOnEdge);
    }
}

