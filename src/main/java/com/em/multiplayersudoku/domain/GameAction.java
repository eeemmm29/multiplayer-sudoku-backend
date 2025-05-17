package com.em.multiplayersudoku.domain;

import java.time.Instant;

/**
 * Represents a single player action in a multiplayer Sudoku game.
 */
public class GameAction {
    public enum ActionType {
        FILL, // place a number in a cell
        REMOVE, // remove one number from opponent’s grid
        JOIN, // player joins room
        LEAVE, // player leaves room
        WIN, // game won
        HEARTBEAT // keepalive/ping
    }

    private ActionType type; // what kind of action
    private int row; // 0–8 for Sudoku grid
    private int col; // 0–8 for Sudoku grid
    private int value; // 1–9 for fill/remove
    private Instant timestamp; // when the action was taken
    private String sessionId; // The player who performed the action (for WIN, the winner)

    // Used when creating a new room.
    private Difficulty difficulty; // Game difficulty.
    private int cooldown; // seconds until the remove-number power-up can be used again
    private int maxStepGap; // max number of steps between two players before a timeout

    public GameAction() {
        /* for deserialization */ }

    public GameAction(String sessionId, ActionType type, int row, int col, int value) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.value = value;
        this.timestamp = Instant.now();
        this.sessionId = sessionId;
    }

    // Getters and setters

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getMaxStepGap() {
        return maxStepGap;
    }

    public void setMaxStepGap(int maxStepGap) {
        this.maxStepGap = maxStepGap;
    }

}