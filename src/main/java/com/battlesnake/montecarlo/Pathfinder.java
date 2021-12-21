package com.battlesnake.montecarlo;

import java.util.ArrayDeque;
import java.util.Queue;

public class Pathfinder {
    private Game game;
    private Position source;
    private Targeter targeter;
    private boolean floodFill;

    private Position target;
    private int distToTarget, areaCovered;
    private String nextMove;

    public Pathfinder(Game game) {
        this.game = game;
        this.source = null;
        this.targeter = null;
        this.floodFill = false;
    }

    public void setSource(Position source) {
        this.source = source;
    }

    public void setDest(Position targetPos) {
        this.targeter = (Position pos) -> { return pos.equals(targetPos); };
    }

    public void setTargeter(Targeter targeter) {
        this.targeter = targeter;
    }

    public void setFloodFill(boolean floodFill) {
        this.floodFill = floodFill;
    }

    public void execute() {
        if (source == null || (!floodFill && targeter == null)) {
            throw new RuntimeException("pathfinder not initialized");
        }

        int width = game.getWidth();
        int height = game.getHeight();

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
                if (game.canMoveOnto(nextPos)
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

    public Position getTarget() {
        return target;
    }

    public boolean canReachTarget() { return target != null; }

    public int getDistToTarget() {
        return distToTarget;
    }

    public int getAreaCovered() {
        return areaCovered;
    }

    public String getNextMove() {
        return nextMove;
    }

    public interface Targeter {
        public boolean isTarget(Position pos);
    }
}