package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class Game {
    public static final byte MAX_NUM_SNAKES = 4;

    private static short width, height;
    private static byte numSnakes;
    private static byte numSnakesAlive;

    private static String mySnakeId;
    private static byte mySnakeIdx;
    private static Map<String, Byte> snakeIdToIdxMap;

    private static byte[][] snakeOccupied;
    private static short[][] moveOccupied;
    private static boolean[][] food;

    private static List<Position>[] body;
    private static byte[] health;
    private static short[] length;
    private static boolean[] alive;

    private static ArrayDeque<Move> moveStack;

    private static long startTime;

    public static void init(JsonNode startRequestObj) {
        width = (short) startRequestObj.get("board").get("width").asInt();
        height = (short) startRequestObj.get("board").get("height").asInt();
        numSnakes = numSnakesAlive = (byte) startRequestObj.get("board").get("snakes").size();
        mySnakeId = startRequestObj.get("you").get("id").asText();
        snakeIdToIdxMap = new HashMap<>(MAX_NUM_SNAKES);

        snakeOccupied = new byte[width][height];
        moveOccupied = new short[width][height];
        food = new boolean[width][height];

        body = new List[numSnakes];
        health = new byte[numSnakes];
        length = new short[numSnakes];
        alive = new boolean[numSnakes];

        initSnakes(startRequestObj.get("board").get("snakes"));
        addFood(startRequestObj.get("board").get("food"));

        moveStack = new ArrayDeque<>();
    }

    public static void update(JsonNode moveRequestObj) {
        JsonNode snakesObj = moveRequestObj.get("board").get("snakes");
        Direction[] dir = new Direction[Game.getNumSnakes()];
        boolean allNull = true;
        for (JsonNode snakeObj : snakesObj) {
            byte snakeIdx = snakeIdToIdxMap.get(snakeObj.get("id").asText());
            Position headPos = Position.fromJsonObj(snakeObj.get("body").get(0));
            Position lastHeadPos = Game.getHeadPos(snakeIdx);
            dir[snakeIdx] = lastHeadPos.directionTo(headPos);
            allNull &= dir[snakeIdx] == null;
        }

        if (!allNull) {
          Game.step(dir);
          addFood(moveRequestObj.get("board").get("food"));
        }
    }

    private static void initSnakes(JsonNode snakesObj) {
        short maxDepth = (short) (-width * height);
        for (short x = 0; x < width; x++) {
            for (short y = 0; y < height; y++){
                snakeOccupied[x][y] = -1;
                moveOccupied[x][y] = maxDepth;
            }
        }

        byte snakeIdx = 0;
        for (JsonNode snakeObj : snakesObj) {
            body[snakeIdx] = new ArrayList<>();
            health[snakeIdx] = (byte) snakeObj.get("health").asInt();
            length[snakeIdx] = (short) snakeObj.get("length").asInt();
            alive[snakeIdx] = true;

            String snakeId = snakeObj.get("id").asText();
            if (snakeId.equals(mySnakeId)) {
              mySnakeIdx = snakeIdx;
            }

            snakeIdToIdxMap.put(snakeId, snakeIdx);

            for (JsonNode posObj : snakeObj.get("body")) {
                body[snakeIdx].add(Position.fromJsonObj(posObj));
            }
            Collections.reverse(body[snakeIdx]);

            short move = (short) -body[snakeIdx].size();
            for (Position pos : body[snakeIdx]) {
                snakeOccupied[pos.x][pos.y] = snakeIdx;
                moveOccupied[pos.x][pos.y] = ++move;
            }

            snakeIdx++;
        }
    }

    private static void addFood(JsonNode foodObj) {
        for (JsonNode posObj : foodObj) {
            int x = posObj.get("x").asInt();
            int y = posObj.get("y").asInt();
            food[x][y] = true;
        }
    }

    public static void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public static long getTimeElapsed() {
        return System.currentTimeMillis() - startTime;
    }

    public static short getWidth() {
        return width;
    }
    public static short getHeight() {
        return height;
    }
    public static byte getNumSnakes() { return numSnakes; }

    public static boolean inBounds(Position pos) {
        return pos.inBounds(width, height);
    }
    public static boolean onEdge(Position pos) {
        return pos.x == 0 || pos.x == width - 1 || pos.y == 0 || pos.y == height - 1;
    }
    public static boolean canMoveOnto(Position targetPos, int turnsElapsed) {
        if (!inBounds(targetPos)) return false;
        byte snakeIdx = snakeOccupied[targetPos.x][targetPos.y];
        return snakeIdx == -1
                || !alive[snakeIdx]
                || turnsElapsed + moveStack.size() - moveOccupied[targetPos.x][targetPos.y] >= length[snakeIdx] - 1;
    }
    public static boolean canMoveOnto(Position targetPos) {
        return canMoveOnto(targetPos, 0);
    }
    public static boolean isFood(Position pos) {
        return food[pos.x][pos.y];
    }

    public static Position getHeadPos(byte snakeIdx) {
        return body[snakeIdx].get(body[snakeIdx].size() - 1);
    }
    public static Position getTailPos(byte snakeIdx) {
        return body[snakeIdx].get(body[snakeIdx].size() - length[snakeIdx]);
    }
    public static short getLength(byte snakeIdx) {
        return length[snakeIdx];
    }
    public static byte getHealth(byte snakeIdx) {
        return health[snakeIdx];
    }
    public static boolean isAlive(byte snakeIdx) {
        return alive[snakeIdx];
    }

    public static void step(Direction[] dir) {
        boolean[] collidedOrOob = new boolean[numSnakes];

        byte[] prevHealth = new byte[numSnakes];
        byte[] prevSnakeOccupied = new byte[numSnakes];
        short[] prevMoveOccupied = new short[numSnakes];
        boolean[] prevAlive = new boolean[numSnakes];
        boolean[] foodAcquired = new boolean[numSnakes];

        // move, check for body collisions or out-of-bounds, and reduce health
        for (byte snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
            if (!alive[snakeIdx] || dir[snakeIdx] == null) continue;
            
            prevAlive[snakeIdx] = alive[snakeIdx];
            prevHealth[snakeIdx] = health[snakeIdx];

            Position nextHeadPos = getHeadPos(snakeIdx).move(dir[snakeIdx]);
            body[snakeIdx].add(nextHeadPos);
            health[snakeIdx]--;

            // check for oob
            if (!nextHeadPos.inBounds(width, height)) {
              collidedOrOob[snakeIdx] = true;
              continue;
            }

            prevSnakeOccupied[snakeIdx] = snakeOccupied[nextHeadPos.x][nextHeadPos.y];
            prevMoveOccupied[snakeIdx] = moveOccupied[nextHeadPos.x][nextHeadPos.y];

            // check for collision
            if (!canMoveOnto(nextHeadPos)) {
              collidedOrOob[snakeIdx] = true;

              // head-to-head collision
              if (moveOccupied[nextHeadPos.x][nextHeadPos.y] == moveStack.size() + 1) {
                  byte collidedSnakeIdx = snakeOccupied[nextHeadPos.x][nextHeadPos.y];

                  collidedOrOob[collidedSnakeIdx] = length[collidedSnakeIdx] <= length[snakeIdx];
                  collidedOrOob[snakeIdx] = length[snakeIdx] <= length[collidedSnakeIdx];

                  // pull values from already moved snake
                  prevSnakeOccupied[snakeIdx] = prevSnakeOccupied[collidedSnakeIdx];
                  prevMoveOccupied[snakeIdx] = prevMoveOccupied[collidedSnakeIdx];
              }
            } else {
              snakeOccupied[nextHeadPos.x][nextHeadPos.y] = snakeIdx;
              moveOccupied[nextHeadPos.x][nextHeadPos.y] = (short) (moveStack.size() + 1);
            }
        }

        // food and update grids
        for (byte snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
          if (!alive[snakeIdx] || collidedOrOob[snakeIdx]) continue;

          Position nextHeadPos = getHeadPos(snakeIdx);
          foodAcquired[snakeIdx] = isFood(nextHeadPos);
          if (foodAcquired[snakeIdx]) {
              health[snakeIdx] = 100;
              length[snakeIdx]++;
              food[nextHeadPos.x][nextHeadPos.y] = false;
          }
        }

        // remove dead snakes
        for (byte snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
          if (alive[snakeIdx] && (collidedOrOob[snakeIdx] || health[snakeIdx] == 0)) {
            alive[snakeIdx] = false;
            numSnakesAlive--;
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

    public static void backtrack() {
        Move lastMove = moveStack.pop();
        for (byte snakeIdx = 0; snakeIdx < numSnakes; snakeIdx++) {
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

            if (lastMove.prevAlive[snakeIdx] && !alive[snakeIdx]) {
                numSnakesAlive++;
            }

            health[snakeIdx] = lastMove.prevHealth[snakeIdx];
            alive[snakeIdx] = lastMove.prevAlive[snakeIdx];
            body[snakeIdx].remove(body[snakeIdx].size() - 1);
        }
    }

    public static byte getMySnakeIdx() {
      return mySnakeIdx;
    }
    public static Position getHeadPos() {
        return getHeadPos(mySnakeIdx);
    }
    public static Position getTailPos() {
        return getTailPos(mySnakeIdx);
    }
    public static short getLength() {
        return getLength(mySnakeIdx);
    }
    public static byte getHealth() {
        return getHealth(mySnakeIdx);
    }
    public static boolean isOver() {
        return getNumLivingSnakes() <= (numSnakes == 1 ? 0 : 1);
    }

    public static byte getNumLivingSnakes() {
        return numSnakesAlive;
    }

    private static class Move {
        Direction[] dir;
        byte[] prevHealth;
        byte[] prevSnakeOccupied;
        short[] prevMoveOccupied;
        boolean[] prevAlive;
        boolean[] foodAcquired;
        public Move(Direction[] dir,
                    byte[] prevHealth,
                    byte[] prevSnakeOccupied,
                    short[] prevMoveOccupied,
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
