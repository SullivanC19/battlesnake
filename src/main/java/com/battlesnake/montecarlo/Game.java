package com.battlesnake.montecarlo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class Game {
    private final int width, height;
    private final int numSnakes;

    private int[][] snakeOccupied;
    private int[][] moveOccupied;
    private boolean[][] food;

    private List<Position>[] body;
    private int[] health;
    private int[] length;

    private ArrayDeque<Move> moveStack;

    public Game(JsonNode moveRequestObj) {
        this.width = moveRequestObj.get("board").get("width").asInt();
        this.height = moveRequestObj.get("board").get("height").asInt();
        this.numSnakes = moveRequestObj.get("board").get("snakes").size();

        this.snakeOccupied = new int[width][height];
        this.moveOccupied = new int[width][height];
        this.food = new boolean[width][height];

        this.body = new List[numSnakes];
        this.health = new int[numSnakes];
        this.length = new int[numSnakes];

        initSnakes(moveRequestObj.get("board").get("snakes"));
        initFood(moveRequestObj.get("board").get("food"));

        this.moveStack = new ArrayDeque<>();
    }

    private void initSnakes(JsonNode snakesObj) {
        int maxDepth = -(width * height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++){
                snakeOccupied[x][y] = -1;
                moveOccupied[x][y] = maxDepth;
            }
        }

        int snakeIdx = 0;
        for (JsonNode snakeObj : snakesObj) {
            body[snakeIdx] = new ArrayList<>();
            health[snakeIdx] = snakeObj.get("health").asInt();
            length[snakeIdx] = snakeObj.get("length").asInt();

            int move = 0;
            for (JsonNode posObj : snakeObj.get("body")) {
                int x = posObj.get("x").asInt();
                int y = posObj.get("y").asInt();
                snakeOccupied[x][y] = snakeIdx;
                moveOccupied[x][y] = move--;
                body[snakeIdx].add(new Position(x, y));
            }

            snakeIdx++;
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
    public int getNumSnakes() { return numSnakes; }

    public boolean canMoveOnto(Position targetPos) {
        if (!targetPos.inBounds(width, height)) return false;
        int snakeIdx = snakeOccupied[targetPos.x][targetPos.y];
        return snakeIdx == -1
                || health[snakeIdx] <= 0
                || moveStack.size() - moveOccupied[targetPos.x][targetPos.y] >= length[snakeIdx] - 1;
    }
    public boolean isFood(Position pos) {
        return food[pos.x][pos.y];
    }

    public Position getHeadPos(int snakeIdx) {
        return body[snakeIdx].get(body[snakeIdx].size() - 1);
    }
    public Position getTailPos(int snakeIdx) {
        return body[snakeIdx].get(body[snakeIdx].size() - length[snakeIdx]);
    }
    public int getLength(int snakeIdx) {
        return length[snakeIdx];
    }
    public int getHealth(int snakeIdx) {
        return health[snakeIdx];
    }
    public boolean isAlive(int snakeIdx) { return health[snakeIdx] > 0; }

    public void step(Direction[] dir) {
        int[] prevHealth = new int[numSnakes];
        int[] prevSnakeOccupied = new int[numSnakes];
        int[] prevMoveOccupied = new int[numSnakes];
        boolean[] foodAcquired = new boolean[numSnakes];
        for (int snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
            if (health[snakeIdx] <= 0) continue;

            prevHealth[snakeIdx] = health[snakeIdx];

            Position headPos = getHeadPos(snakeIdx);
            prevSnakeOccupied[snakeIdx] = snakeOccupied[headPos.x][headPos.y];
            prevMoveOccupied[snakeIdx] = moveOccupied[headPos.x][headPos.y];

            Position nextHeadPos = getHeadPos().move(dir[snakeIdx]);
            body[snakeIdx].add(nextHeadPos);

            if (!nextHeadPos.inBounds(width, height) || !canMoveOnto(nextHeadPos)) {
                health[snakeIdx] = 0;
                continue;
            }

            // TODO: check the order in which food gathering, hunger, and collisions are resolved
            // TODO: may have to store collision locations to go back to or something

            foodAcquired[snakeIdx] = isFood(nextHeadPos);
            if (foodAcquired[snakeIdx]) {
                health[snakeIdx] = 100;
                length[snakeIdx]++;
                food[nextHeadPos.x][nextHeadPos.y] = false;
            } else {
                health[snakeIdx]--;
            }

            // head to head collision
            if (moveOccupied[nextHeadPos.x][nextHeadPos.y] == moveStack.size() + 1) {
                int collisionSnakeIdx = snakeOccupied[nextHeadPos.x][nextHeadPos.y];
                if (length[snakeIdx] <= length[collisionSnakeIdx]) health[snakeIdx] = 0;
                if (length[snakeIdx] >= length[collisionSnakeIdx]) health[collisionSnakeIdx] = 0;
            }

            snakeOccupied[nextHeadPos.x][nextHeadPos.y] = snakeIdx;
            moveOccupied[nextHeadPos.x][nextHeadPos.y] = moveStack.size() + 1;
        }

        Move move = new Move(
            dir,
            prevHealth,
            prevSnakeOccupied,
            prevMoveOccupied,
            foodAcquired
        );

        moveStack.push(move);
    }

    public void backtrack() {
        Move lastMove = moveStack.pop();
        for (int snakeIdx = numSnakes - 1; snakeIdx >= 0; snakeIdx--) {
            if (lastMove.prevHealth[snakeIdx] <= 0) continue;

            Position headPos = getHeadPos(snakeIdx);
            if (lastMove.foodAcquired[snakeIdx]) {
                food[headPos.x][headPos.y] = true;
                length[snakeIdx]--;
            }

            if (headPos.inBounds(width, height)) {
                snakeOccupied[headPos.x][headPos.y] = lastMove.prevSnakeOccupied[snakeIdx];
                moveOccupied[headPos.x][headPos.y] = lastMove.prevMoveOccupied[snakeIdx];
            }

            health[snakeIdx] = lastMove.prevHealth[snakeIdx];
            body[snakeIdx].remove(body[snakeIdx].size() - 1);
        }
    }

    public Position getHeadPos() {
        return getHeadPos(0);
    }
    public Position getTailPos() {
        return getTailPos(0);
    }
    public int getLength() {
        return getLength(0);
    }
    public int getHealth() {
        return getHealth(0);
    }

    // TODO make more efficient
    public int getNumLivingSnakes() {
        int numLivingSnakes = 0;
        for (int snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
            numLivingSnakes += isAlive(snakeIdx) ? 1 : 0;
        }
        return numLivingSnakes;
    }

    private static class Move {
        Direction[] dir;
        int[] prevHealth;
        int[] prevSnakeOccupied;
        int[] prevMoveOccupied;
        boolean[] foodAcquired;
        public Move(Direction[] dir,
                    int[] prevHealth,
                    int[] prevSnakeOccupied,
                    int[] prevMoveOccupied,
                    boolean[] foodAcquired) {
            this.dir = dir;
            this.prevHealth = prevHealth;
            this.prevSnakeOccupied = prevSnakeOccupied;
            this.prevMoveOccupied = prevMoveOccupied;
            this.foodAcquired = foodAcquired;
        }
    }
}
