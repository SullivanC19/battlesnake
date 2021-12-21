package com.battlesnake.montecarlo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class Game {
    private final int width, height;
    private final int numSnakes;
    private final String mySnakeId;
    private int mySnakeIdx;

    private int[][] snakeOccupied;
    private int[][] moveOccupied;
    private boolean[][] food;

    private List<Position>[] body;
    private int[] health;
    private int[] length;
    private boolean[] alive;

    private ArrayDeque<Move> moveStack;

    public Game(JsonNode moveRequestObj) {
        this.width = moveRequestObj.get("board").get("width").asInt();
        this.height = moveRequestObj.get("board").get("height").asInt();
        this.numSnakes = moveRequestObj.get("board").get("snakes").size();
        this.mySnakeId = moveRequestObj.get("you").get("id").asText();
        this.mySnakeIdx = -1;

        this.snakeOccupied = new int[width][height];
        this.moveOccupied = new int[width][height];
        this.food = new boolean[width][height];

        this.body = new List[numSnakes];
        this.health = new int[numSnakes];
        this.length = new int[numSnakes];
        this.alive = new boolean[numSnakes];

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
            alive[snakeIdx] = true;

            String snakeId = snakeObj.get("id").asText();
            if (snakeId.equals(mySnakeId)) {
              this.mySnakeIdx = snakeIdx;
            }

            for (JsonNode posObj : snakeObj.get("body")) {
                int x = posObj.get("x").asInt();
                int y = posObj.get("y").asInt();
                body[snakeIdx].add(new Position(x, y));
            }
            Collections.reverse(body[snakeIdx]);

            int move = -body[snakeIdx].size();
            for (Position pos : body[snakeIdx]) {
                snakeOccupied[pos.x][pos.y] = snakeIdx;
                moveOccupied[pos.x][pos.y] = ++move;
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
                || !alive[snakeIdx]
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
    public boolean isAlive(int snakeIdx) { return alive[snakeIdx]; }

    public void step(Direction[] dir) {
        boolean[] collidedOrOob = new boolean[numSnakes];

        int[] prevHealth = new int[numSnakes];
        int[] prevSnakeOccupied = new int[numSnakes];
        int[] prevMoveOccupied = new int[numSnakes];
        boolean[] prevAlive = new boolean[numSnakes];
        boolean[] foodAcquired = new boolean[numSnakes];

        // move, check for body collissions or out-of-bounds, and reduce health
        for (int snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
            if (!alive[snakeIdx]) continue;
            
            prevAlive[snakeIdx] = alive[snakeIdx];
            prevHealth[snakeIdx] = health[snakeIdx];

            Position nextHeadPos = getHeadPos(snakeIdx).move(dir[snakeIdx]);
            body[snakeIdx].add(nextHeadPos);
            health[snakeIdx]--;

            if (canMoveOnto(nextHeadPos)) {
              prevSnakeOccupied[snakeIdx] = snakeOccupied[nextHeadPos.x][nextHeadPos.y];
              prevMoveOccupied[snakeIdx] = moveOccupied[nextHeadPos.x][nextHeadPos.y];
            } else {
                collidedOrOob[snakeIdx] = true;
                continue;
            }
        }

        // TODO make more efficient
        // head-to-head collisions
        for (int snakeIdx1 = 0; snakeIdx1 < numSnakes; snakeIdx1++) {
          if (!alive[snakeIdx1]) continue;
          for (int snakeIdx2 = snakeIdx1 + 1; snakeIdx2 < numSnakes; snakeIdx2++) {
            if (!alive[snakeIdx2]) continue;
            if (getHeadPos(snakeIdx1).equals(getHeadPos(snakeIdx2))) {
              if (length[snakeIdx1] <= length[snakeIdx2]) {
                collidedOrOob[snakeIdx1] = true;
              }
              if (length[snakeIdx2] <= length[snakeIdx1]) {
                collidedOrOob[snakeIdx2] = true;
              }
            }
          }
        }

        // food and update grids
        for (int snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
          if (!alive[snakeIdx] || collidedOrOob[snakeIdx]) continue;
          Position nextHeadPos = getHeadPos(snakeIdx);

          foodAcquired[snakeIdx] = isFood(nextHeadPos);
          if (foodAcquired[snakeIdx]) {
              health[snakeIdx] = 100;
              length[snakeIdx]++;
              food[nextHeadPos.x][nextHeadPos.y] = false;
          } else {
              health[snakeIdx]--;
          }

          snakeOccupied[nextHeadPos.x][nextHeadPos.y] = snakeIdx;
          moveOccupied[nextHeadPos.x][nextHeadPos.y] = moveStack.size() + 1;
        }

        // remove dead snakes
        for (int snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
          if (collidedOrOob[snakeIdx] || health[snakeIdx] == 0) {
            alive[snakeIdx] = false;
          }
        }

        Move move = new Move(
            dir,
            prevHealth,
            prevSnakeOccupied,
            prevMoveOccupied,
            prevAlive,
            foodAcquired
        );

        moveStack.push(move);
    }

    public void backtrack() {
        Move lastMove = moveStack.pop();
        for (int snakeIdx = numSnakes - 1; snakeIdx >= 0; snakeIdx--) {
            if (!lastMove.prevAlive[snakeIdx]) continue;

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
            alive[snakeIdx] = lastMove.prevAlive[snakeIdx];
            body[snakeIdx].remove(body[snakeIdx].size() - 1);
        }
    }

    public int getMySnakeIdx() {
      return mySnakeIdx;
    }
    public Position getHeadPos() {
        return getHeadPos(mySnakeIdx);
    }
    public Position getTailPos() {
        return getTailPos(mySnakeIdx);
    }
    public int getLength() {
        return getLength(mySnakeIdx);
    }
    public int getHealth() {
        return getHealth(mySnakeIdx);
    }
    public boolean isOver() {
        return getNumLivingSnakes() <= 1;
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
        boolean[] prevAlive;
        boolean[] foodAcquired;
        public Move(Direction[] dir,
                    int[] prevHealth,
                    int[] prevSnakeOccupied,
                    int[] prevMoveOccupied,
                    boolean[] prevAlive,
                    boolean[] foodAcquired) {
            this.dir = dir;
            this.prevHealth = prevHealth;
            this.prevSnakeOccupied = prevSnakeOccupied;
            this.prevMoveOccupied = prevMoveOccupied;
            this.prevAlive = prevAlive;
            this.foodAcquired = foodAcquired;
        }
    }
}
