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
    public boolean fillBoard(int[][] board) {
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
     * clues, ensuring the puzzle has a unique solution.
     */
    public void removeNumbers(int[][] board, int clues) {
        int cellsToRemove = GRID_SIZE * GRID_SIZE - clues;
        List<int[]> positions = new ArrayList<>();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                positions.add(new int[] { row, col });
            }
        }
        Collections.shuffle(positions, random);
        int removed = 0;
        for (int i = 0; i < positions.size() && removed < cellsToRemove; i++) {
            int[] pos = positions.get(i);
            int row = pos[0];
            int col = pos[1];
            int backup = board[row][col];
            board[row][col] = 0;
            // Check for unique solution
            if (countSolutions(copyBoard(board), 2) != 1) {
                board[row][col] = backup; // revert if not unique
            } else {
                removed++;
            }
        }
    }

    /**
     * Counts the number of solutions for a given board, up to a maximum.
     * Returns as soon as more than maxSolutions are found.
     */
    public int countSolutions(int[][] board, int maxSolutions) {
        return countSolutionsHelper(board, 0, 0, maxSolutions, new int[] { 0 });
    }

    private int countSolutionsHelper(int[][] board, int row, int col, int maxSolutions, int[] count) {
        if (row == GRID_SIZE) {
            count[0]++;
            return count[0];
        }
        if (count[0] > maxSolutions)
            return count[0];
        int nextRow = (col == GRID_SIZE - 1) ? row + 1 : row;
        int nextCol = (col == GRID_SIZE - 1) ? 0 : col + 1;
        if (board[row][col] != 0) {
            return countSolutionsHelper(board, nextRow, nextCol, maxSolutions, count);
        }
        for (int num = 1; num <= GRID_SIZE; num++) {
            if (isSafe(board, row, col, num)) {
                board[row][col] = num;
                countSolutionsHelper(board, nextRow, nextCol, maxSolutions, count);
                board[row][col] = 0;
                if (count[0] > maxSolutions)
                    return count[0];
            }
        }
        return count[0];
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
    public int[][] copyBoard(int[][] board) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }
}
