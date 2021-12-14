package com.battlesnake.findfood;

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

    public static Position fromHash(int hashCode, int width) {
        return new Position(hashCode % width, hashCode / width);
    }

    public Position move(String move) {
        Position nextPos = null;
        switch (move) {
            case "up":
                nextPos = new Position(x, y + 1);
                break;
            case "down":
                nextPos = new Position(x, y - 1);
                break;
            case "left":
                nextPos = new Position(x - 1, y);
                break;
            case "right":
                nextPos = new Position(x + 1, y);
                break;
        }

        return nextPos;
    }

    public int hashCode(int width) {
        return x + y * width;
    }

    public boolean inBounds(int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(Position pos) {
        return pos.x == x && pos.y == y;
    }
}
