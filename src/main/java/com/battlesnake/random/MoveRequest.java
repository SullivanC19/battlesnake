package com.battlesnake.random;

import com.fasterxml.jackson.databind.JsonNode;

public class MoveRequest {
    private final int turn;
    private final int width, height;

    private final Position headPos;

    private final boolean[][] occupied;

    public MoveRequest(JsonNode moveRequestObj) {
        this.turn = moveRequestObj.get("turn").asInt();
        this.width = moveRequestObj.get("board").get("width").asInt();
        this.height = moveRequestObj.get("board").get("height").asInt();
        this.headPos = new Position(moveRequestObj.get("you").get("head"));

        this.occupied = new boolean[width][height];
        initOccupied(moveRequestObj.get("board").get("snakes"));
    }

    private void initOccupied(JsonNode snakesObj) {
        for (JsonNode snakeObj : snakesObj) {
            for (JsonNode posObj : snakeObj.get("body")) {
                int x = posObj.get("x").asInt();
                int y = posObj.get("y").asInt();
                occupied[x][y] = true;
            }
        }
    }

    public int getTurn() { return turn; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Position getHeadPos() { return headPos; }

    public boolean isOccupied(int x, int y) {
        return occupied[x][y];
    }

    public boolean isOccupied(Position pos) {
        return isOccupied(pos.x, pos.y);
    }
}