package com.em.multiplayersudoku;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.em.multiplayersudoku.domain.Difficulty;
import com.em.multiplayersudoku.domain.GameAction;
import com.em.multiplayersudoku.domain.GameAction.ActionType;
import com.em.multiplayersudoku.domain.Room;
import com.em.multiplayersudoku.domain.RoomCreatedResponse;

@Controller
@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class RoomController {
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{code}/action")
    public void handleAction(@DestinationVariable String code, GameAction action,
            @Header("simpSessionId") String sessionId) {
        logger.info("handleAction: code={}, sessionId={}, actionType={}", code, sessionId, action.getType());
        Room room = roomService.getRoom(code);
        if (room == null) {
            logger.warn("Room not found: {}", code);
            return;
        }
        logger.info("Players in room {}: {}", code, room.getPlayers());
        Map<String, Boolean> canRemoveOpponentCellMap = new java.util.HashMap<>();
        Map<String, Long> removeCooldownUntilMap = new java.util.HashMap<>();
        switch (action.getType()) {
            case FILL:
                if (sessionId != null && room.getPlayers().contains(sessionId)) {
                    // Block input if cell is on cooldown
                    if (room.isCellOnCooldown(sessionId, action.getRow(), action.getCol())) {
                        break;
                    }
                    int[][] solution = room.getSolutionForPlayer(sessionId);
                    if (solution != null && action.getValue() != solution[action.getRow()][action.getCol()]) {
                        room.setCellCooldown(sessionId, action.getRow(), action.getCol());
                    }
                    room.updateCellForPlayer(sessionId, action.getRow(), action.getCol(), action.getValue());
                    if (room.isPlayerBoardComplete(sessionId)) {
                        GameAction winAction = new GameAction();
                        winAction.setType(ActionType.WIN);
                        winAction.setSessionId(sessionId);
                        messagingTemplate.convertAndSend("/topic/room/" + code, winAction);
                    }
                }
                break;
            case REMOVE:
                logger.info("REMOVE action: sessionId={}, actionSessionId={}", sessionId, action.getSessionId());
                if (sessionId != null && room.getPlayers().contains(sessionId)) {
                    // If removing from own board, always allow
                    if (action.getSessionId() != null && action.getSessionId().equals(sessionId)) {
                        room.updateCellForPlayer(sessionId, action.getRow(), action.getCol(), 0);
                    } else {
                        // Removing from opponent's board: validate eligibility
                        boolean canRemove = canRemoveOpponentCellMap.getOrDefault(sessionId, false);
                        if (canRemove) {
                            // Find opponent
                            String opponentId = room.getPlayers().stream().filter(id -> !id.equals(sessionId))
                                    .findFirst().orElse(null);
                            if (opponentId != null && action.getSessionId() != null
                                    && action.getSessionId().equals(opponentId)) {
                                room.updateCellForPlayer(opponentId, action.getRow(), action.getCol(), 0);
                                room.recordRemoveUse(sessionId);
                            }
                        }
                        // else: ignore/remove not allowed
                    }
                }
                break;
            case JOIN:
                // No-op, just broadcast boards below
                break;
            case LEAVE:
                // No-op, just broadcast boards below
                break;
            case WIN:
                // Fill all cells for the winner and broadcast
                if (sessionId != null && room.getPlayers().contains(sessionId)) {
                    int[][] solution = room.getSolutionForPlayer(sessionId);
                    if (solution != null) {
                        Cell[][] board = room.getBoardForPlayer(sessionId);
                        for (int row = 0; row < solution.length; row++) {
                            for (int col = 0; col < solution[row].length; col++) {
                                board[row][col].setValue(solution[row][col]);
                                board[row][col].setStatus(com.em.multiplayersudoku.CellStatus.CORRECT_GUESS);
                            }
                        }
                        // Broadcast WIN action to all clients
                        GameAction winAction = new GameAction();
                        winAction.setType(ActionType.WIN);
                        winAction.setSessionId(sessionId);
                        messagingTemplate.convertAndSend("/topic/room/" + code, winAction);
                    }
                }
                break;
            case HEARTBEAT:
                // Optionally handle keepalive/ping
                return;
        }
        // After any board-changing action, broadcast all boards in a single message
        Map<String, Cell[][]> boards = new java.util.HashMap<>();
        Map<String, Integer> filledCounts = new java.util.HashMap<>();
        Map<String, Integer> stepsAhead = new java.util.HashMap<>();
        int maxFilled = 0;
        for (String player : room.getPlayers()) {
            Cell[][] board = room.getBoardForPlayer(player);
            long[][] cooldowns = room.getCellCooldowns(player);
            if (board != null && cooldowns != null) {
                for (int row = 0; row < board.length; row++) {
                    for (int col = 0; col < board[row].length; col++) {
                        board[row][col].setCooldownUntil(cooldowns[row][col]);
                    }
                }
            }
            boards.put(player, board);
            int filled = room.getFilledCellCount(player);
            filledCounts.put(player, filled);
            if (filled > maxFilled)
                maxFilled = filled;
        }
        // Calculate steps ahead for each player (relative to the max filled)
        for (String player : room.getPlayers()) {
            stepsAhead.put(player, filledCounts.get(player)
                    - (filledCounts.values().stream().filter(x -> x != null).mapToInt(x -> x).max().orElse(0)));
        }
        int playerCount = room.getPlayers().size();
        // Build eligibility maps for all players
        for (String playerSessionId : room.getPlayers()) {
            boolean canRemove = false;
            long cooldownUntil = 0L;
            if (room.getPlayers().size() == 2) {
                String opponentId = room.getPlayers().stream().filter(id -> !id.equals(playerSessionId)).findFirst()
                        .orElse(null);
                if (opponentId != null) {
                    int myFilled = filledCounts.getOrDefault(playerSessionId, 0);
                    int oppFilled = filledCounts.getOrDefault(opponentId, 0);
                    int stepGap = oppFilled - myFilled;
                    boolean cooldownReady = room.canUseRemove(playerSessionId);
                    canRemove = (stepGap >= room.getRemoveThreshold()) && cooldownReady;
                    if (!cooldownReady) {
                        java.time.Instant last = room.getLastRemoveUsed(playerSessionId);
                        if (last != null) {
                            cooldownUntil = last.plusSeconds(room.getCooldownSeconds()).toEpochMilli();
                        }
                    }
                }
            }
            canRemoveOpponentCellMap.put(playerSessionId, canRemove);
            removeCooldownUntilMap.put(playerSessionId, cooldownUntil);
        }
        BoardsListMessage boardsListMessage = new BoardsListMessage(boards, playerCount, filledCounts, stepsAhead);
        boardsListMessage.setCanRemoveOpponentCellMap(canRemoveOpponentCellMap);
        boardsListMessage.setRemoveCooldownUntilMap(removeCooldownUntilMap);
        messagingTemplate.convertAndSend("/topic/room/" + code, boardsListMessage);
    }

    @PostMapping("/room")
    @ResponseBody
    public RoomCreatedResponse createRoom() {
        // You can add parameters to the request if you want to specify difficulty, etc.
        Difficulty difficulty = Difficulty.EASY; // Default or based on request
        int maxStepGap = 5;
        int cooldownSeconds = 10;
        String code = roomService.createRoom(difficulty, maxStepGap, cooldownSeconds);
        return new RoomCreatedResponse(code);
    }

    @MessageMapping("/room/{code}/start")
    public void handleStartGame(@DestinationVariable String code, @Header("simpSessionId") String sessionId) {
        logger.info("handleStartGame: code={}, sessionId={}", code, sessionId);
        Room room = roomService.getRoom(code);
        if (room == null) {
            logger.warn("Room not found in handleStartGame: {}", code);
            return;
        }
        logger.info("Room object identity: {}", System.identityHashCode(room));
        roomService.addPlayerToRoom(code, sessionId);
        logger.info("Players in room {} after add: {}", code, room.getPlayers());
        // Initialize a board for every player in the room
        for (String player : room.getPlayers()) {
            room.initializeBoardForPlayer(player, 30);
        }
        // Broadcast all boards to all players (on game start)
        Map<String, Cell[][]> boards = new java.util.HashMap<>();
        Map<String, Integer> filledCounts = new java.util.HashMap<>();
        Map<String, Integer> stepsAhead = new java.util.HashMap<>();
        int maxFilled = 0;
        for (String player : room.getPlayers()) {
            Cell[][] board = room.getBoardForPlayer(player);
            boards.put(player, board);
            int filled = room.getFilledCellCount(player);
            filledCounts.put(player, filled);
            if (filled > maxFilled)
                maxFilled = filled;
        }
        for (String player : room.getPlayers()) {
            stepsAhead.put(player, filledCounts.get(player)
                    - (filledCounts.values().stream().filter(x -> x != null).mapToInt(x -> x).max().orElse(0)));
        }
        int playerCount = room.getPlayers().size();
        // Build eligibility maps for all players
        Map<String, Boolean> canRemoveOpponentCellMap = new java.util.HashMap<>();
        Map<String, Long> removeCooldownUntilMap = new java.util.HashMap<>();
        for (String playerSessionId : room.getPlayers()) {
            boolean canRemove = false;
            long cooldownUntil = 0L;
            if (room.getPlayers().size() == 2) {
                String opponentId = room.getPlayers().stream().filter(id -> !id.equals(playerSessionId)).findFirst()
                        .orElse(null);
                if (opponentId != null) {
                    int myFilled = filledCounts.getOrDefault(playerSessionId, 0);
                    int oppFilled = filledCounts.getOrDefault(opponentId, 0);
                    int stepGap = oppFilled - myFilled;
                    boolean cooldownReady = room.canUseRemove(playerSessionId);
                    canRemove = (stepGap >= room.getRemoveThreshold()) && cooldownReady;
                    if (!cooldownReady) {
                        java.time.Instant last = room.getLastRemoveUsed(playerSessionId);
                        if (last != null) {
                            cooldownUntil = last.plusSeconds(room.getCooldownSeconds()).toEpochMilli();
                        }
                    }
                }
            }
            canRemoveOpponentCellMap.put(playerSessionId, canRemove);
            removeCooldownUntilMap.put(playerSessionId, cooldownUntil);
        }
        BoardsListMessage boardsListMessage = new BoardsListMessage(boards, playerCount, filledCounts, stepsAhead);
        boardsListMessage.setCanRemoveOpponentCellMap(canRemoveOpponentCellMap);
        boardsListMessage.setRemoveCooldownUntilMap(removeCooldownUntilMap);
        messagingTemplate.convertAndSend("/topic/room/" + code, boardsListMessage);
    }
}