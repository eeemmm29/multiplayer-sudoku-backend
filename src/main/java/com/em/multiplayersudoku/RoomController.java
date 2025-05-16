package com.em.multiplayersudoku;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.em.multiplayersudoku.domain.Difficulty;
import com.em.multiplayersudoku.domain.GameAction;
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
        switch (action.getType()) {
            case FILL:
                room.updateCellForPlayer(sessionId, action.getRow(), action.getCol(), action.getValue());
                // Broadcast both boards to all players
                for (String player : room.getPlayers()) {
                    Cell[][] myBoard = room.getBoardForPlayer(player);
                    String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
                    Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
                    int playerCount = room.getPlayers().size();
                    BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
                    messagingTemplate.convertAndSend("/topic/room/" + code, boardsMessage);
                }
                break;
            case REMOVE:
                for (String player : room.getPlayers()) {
                    if (!player.equals(sessionId)) {
                        room.updateCellForPlayer(player, action.getRow(), action.getCol(), 0);
                    }
                }
                // Broadcast both boards to all players
                for (String player : room.getPlayers()) {
                    Cell[][] myBoard = room.getBoardForPlayer(player);
                    String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
                    Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
                    int playerCount = room.getPlayers().size();
                    BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
                    messagingTemplate.convertAndSend("/topic/room/" + code, boardsMessage);
                }
                break;
            case JOIN:
                // Also broadcast updated boards to all players
                for (String player : room.getPlayers()) {
                    Cell[][] myBoard = room.getBoardForPlayer(player);
                    String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
                    Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
                    int playerCount = room.getPlayers().size();
                    BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
                    messagingTemplate.convertAndSend("/topic/room/" + code, boardsMessage);
                }
                break;
            case LEAVE:
                // Also broadcast updated boards to all players
                for (String player : room.getPlayers()) {
                    Cell[][] myBoard = room.getBoardForPlayer(player);
                    String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
                    Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
                    int playerCount = room.getPlayers().size();
                    BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
                    messagingTemplate.convertAndSend("/topic/room/" + code, boardsMessage);
                }
                break;
            case WIN:
                // Also broadcast updated boards to all players
                for (String player : room.getPlayers()) {
                    Cell[][] myBoard = room.getBoardForPlayer(player);
                    String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
                    Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
                    int playerCount = room.getPlayers().size();
                    BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
                    messagingTemplate.convertAndSend("/topic/room/" + code, boardsMessage);
                }
                break;
            case HEARTBEAT:
                // Optionally handle keepalive/ping
                break;
        }
    }

    @PostMapping("/room")
    @ResponseBody
    public RoomCreatedResponse createRoom(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        logger.info("createRoom (HTTP): sessionId={}", sessionId);
        // You can add parameters to the request if you want to specify difficulty, etc.
        Difficulty difficulty = Difficulty.EASY; // Default or based on request
        int maxStepGap = 5;
        int cooldownSeconds = 10;

        String code = roomService.createRoom(difficulty, maxStepGap, cooldownSeconds);
        Room room = roomService.getRoom(code);
        if (sessionId != null && !sessionId.isEmpty()) {
            room.addPlayer(sessionId);
        }
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
        // For now, use 30 clues for medium difficulty
        room.initializeBoardForPlayer(sessionId, 30);
        // Send both boards to both players
        int playerCount = room.getPlayers().size();
        for (String player : room.getPlayers()) {
            Cell[][] myBoard = room.getBoardForPlayer(player);
            String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
            Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
            BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
            logger.info("Sending boards to player {}: myBoard={}, opponentBoard={}, playerCount={}, roomHash={}",
                    player,
                    myBoard != null, opponentBoard != null, playerCount, System.identityHashCode(room));
            messagingTemplate.convertAndSendToUser(player, "/topic/room/" + code, boardsMessage);
        }
    }
}