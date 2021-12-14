package com.battlesnake.findfood;

import java.util.*;

public class Strategy {

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

        Position start = moveRequest.getHeadPos();
        toVisit.add(start.hashCode(width));
        inQueue[start.x][start.y] = true;
        dist[start.x][start.y] = 0;

        Position targetPos = null;
        while (!toVisit.isEmpty() && targetPos == null) {
            Position node = Position.fromHash(toVisit.remove(), width);
            for (int dir = 0; dir < 4; dir++) {
                int nextX = node.x + (dir % 2 == 0 ? dir - 1 : 0);
                int nextY = node.y + (dir % 2 == 1 ? dir - 2 : 0);
                Position nextPos = new Position(nextX, nextY);
                if (nextPos.inBounds(width, height)
                        && !moveRequest.isOccupied(nextPos)
                        && !inQueue[nextX][nextY]) {
                    toVisit.add(nextPos.hashCode(width));
                    inQueue[nextX][nextY] = true;
                    dist[nextX][nextY] = dist[node.x][node.y] + 1;
                    if (moveRequest.isFood(nextPos)) {
                        targetPos = nextPos;
                        break;
                    }
                }
            }
        }

        if (targetPos != null
                && dist[targetPos.x][targetPos.y] >= moveRequest.getHealth()) {
            Collections.shuffle(allMoves);

            Position headPos = moveRequest.getHeadPos();
            for (String move : allMoves) {
                Position nextPos = headPos.move(move);
                if (nextPos.inBounds(moveRequest.getWidth(), moveRequest.getHeight())
                        && !moveRequest.isOccupied(nextPos)
                        && dist[nextPos.x][nextPos.y] < dist[headPos.x][headPos.y]) {
                    return move;
                }
            }

            return "right";
        } else {
            return moveRandomly(moveRequest);
        }

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

