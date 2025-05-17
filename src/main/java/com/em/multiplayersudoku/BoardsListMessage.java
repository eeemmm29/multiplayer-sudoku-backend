package com.em.multiplayersudoku;

import java.util.Map;

public class BoardsListMessage {
    private Map<String, Cell[][]> boards; // sessionId -> board
    private int playerCount;

    public BoardsListMessage(Map<String, Cell[][]> boards, int playerCount) {
        this.boards = boards;
        this.playerCount = playerCount;
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
}
