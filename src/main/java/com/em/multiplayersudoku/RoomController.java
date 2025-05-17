package com.em.multiplayersudoku;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
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
                // Only allow the user to update their own board
                if (sessionId != null && room.getPlayers().contains(sessionId)) {
                    room.updateCellForPlayer(sessionId, action.getRow(), action.getCol(),
                            action.getValue());
                }
                break;
            case REMOVE:
                // Only allow the user to update their own board
                if (sessionId != null && room.getPlayers().contains(sessionId)) {
                    room.updateCellForPlayer(sessionId, action.getRow(), action.getCol(), 0);
                }
                break;
            case JOIN:
                // No-op, just broadcast boards below
                break;
            case LEAVE:
                // No-op, just broadcast boards below
                break;
            case WIN:
                // No-op, just broadcast boards below
                break;
            case HEARTBEAT:
                // Optionally handle keepalive/ping
                return;
        }
        // After any board-changing action, broadcast all boards in a single message
        Map<String, Cell[][]> boards = new java.util.HashMap<>();
        for (String player : room.getPlayers()) {
            logger.info("Player {}: {}", player, room.getBoardForPlayer(player));
            boards.put(player, room.getBoardForPlayer(player));
        }
        int playerCount = room.getPlayers().size();
        BoardsListMessage boardsListMessage = new BoardsListMessage(boards, playerCount);
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
        // Broadcast all boards to all players
        Map<String, Cell[][]> boards = new java.util.HashMap<>();
        for (String player : room.getPlayers()) {
            boards.put(player, room.getBoardForPlayer(player));
        }
        int playerCount = room.getPlayers().size();
        BoardsListMessage boardsListMessage = new BoardsListMessage(boards, playerCount);
        messagingTemplate.convertAndSend("/topic/room/" + code, boardsListMessage);
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}