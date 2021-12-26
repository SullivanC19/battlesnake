package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;

public class Position {
    short x, y;

    public Position(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public static Position fromJsonObj(JsonNode positionObj) {
        return new Position((short) positionObj.get("x").asInt(), (short) positionObj.get("y").asInt());
    }

    public Position move(Direction dir) {
        return new Position((short) (x + dir.xDiff()), (short) (y + dir.yDiff()));
    }

    public int hashCode(int width) {
        return x + y * width;
    }

    public boolean inBounds(short width, short height) {
        return x >= 0 && y >= 0 && x < width && y < height;
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

    public boolean equals(Position pos) {
        return pos.x == x && pos.y == y;
    }
}
