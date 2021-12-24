package com.battlesnake.montecarlo;

import com.fasterxml.jackson.databind.JsonNode;

public class Position {
    byte x, y;

    public Position(byte x, byte y) {
        this.x = x;
        this.y = y;
    }

    public static Position fromJsonObj(JsonNode positionObj) {
        return new Position((byte) positionObj.get("x").asInt(), (byte) positionObj.get("y").asInt());
    }

    public Position move(Direction dir) {
        return new Position((byte) (x + dir.xDiff()), (byte) (y + dir.yDiff()));
    }

    public int hashCode(int width) {
        return x + y * width;
    }

    public boolean inBounds(int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public Direction directionTo(Position pos) {
        if (pos.x != x) {
            return pos.x < x ? Direction.LEFT : Direction.RIGHT;
        } else {
            return pos.y < y ? Direction.DOWN : Direction.UP;
        }
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(Position pos) {
        return pos.x == x && pos.y == y;
    }
}
