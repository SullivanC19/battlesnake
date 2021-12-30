package com.battlesnake.minmax;

public interface AbstractDirection {
    public String getMove();
    public byte xDiff();
    public byte yDiff();
    public Direction opposite();
    public int index();
}

enum Direction implements AbstractDirection {
    UP {
        public String getMove() { return "up"; }
        public byte xDiff() { return 0; }
        public byte yDiff() { return 1; }
        public Direction opposite() { return DOWN; }
        public int index() { return 0; }
    },
    DOWN {
        public String getMove() { return "down"; }
        public byte xDiff() { return 0; }
        public byte yDiff() { return -1; }
        public Direction opposite() { return UP; }
        public int index() { return 1; }
    },
    LEFT {
        public String getMove() { return "left"; }
        public byte xDiff() { return -1; }
        public byte yDiff() { return 0; }
        public Direction opposite() { return RIGHT; }
        public int index() { return 2; }
    },
    RIGHT {
        public String getMove() { return "right"; }
        public byte xDiff() { return 1; }
        public byte yDiff() { return 0; }
        public Direction opposite() { return LEFT; }
        public int index() { return 3; }
    },
}
