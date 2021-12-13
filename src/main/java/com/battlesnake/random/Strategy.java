package com.battlesnake.random;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Strategy {

    public static List<String> allMoves = Arrays.asList("up", "down", "left", "right");

    public static void start() {

    }

    public static void end() {

    }

    public static String move(MoveRequest moveRequest) {
        // shuffle moves randomly
        Collections.shuffle(allMoves);

        // find move that is in bounds and unoccupied
        for (String move : allMoves) {
            Position nextPos = moveRequest.getHeadPos().move(move);
            if (nextPos.x >= 0
            && nextPos.y >= 0
            && nextPos.x < moveRequest.getWidth()
            && nextPos.y < moveRequest.getHeight()
            && !moveRequest.isOccupied(nextPos)) {
                return move;
            }
        }

        // move right if no such move is found
        return "right";
    }

}

