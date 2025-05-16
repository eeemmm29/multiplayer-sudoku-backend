package com.em.multiplayersudoku;

import java.io.Serializable;

/**
 * Message containing both the player's and the opponent's Sudoku boards.
 */
public class BoardsMessage implements Serializable {
    private Cell[][] myBoard;
    private Cell[][] opponentBoard;
    private int playerCount;

    public BoardsMessage(Cell[][] myBoard, Cell[][] opponentBoard, int playerCount) {
        this.myBoard = myBoard;
        this.opponentBoard = opponentBoard;
        this.playerCount = playerCount;
    }

    public BoardsMessage(Cell[][] myBoard, Cell[][] opponentBoard) {
        this(myBoard, opponentBoard, 1);
    }

    public Cell[][] getMyBoard() {
        return myBoard;
    }

    public void setMyBoard(Cell[][] myBoard) {
        this.myBoard = myBoard;
    }

    public Cell[][] getOpponentBoard() {
        return opponentBoard;
    }

    public void setOpponentBoard(Cell[][] opponentBoard) {
        this.opponentBoard = opponentBoard;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
}
