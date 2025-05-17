package com.em.multiplayersudoku;

/**
 * Represents a cell in the Sudoku board, holding its value and status.
 */
public class Cell {
    private int value; // 1-9, or 0 for empty
    private CellStatus status;
    private long cooldownUntil = 0; // epoch millis

    public Cell(int value, CellStatus status) {
        this.value = value;
        this.status = status;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public CellStatus getStatus() {
        return status;
    }

    public void setStatus(CellStatus status) {
        this.status = status;
    }

    public long getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(long cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }
}
