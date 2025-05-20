package com.em.multiplayersudoku;

import java.util.Map;

public class BoardsListMessage {
    private Map<String, Cell[][]> boards; // sessionId -> board
    private int playerCount;
    private Map<String, Integer> filledCounts; // sessionId -> filled cell count
    private Map<String, Integer> stepsAhead; // sessionId -> steps ahead (difference to each other)
    private boolean canRemoveOpponentCell;
    private long removeCooldownUntil; // epoch millis, 0 if available
    private Map<String, Boolean> canRemoveOpponentCellMap;
    private Map<String, Long> removeCooldownUntilMap;
    private int maxStepGap;
    private int cooldownSeconds;
    private String difficulty;

    public BoardsListMessage(Map<String, Cell[][]> boards, int playerCount, Map<String, Integer> filledCounts,
            Map<String, Integer> stepsAhead) {
        this.boards = boards;
        this.playerCount = playerCount;
        this.filledCounts = filledCounts;
        this.stepsAhead = stepsAhead;
    }

    public Map<String, Cell[][]> getBoards() {
        return boards;
    }

    public void setBoards(Map<String, Cell[][]> boards) {
        this.boards = boards;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public Map<String, Integer> getFilledCounts() {
        return filledCounts;
    }

    public void setFilledCounts(Map<String, Integer> filledCounts) {
        this.filledCounts = filledCounts;
    }

    public Map<String, Integer> getStepsAhead() {
        return stepsAhead;
    }

    public void setStepsAhead(Map<String, Integer> stepsAhead) {
        this.stepsAhead = stepsAhead;
    }

    public boolean isCanRemoveOpponentCell() {
        return canRemoveOpponentCell;
    }

    public void setCanRemoveOpponentCell(boolean canRemoveOpponentCell) {
        this.canRemoveOpponentCell = canRemoveOpponentCell;
    }

    public long getRemoveCooldownUntil() {
        return removeCooldownUntil;
    }

    public void setRemoveCooldownUntil(long removeCooldownUntil) {
        this.removeCooldownUntil = removeCooldownUntil;
    }

    public Map<String, Boolean> getCanRemoveOpponentCellMap() {
        return canRemoveOpponentCellMap;
    }

    public void setCanRemoveOpponentCellMap(Map<String, Boolean> map) {
        this.canRemoveOpponentCellMap = map;
    }

    public Map<String, Long> getRemoveCooldownUntilMap() {
        return removeCooldownUntilMap;
    }

    public void setRemoveCooldownUntilMap(Map<String, Long> map) {
        this.removeCooldownUntilMap = map;
    }

    public int getMaxStepGap() {
        return maxStepGap;
    }

    public void setMaxStepGap(int maxStepGap) {
        this.maxStepGap = maxStepGap;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
