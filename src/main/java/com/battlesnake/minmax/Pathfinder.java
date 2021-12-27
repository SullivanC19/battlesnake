package com.battlesnake.minmax;

import java.util.ArrayDeque;
import java.util.Queue;

public class Pathfinder {
    private static Position source;
    private static Targeter targeter;
    private static boolean floodFill;

    private static Position target;
    private static int distToTarget, areaCovered;
    private static String nextMove;

    public static void init() {
        source = null;
        targeter = null;
        floodFill = false;
    }

    public static void setSource(Position source) {
        Pathfinder.source = source;
    }

    public static void setDest(Position targetPos) {
        Pathfinder.targeter = (Position pos) -> { return pos.equals(targetPos); };
    }

    public static void setTargeter(Targeter targeter) {
        Pathfinder.targeter = targeter;
    }

    public static void setFloodFill(boolean floodFill) {
        Pathfinder.floodFill = floodFill;
    }

    public static void execute() {
        if (source == null || (!floodFill && targeter == null)) {
            throw new RuntimeException("pathfinder not initialized");
        }

        int width = Game.getWidth();
        int height = Game.getHeight();

        // largest possible number of nodes in queue is perimeter of outer rectangle
        Queue<Position> toVisit = new ArrayDeque<>((width + height) * 2);
        boolean[][] inQueue = new boolean[width][height];
        int[][] dist = new int[width][height];
        Direction[][] backtrackDir = new Direction[width][height];

        toVisit.add(source);
        inQueue[source.x][source.y] = true;
        dist[source.x][source.y] = 0;
        backtrackDir[source.x][source.y] = null;

        target = null;
        nextMove = null;
        distToTarget = Integer.MAX_VALUE;
        areaCovered = 0;
        while (!toVisit.isEmpty() && target == null) {
            Position pos = toVisit.remove();
            areaCovered++;

            for (Direction dir : Direction.values()) {
                Position nextPos = pos.move(dir);
                if (Game.canMoveOnto(nextPos, dist[pos.x][pos.y])
                        && !inQueue[nextPos.x][nextPos.y]) {
                    toVisit.add(nextPos);
                    inQueue[nextPos.x][nextPos.y] = true;
                    dist[nextPos.x][nextPos.y] = dist[pos.x][pos.y] + 1;
                    backtrackDir[nextPos.x][nextPos.y] = dir.opposite();
                    if (!floodFill && targeter.isTarget(nextPos)) {
                        target = nextPos;
                        distToTarget = dist[nextPos.x][nextPos.y];
                        break;
                    }
                }
            }
        }

        if (target != null) {
            nextMove = backtrack(target, source, backtrackDir);
        }
    }

    private static String backtrack(Position targetPos, Position start, Direction[][] backtrackDir) {
        Direction lastDir = null;
        Position temp = targetPos;
        while (!temp.equals(start)) {
            lastDir = backtrackDir[temp.x][temp.y];
            temp = temp.move(lastDir);
        }

        return lastDir == null ? null : lastDir.getMove();
    }

    public static Position getTarget() {
        return target;
    }

    public static boolean canReachTarget() { return target != null; }

    public static int getDistToTarget() {
        return distToTarget;
    }

    public static int getAreaCovered() {
        return areaCovered;
    }

    public static String getNextMove() {
        return nextMove;
    }

    public interface Targeter {
        public boolean isTarget(Position pos);
    }
}