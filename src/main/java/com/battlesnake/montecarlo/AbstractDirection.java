package com.battlesnake.montecarlo;

public interface AbstractDirection {
    public String getMove();
    public int xDiff();
    public int yDiff();
    public Direction opposite();
}

enum Direction implements AbstractDirection {
    UP {
        public String getMove() { return "up"; }
        public int xDiff() { return 0; }
        public int yDiff() { return 1; }
        public Direction opposite() { return DOWN; }
    },
    DOWN {
        public String getMove() { return "down"; }
        public int xDiff() { return 0; }
        public int yDiff() { return -1; }
        public Direction opposite() { return UP; }
    },
    LEFT {
        public String getMove() { return "left"; }
        public int xDiff() { return -1; }
        public int yDiff() { return 0; }
        public Direction opposite() { return RIGHT; }
    },
    RIGHT {
        public String getMove() { return "right"; }
        public int xDiff() { return 1; }
        public int yDiff() { return 0; }
        public Direction opposite() { return LEFT; }
    },
}
