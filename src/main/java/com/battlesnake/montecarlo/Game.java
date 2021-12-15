package com.battlesnake.montecarlo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Stack;

public class Game {
    private int depth;
    private boolean gameOver;
    private final int width, height;

    private Position headPos;
    private int health;
    private int length;

    private int[][] snakeDepthOccupied;
    private boolean[][] food;

    private Stack<Move> moveStack;

    public Game(JsonNode moveRequestObj) {
        this.depth = 0;
        this.gameOver = false;

        this.width = moveRequestObj.get("board").get("width").asInt();
        this.height = moveRequestObj.get("board").get("height").asInt();

        this.headPos = Position.fromJsonObj(moveRequestObj.get("you").get("head"));
        this.health = moveRequestObj.get("you").get("health").asInt();
        this.length = moveRequestObj.get("you").get("length").asInt();

        this.snakeDepthOccupied = new int[width][height];
        this.food = new boolean[width][height];

        initSnakeDepthOccupied(moveRequestObj.get("you"));
        initFood(moveRequestObj.get("board").get("food"));
    }

    private void initSnakeDepthOccupied(JsonNode snakeObj) {
        int i = 0;
        for (JsonNode posObj : snakeObj.get("body")) {
            int x = posObj.get("x").asInt();
            int y = posObj.get("y").asInt();
            snakeDepthOccupied[x][y] = -(i++);
        }
    }

    private void initFood(JsonNode foodObj) {
        for (JsonNode posObj : foodObj) {
            int x = posObj.get("x").asInt();
            int y = posObj.get("y").asInt();
            food[x][y] = true;
        }
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

    public boolean isOccupied(Position pos) {
        return depth - snakeDepthOccupied[pos.x][pos.y] <= length;
    }

    public boolean isFood(Position pos) {
        return food[pos.x][pos.y];
    }

    public void step(Direction dir) {
        if (gameOver) return;

        Position nextHeadPos = headPos.move(dir);
        if (health == 0 || !nextHeadPos.inBounds(width, height) || isOccupied(nextHeadPos)) {
            gameOver = true;
            return;
        }

        Move move = new Move(dir,
                health,
                snakeDepthOccupied[nextHeadPos.x][nextHeadPos.y],
                food[nextHeadPos.x][nextHeadPos.y]);

        if (move.food) {
            health = 100;
            food[nextHeadPos.x][nextHeadPos.y] = false;
        } else {
            health--;
        }

        snakeDepthOccupied[nextHeadPos.x][nextHeadPos.y] = depth;
        headPos = nextHeadPos;

        moveStack.push(move);
        depth++;
    }

    public void backtrack() {
        Move lastMove = moveStack.pop();

        if (lastMove.food) {
            food[headPos.x][headPos.y] = true;
            length--;
        }

        health = lastMove.health;
        snakeDepthOccupied[headPos.x][headPos.y] = lastMove.snakeDepthOccupied;
        headPos = headPos.move(lastMove.dir.opposite());

        depth--;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private static class Move {
        Direction dir;
        int health;
        int snakeDepthOccupied;
        boolean food;
        public Move(Direction dir,
                    int health,
                    int snakeDepthOccupied,
                    boolean food) {
            this.dir = dir;
            this.health = health;
            this.snakeDepthOccupied = snakeDepthOccupied;
            this.food = food;
        }
    }
}
