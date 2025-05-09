package com.em.multiplayersudoku;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.em.multiplayersudoku.domain.Difficulty;
import com.em.multiplayersudoku.domain.GameAction;
import com.em.multiplayersudoku.domain.Room;
import com.em.multiplayersudoku.domain.RoomCreatedResponse;

@Controller
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{code}/action")
    public void handleAction(@DestinationVariable String code, GameAction action,
            @Header("simpSessionId") String sessionId) {
        Room room = roomService.getRoom(code);
        // apply game logic: fill cell, check win, handle remove-number power-up…
        // broadcast updated state to both players:
        messagingTemplate.convertAndSend("/topic/room/" + code, action);
    }

    @MessageMapping("/room/create")
    @SendToUser("/topic/room/created")
    public RoomCreatedResponse handleRoomCreate(GameAction action, @Header("simpSessionId") String sessionId) {
        // You can add parameters to GameAction if you want to specify difficulty, etc.
        Difficulty difficulty = Difficulty.EASY; // Default or based on action
        int maxStepGap = 5;
        int cooldownSeconds = 10;

        String code = roomService.createRoom(difficulty, maxStepGap, cooldownSeconds);
        Room room = roomService.getRoom(code);
        room.addPlayer(sessionId);

        // Optionally broadcast to others that a new room is available, or keep it
        // private

        return new RoomCreatedResponse(code);
    }
}