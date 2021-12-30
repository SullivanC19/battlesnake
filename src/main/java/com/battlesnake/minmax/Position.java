package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;

public class Position {
    int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Position fromJsonObj(JsonNode positionObj) {
        return new Position(positionObj.get("x").asInt(), positionObj.get("y").asInt());
    }

    public Position move(Direction dir) {
        return new Position(x + dir.xDiff(), y + dir.yDiff());
    }


    public boolean inBounds(int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }
    public boolean onEdge(int width, int height) {
        return x == 0 || y == 0 || x == width - 1 || y == height - 1;
    }
    public Direction directionTo(Position pos) {
        if (pos.x != x) {
            return pos.x < x ? Direction.LEFT : Direction.RIGHT;
        }
        
        if (pos.y != y) {
            return pos.y < y ? Direction.DOWN : Direction.UP;
        }

        return null;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    public int hashCode() {
        return x + y * Game.MAX_BOARD_SIZE;
    }
    public boolean equals(Position pos) {
        return pos.x == x && pos.y == y;
    }
}
