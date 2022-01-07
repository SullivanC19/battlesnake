package com.battlesnake.minmax;

import java.util.ArrayDeque;
import java.util.Queue;

public class Pathfinder {
    private Game game;

    private Position[] sources;
    private Targeter targeter;
    private boolean floodFill;

    private Position target;
    private int distToTarget;
    private int[] areaCovered;
    private String nextMove;

    public Pathfinder(Game game) {
        this.game = game;

        sources = null;
        targeter = null;
        floodFill = false;
    }

    public void setSource(Position[] sources) {
        this.sources = sources;
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
        if (sources == null || (!floodFill && targeter == null)) {
            throw new RuntimeException("pathfinder not initialized");
        }

        int width = game.getWidth();
        int height = game.getHeight();

        // largest possible number of nodes in queue is perimeter of outer rectangle
        Queue<Position> toVisit = new ArrayDeque<>((width + height) * 2);
        boolean[][] inQueue = new boolean[width][height];
        int[][] dist = new int[width][height];
        int[][] sourceIdx = new int[width][height];
        Direction[][] backtrackDir = new Direction[width][height];

        for (int i = 0; i < sources.length; i++) {
            toVisit.add(sources[i]);
            inQueue[sources[i].x][sources[i].y] = true;
            dist[sources[i].x][sources[i].y] = 0;
            sourceIdx[sources[i].x][sources[i].y] = i;
            backtrackDir[sources[i].x][sources[i].y] = null;
        }

        target = null;
        nextMove = null;
        distToTarget = Integer.MAX_VALUE;
        areaCovered = new int[sources.length];
        while (!toVisit.isEmpty() && target == null) {
            Position pos = toVisit.remove();
            areaCovered[sourceIdx[pos.x][pos.y]]++;

            for (Direction dir : Direction.values()) {
                Position nextPos = pos.move(dir);
                if (!floodFill && targeter.isTarget(nextPos)) {
                    target = nextPos;
                    distToTarget = dist[nextPos.x][nextPos.y];
                    break;
                }

                if (game.canMoveOnto(nextPos)
                        && !inQueue[nextPos.x][nextPos.y]) {
                    toVisit.add(nextPos);
                    inQueue[nextPos.x][nextPos.y] = true;
                    dist[nextPos.x][nextPos.y] = dist[pos.x][pos.y] + 1;
                    sourceIdx[nextPos.x][nextPos.y] = sourceIdx[pos.x][pos.y];
                    backtrackDir[nextPos.x][nextPos.y] = dir.opposite();
                }
            }
        }

        if (target != null) {
            nextMove = backtrack(target, backtrackDir);
        }
    }

    private static String backtrack(Position targetPos, Direction[][] backtrackDir) {
        Direction lastBacktrackDir = null;
        Position temp = targetPos;
        while (backtrackDir[temp.x][temp.y] != null) {
            lastBacktrackDir = backtrackDir[temp.x][temp.y];
            temp = temp.move(lastBacktrackDir);
        }

        return lastBacktrackDir == null ? null : lastBacktrackDir.opposite().getMove();
    }

    public Position getTarget() {
        return target;
    }

    public boolean canReachTarget() { return target != null; }

    public int getDistToTarget() {
        return distToTarget;
    }

    public int getAreaCovered(int sourceIdx) {
        return areaCovered[sourceIdx];
    }

    public String getNextMove() {
        return nextMove;
    }

    public interface Targeter {
        public boolean isTarget(Position pos);
    }
}