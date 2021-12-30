package com.battlesnake.minmax;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class Game {
    public static final byte MAX_NUM_SNAKES = 2;
    public static final int MAX_BOARD_SIZE = 30;

    public static final byte GAME_STATE_IN_PROGRESS = 0;
    public static final byte GAME_STATE_OVER_WIN = 1;
    public static final byte GAME_STATE_OVER_DRAW = 2;
    public static final byte GAME_STATE_OVER_LOSS = 3;

    private final int width, height;
    private final int numSnakes;
    private final String mySnakeId;

    private final boolean[][] snake;
    private final boolean[][] food;
    private final List<State> states;

    public Game(JsonNode moveRequestObj) {
        width = moveRequestObj.get("board").get("width").asInt();
        height = moveRequestObj.get("board").get("height").asInt();
        numSnakes = moveRequestObj.get("board").get("snakes").size();
        mySnakeId = moveRequestObj.get("you").get("id").asText();

        snake = new boolean[width][height];
        food = new boolean[width][height];
        states = new ArrayList<>();

        initSnakes(moveRequestObj.get("board").get("snakes"));
        initFood(moveRequestObj.get("board").get("food"));
        initStates(moveRequestObj);
    }

    private void initSnakes(JsonNode snakesObj) {
        for (JsonNode snakeObj : snakesObj) {
            for (JsonNode posObj : snakeObj.get("body")) {
                Position pos = Position.fromJsonObj(posObj);
                snake[pos.x][pos.y] = true;
            }
        }
    }

    private void initFood(JsonNode foodObj) {
        for (JsonNode posObj : foodObj) {
            Position pos = Position.fromJsonObj(posObj);
            food[pos.x][pos.y] = true;
        }
    }

    private void initStates(JsonNode moveRequestObj) {
        int curTurn = moveRequestObj.get("turn").asInt();
        JsonNode snakesObj = moveRequestObj.get("board").get("snakes");

        // set current state's length and health
        State curState = new State(curTurn);
        for (JsonNode snakeObj : snakesObj) {
            int snakeIdx = snakeObj.get("id").asText().equals(mySnakeId) ? 0 : 1;
            curState.length[snakeIdx] = snakeObj.get("length").asInt();
            curState.health[snakeIdx] = snakeObj.get("health").asInt();
        }

        states.add(curState);

        // set states' head positions
        for (JsonNode snakeObj : snakesObj) {
            int snakeIdx = snakeObj.get("id").asText().equals(mySnakeId) ? 0 : 1;
            int turn = curTurn;
            for (JsonNode posObj : snakeObj.get("body")) {
                Position pos = Position.fromJsonObj(posObj);
                if (curTurn - turn >= states.size()) states.add(new State(turn));
                states.get(curTurn - turn).head[snakeIdx] = pos;
                turn--;
            }
        }

        // put current state at end of list
        Collections.reverse(states);
    }

    private State getCurrentState(boolean remove) {
        return remove ? states.remove(states.size() - 1) : states.get(states.size() - 1);
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int getNumSnakes() {
        return numSnakes;
    }

    public byte getCurrentGameState() {
        return getCurrentState(false).getGameState();
    }
    public Position getHeadPos(int snakeIdx) {
        return getCurrentState(false).head[snakeIdx];
    }
    public Position getTailPos(int snakeIdx) {
        int length = getCurrentState(false).length[snakeIdx];
        return states.get(states.size() - length).head[snakeIdx];
    }
    public boolean canMoveOnto(Position pos) {
        return pos.inBounds(width, height) && !snake[pos.x][pos.y];
    }

    public void step(Direction[] dir) {
        State state = getCurrentState(false);
        State nextState = new State(state.turn + 1);

        if (state.getGameState() == GAME_STATE_IN_PROGRESS) {
            nextState.head[0] = state.head[0].move(dir[0]);
            nextState.head[1] = state.head[1].move(dir[1]);

            boolean[] alive = new boolean[2];
            alive[0] = alive[1] = true;

            nextState.health[0] = 100;
            nextState.health[1] = 100;

            // head-to-head collision
            if (nextState.head[0].equals(nextState.head[1])) {
                alive[0] = state.length[0] > state.length[1];
                alive[1] = state.length[1] > state.length[0];
            }

            // in bounds
            alive[0] &= nextState.head[0].inBounds(width, height);
            alive[1] &= nextState.head[1].inBounds(width, height);

            if (alive[0] && alive[1]) {
                Position head0 = nextState.head[0];
                Position head1 = nextState.head[1];

                // food eaten
                nextState.foodEaten[0] = food[head0.x][head0.y];
                nextState.foodEaten[1] = food[head1.x][head1.y];
                food[head0.x][head0.y] = false;
                food[head1.x][head1.y] = false;

                // health update
                nextState.health[0] = nextState.foodEaten[0] ? 100 : state.health[0] - 1;
                nextState.health[1] = nextState.foodEaten[1] ? 100 : state.health[1] - 1;

                // length update
                nextState.length[0] = state.length[0] + (nextState.foodEaten[0] ? 0 : 1);
                nextState.length[1] = state.length[1] + (nextState.foodEaten[1] ? 0 : 1);

                // remove tail (if food not eaten)
                Position tail0 = getTailPos(0);
                Position tail1 = getTailPos(1);
                snake[tail0.x][tail1.y] = nextState.foodEaten[0] || state.turn < 2;
                snake[tail0.x][tail1.y] = nextState.foodEaten[1] || state.turn < 2;

                // collision
                alive[0] = !snake[head0.x][head0.y];
                alive[1] = !snake[head1.x][head1.y];

                // add new head
                snake[head0.x][head0.y] = true;
                snake[head1.x][head1.y] = true;
            }

            if (!alive[0]) nextState.health[0] = 0;
            if (!alive[1]) nextState.health[1] = 0;
        }

        states.add(nextState);
    }

    public void backtrack() {
        State state = getCurrentState(true);
        if (!state.head[0].equals(state.head[1])
                && state.head[0].inBounds(width, height)
                && state.head[1].inBounds(width, height)) {
            Position head0 = state.head[0];
            Position head1 = state.head[1];

            // remove head
            snake[head0.x][head0.y] = false;
            snake[head1.x][head1.y] = false;

            // add old tail
            Position tail0 = getTailPos(0);
            Position tail1 = getTailPos(1);
            snake[tail0.x][tail1.y] = true;
            snake[tail0.x][tail1.y] = true;

            // food regurgitated
            food[head0.x][head0.y] = state.foodEaten[0];
            food[head1.x][head1.y] = state.foodEaten[1];
        }
    }

    private static class State {
        int turn;

        Position[] head;
        int[] length;
        int[] health;
        boolean[] foodEaten;

        public State(int turn) {
            this.turn = turn;

            this.head = new Position[MAX_NUM_SNAKES];
            this.length = new int[MAX_NUM_SNAKES];
            this.health = new int[MAX_NUM_SNAKES];
            this.foodEaten = new boolean[2];
        }

        public byte getGameState() {
            if (health[0] <= 0 && health[1] <= 0) return GAME_STATE_OVER_DRAW;
            if (health[0] <= 0) return GAME_STATE_OVER_LOSS;
            if (health[1] <= 0) return GAME_STATE_OVER_WIN;
            return GAME_STATE_IN_PROGRESS;
        }
    }
}
