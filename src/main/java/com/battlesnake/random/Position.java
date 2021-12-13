package com.battlesnake.random;

import com.fasterxml.jackson.databind.JsonNode;

public class Position {
    int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(JsonNode positionObj) {
        this.x = positionObj.get("x").asInt();
        this.y = positionObj.get("y").asInt();
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
}
