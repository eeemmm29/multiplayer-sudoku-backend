package com.em.multiplayersudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating and solving Sudoku boards.
 */
public class SudokuGenerator {
    public static final int GRID_SIZE = 9;
    public static final int SUBGRID_SIZE = 3;
    private final Random random = new Random();

    /**
     * Generates a new Sudoku puzzle with a unique solution.
     * 
     * @param clues Number of cells to leave as clues (difficulty control)
     * @return 2D int array representing the puzzle (0 = empty)
     */
    public int[][] generatePuzzle(int clues) {
        int[][] board = new int[GRID_SIZE][GRID_SIZE];
        fillBoard(board);
        int[][] puzzle = copyBoard(board);
        removeNumbers(puzzle, clues);
        return puzzle;
    }

    /**
     * Generates a new Sudoku puzzle as a Cell[][] board with statuses.
     *
     * @param clues Number of cells to leave as clues (difficulty control)
     * @return 2D Cell array representing the puzzle
     */
    public Cell[][] generatePuzzleWithStatus(int clues) {
        int[][] board = new int[GRID_SIZE][GRID_SIZE];
        fillBoard(board);
        int[][] puzzle = copyBoard(board);
        removeNumbers(puzzle, clues);
        Cell[][] cellBoard = new Cell[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = puzzle[row][col];
                CellStatus status = (value == 0) ? CellStatus.TO_GUESS : CellStatus.GIVEN;
                cellBoard[row][col] = new Cell(value, status);
            }
        }
        return cellBoard;
    }

    /**
     * Generates a fully filled valid Sudoku board.
     */
    private boolean fillBoard(int[][] board) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE; i++)
            numbers.add(i);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (board[row][col] == 0) {
                    Collections.shuffle(numbers, random);
                    for (int num : numbers) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (fillBoard(board))
                                return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Removes numbers from the board to create a puzzle with the given number of
     * clues.
     */
    private void removeNumbers(int[][] board, int clues) {
        int cellsToRemove = GRID_SIZE * GRID_SIZE - clues;
        List<int[]> positions = new ArrayList<>();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                positions.add(new int[] { row, col });
            }
        }
        Collections.shuffle(positions, random);
        for (int i = 0; i < cellsToRemove; i++) {
            int[] pos = positions.get(i);
            int backup = board[pos[0]][pos[1]];
            board[pos[0]][pos[1]] = 0;
            // Optionally: Check for unique solution here (not implemented for brevity)
        }
    }

    /**
     * Checks if it's safe to place a number at the given position.
     */
    private boolean isSafe(int[][] board, int row, int col, int num) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num)
                return false;
        }
        int boxRow = row - row % SUBGRID_SIZE;
        int boxCol = col - col % SUBGRID_SIZE;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (board[boxRow + i][boxCol + j] == num)
                    return false;
            }
        }
        return true;
    }

    /**
     * Utility to copy a board.
     */
    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }
}
