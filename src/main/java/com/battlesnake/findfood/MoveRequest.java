package com.battlesnake.findfood;

import com.fasterxml.jackson.databind.JsonNode;

public class MoveRequest {
    private final int turn;
    private final int width, height;

    private final Position headPos;
    private final int health;

    private final boolean[][] occupied;
    private final boolean[][] food;

    public MoveRequest(JsonNode moveRequestObj) {
        this.turn = moveRequestObj.get("turn").asInt();
        this.width = moveRequestObj.get("board").get("width").asInt();
        this.height = moveRequestObj.get("board").get("height").asInt();
        this.headPos = Position.fromJsonObj(moveRequestObj.get("you").get("head"));
        this.health = moveRequestObj.get("you").get("health").asInt();

        this.occupied = new boolean[width][height];
        initOccupied(moveRequestObj.get("board").get("snakes"));

        this.food = new boolean[width][height];
        initFood(moveRequestObj.get("board").get("food"));
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

    private void initFood(JsonNode foodObj) {
        for (JsonNode posObj : foodObj) {
            int x = posObj.get("x").asInt();
            int y = posObj.get("y").asInt();
            food[x][y] = true;
        }
    }

    public int getTurn() {
        return turn;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position getHeadPos() {
        return headPos;
    }

    public int getHealth() {
        return health;
    }

    public boolean isOccupied(int x, int y) {
        return occupied[x][y];
    }

    public boolean isOccupied(Position pos) {
        return isOccupied(pos.x, pos.y);
    }

    public boolean isFood(int x, int y) {
        return food[x][y];
    }

    public boolean isFood(Position pos) {
        return isFood(pos.x, pos.y);
    }
}