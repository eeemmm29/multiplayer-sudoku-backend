package com.em.multiplayersudoku.domain;

/**
 * Predefined Sudoku difficulty levels.
 */
public enum Difficulty {
    EASY(35),
    MEDIUM(30),
    HARD(25),
    EXPERT(20);

    private final int minClues;

    Difficulty(int minClues) {
        this.minClues = minClues;
    }

    public int getMinClues() {
        return minClues;
    }
}
