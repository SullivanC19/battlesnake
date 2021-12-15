package com.battlesnake.montecarlo;

import com.fasterxml.jackson.databind.JsonNode;

public class MoveRequest {
    private final int turn;
    private Game game;

    public MoveRequest(JsonNode moveRequestObj) {
        this.turn = moveRequestObj.get("turn").asInt();
        this.game = new Game(moveRequestObj);
    }

    public int getTurn() {
        return turn;
    }

    public Game getGame() {
        return game;
    }
}