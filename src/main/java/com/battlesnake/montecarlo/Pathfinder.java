package com.battlesnake.montecarlo;

import java.util.LinkedList;
import java.util.PriorityQueue;
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

        Queue<Position> toVisit = new LinkedList<>();
        boolean[][] inQueue = new boolean[width][height];
        int[][] dist = new int[width][height];
        Position[][] par = new Position[width][height];

        toVisit.add(source);
        inQueue[source.x][source.y] = true;
        dist[source.x][source.y] = 0;
        par[source.x][source.y] = null;

        target = null;
        nextMove = null;
        distToTarget = Integer.MAX_VALUE;
        areaCovered = 0;
        while (!toVisit.isEmpty() && target == null) {
            Position pos = toVisit.remove();
            areaCovered++;

            for (Direction dir : Direction.values()) {
                Position nextPos = pos.move(dir);
                if (nextPos.inBounds(width, height)
                        && !game.isOccupied(nextPos)
                        && !inQueue[nextPos.x][nextPos.y]) {
                    toVisit.add(nextPos);
                    inQueue[nextPos.x][nextPos.y] = true;
                    dist[nextPos.x][nextPos.y] = dist[pos.x][pos.y] + 1;
                    par[nextPos.x][nextPos.y] = pos;
                    if (!floodFill && targeter.isTarget(nextPos)) {
                        target = nextPos;
                        distToTarget = dist[nextPos.x][nextPos.y];
                        break;
                    }
                }
            }
        }

        if (target != null) {
            nextMove = backtrack(target, source, par);
        }
    }

    private static String backtrack(Position targetPos, Position start, Position[][] par) {
        Position temp = targetPos;
        while (!par[temp.x][temp.y].equals(start)) {
            temp = par[temp.x][temp.y];
        }

        for (Direction dir : Direction.values()) {
            if (start.move(dir).equals(temp)) {
                return dir.getMove();
            }
        }

        return null;
    }

    public Position getTarget() {
        return target;
    }

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