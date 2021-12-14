package com.battlesnake.findfood;

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
        int width = moveRequest.getWidth();
        int height = moveRequest.getHeight();

        Queue<Integer> toVisit = new LinkedList<Integer>();
        boolean[][] inQueue = new boolean[width][height];
        int[][] dist = new int[width][height];
        Position[][] par = new Position[width][height];

        Position start = moveRequest.getHeadPos();
        toVisit.add(start.hashCode(width));
        inQueue[start.x][start.y] = true;
        dist[start.x][start.y] = 0;
        par[start.x][start.y] = null;

        Position targetPos = null;
        while (!toVisit.isEmpty() && targetPos == null) {
            Position pos = Position.fromHash(toVisit.remove(), width);
            for (int dir = 0; dir < 4; dir++) {
                int nextX = pos.x + (dir % 2 == 0 ? dir - 1 : 0);
                int nextY = pos.y + (dir % 2 == 1 ? dir - 2 : 0);
                Position nextPos = new Position(nextX, nextY);
                if (nextPos.inBounds(width, height)
                        && !moveRequest.isOccupied(nextPos)
                        && !inQueue[nextX][nextY]) {
                    toVisit.add(nextPos.hashCode(width));
                    inQueue[nextX][nextY] = true;
                    dist[nextX][nextY] = dist[pos.x][pos.y] + 1;
                    par[nextX][nextY] = pos;
                    if (moveRequest.isFood(nextPos)) {
                        targetPos = nextPos;
                        break;
                    }
                }
            }
        }

        if (targetPos != null
                && dist[targetPos.x][targetPos.y] >= moveRequest.getHealth()) {
            return backtrack(targetPos, start, par);
        } else {
            return moveRandomly(moveRequest);
        }

    }

    private static String backtrack(Position targetPos, Position start, Position[][] par) {
        Position temp = targetPos;
        while (par[temp.x][temp.y] != start) {
            temp = par[temp.x][temp.y];
        }

        for (String move : allMoves) {
            if (start.move(move).equals(temp)) {
                return move;
            }
        }

        return "right";
    }

    private static String moveRandomly(MoveRequest moveRequest) {
        // shuffle moves randomly
        Collections.shuffle(allMoves);

        // find move that is in bounds and unoccupied
        for (String move : allMoves) {
            Position nextPos = moveRequest.getHeadPos().move(move);
            if (nextPos.inBounds(moveRequest.getWidth(), moveRequest.getHeight())
                    && !moveRequest.isOccupied(nextPos)) {
                return move;
            }
        }

        // move right if no such move is found
        return "right";
    }
}

