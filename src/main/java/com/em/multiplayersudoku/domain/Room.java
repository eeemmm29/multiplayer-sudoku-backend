package com.em.multiplayersudoku.domain;

import com.em.multiplayersudoku.Cell;
import com.em.multiplayersudoku.CellStatus;
import com.em.multiplayersudoku.SudokuGenerator;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String code;
    private final Difficulty difficulty;
    private final int removeThreshold;
    private final int cooldownSeconds;

    // session IDs of the two players
    private final Set<String> players = ConcurrentHashMap.newKeySet();

    // track last-used timestamps for power-ups per session
    private final ConcurrentHashMap<String, Instant> lastRemoveUsed = new ConcurrentHashMap<>();

    // Add a field for the puzzle board (Cell[][]) for each player
    private final ConcurrentHashMap<String, Cell[][]> playerBoards = new ConcurrentHashMap<>();

    // Add a field for the solution board (int[][]) for each player
    private final ConcurrentHashMap<String, int[][]> playerSolutions = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, long[][]> cellCooldowns = new ConcurrentHashMap<>();
    private static final int CELL_COOLDOWN_SECONDS = 3;

    public Room(String code, Difficulty difficulty, int removeThreshold, int cooldownSeconds) {
        this.code = code;
        this.difficulty = difficulty;
        this.removeThreshold = removeThreshold;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getCode() {
        return code;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getRemoveThreshold() {
        return removeThreshold;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public Set<String> getPlayers() {
        return players;
    }

    public void addPlayer(String sessionId) {
        if (!players.contains(sessionId) && players.size() < 2) {
            players.add(sessionId);
        }
    }

    public void removePlayer(String sessionId) {
        players.remove(sessionId);
        lastRemoveUsed.remove(sessionId);
        playerBoards.remove(sessionId);
        playerSolutions.remove(sessionId);
        cellCooldowns.remove(sessionId);
    }

    public boolean canUseRemove(String sessionId) {
        Instant last = lastRemoveUsed.get(sessionId);
        return last == null || Instant.now().isAfter(last.plusSeconds(cooldownSeconds));
    }

    public void recordRemoveUse(String sessionId) {
        lastRemoveUsed.put(sessionId, Instant.now());
    }

    // Add a method to initialize a board for a player
    public void initializeBoardForPlayer(String sessionId, int clues) {
        SudokuGenerator generator = new SudokuGenerator();
        int[][] solution = new int[SudokuGenerator.GRID_SIZE][SudokuGenerator.GRID_SIZE];
        generator.fillBoard(solution); // generate a full solution
        int[][] puzzle = generator.copyBoard(solution);
        generator.removeNumbers(puzzle, clues);
        Cell[][] cellBoard = new Cell[SudokuGenerator.GRID_SIZE][SudokuGenerator.GRID_SIZE];
        for (int row = 0; row < SudokuGenerator.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuGenerator.GRID_SIZE; col++) {
                int value = puzzle[row][col];
                CellStatus status = (value == 0) ? CellStatus.TO_GUESS : CellStatus.GIVEN;
                cellBoard[row][col] = new Cell(value, status);
            }
        }
        playerBoards.put(sessionId, cellBoard);
        playerSolutions.put(sessionId, solution);
        cellCooldowns.put(sessionId, new long[SudokuGenerator.GRID_SIZE][SudokuGenerator.GRID_SIZE]);
    }

    public boolean isCellOnCooldown(String sessionId, int row, int col) {
        long[][] cooldowns = cellCooldowns.get(sessionId);
        return cooldowns != null && System.currentTimeMillis() < cooldowns[row][col];
    }

    public void setCellCooldown(String sessionId, int row, int col) {
        long[][] cooldowns = cellCooldowns.get(sessionId);
        if (cooldowns != null) {
            cooldowns[row][col] = System.currentTimeMillis() + CELL_COOLDOWN_SECONDS * 1000;
        }
    }

    public long getCellCooldownUntil(String sessionId, int row, int col) {
        long[][] cooldowns = cellCooldowns.get(sessionId);
        return (cooldowns != null) ? cooldowns[row][col] : 0;
    }

    public Cell[][] getBoardForPlayer(String sessionId) {
        return playerBoards.get(sessionId);
    }

    // Optionally, add a method to update a cell for a player
    public void updateCellForPlayer(String sessionId, int row, int col, int value) {
        Cell[][] board = playerBoards.get(sessionId);
        int[][] solution = playerSolutions.get(sessionId);
        if (board != null && solution != null) {
            board[row][col].setValue(value);
            if (value == 0) {
                board[row][col].setStatus(CellStatus.TO_GUESS);
            } else if (value == solution[row][col]) {
                board[row][col].setStatus(CellStatus.CORRECT_GUESS);
            } else {
                board[row][col].setStatus(CellStatus.WRONG_GUESS);
            }
        }
    }

    public boolean isPlayerBoardComplete(String sessionId) {
        Cell[][] board = playerBoards.get(sessionId);
        int[][] solution = playerSolutions.get(sessionId);
        if (board == null || solution == null)
            return false;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col].getValue() != solution[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getSolutionForPlayer(String sessionId) {
        return playerSolutions.get(sessionId);
    }

    public long[][] getCellCooldowns(String sessionId) {
        return cellCooldowns.get(sessionId);
    }

    // Utility: count filled cells (CORRECT_GUESS only) for a player
    public int getFilledCellCount(String sessionId) {
        Cell[][] board = playerBoards.get(sessionId);
        if (board == null)
            return 0;
        int count = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                CellStatus status = board[row][col].getStatus();
                if (status == CellStatus.CORRECT_GUESS) {
                    count++;
                }
            }
        }
        return count;
    }

    public Instant getLastRemoveUsed(String sessionId) {
        return lastRemoveUsed.get(sessionId);
    }

    // … other game-state methods (puzzle grid, steps behind, etc.) …
}
