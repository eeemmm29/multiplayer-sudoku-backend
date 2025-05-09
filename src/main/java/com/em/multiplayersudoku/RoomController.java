package com.em.multiplayersudoku;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.em.multiplayersudoku.domain.GameAction;
import com.em.multiplayersudoku.domain.Room;

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

    @MessageMapping("/create-room")
    @SendTo("/topic/rooms")
    public String createRoom() {
        return "Room created with code: " + generateRoomCode();
    }

    @MessageMapping("/join-room")
    @SendTo("/topic/rooms")
    public String joinRoom(String roomCode) {
        return "User joined room: " + roomCode;
    }

    @MessageMapping("/send-message")
    public void sendMessage(String payload) {
        System.out.println("Received payload: " + payload);
        String[] parts = payload.split(":", 2);
        String roomCode = parts[0];
        String message = parts[1];
        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode, "Message: " + message);
    }

    private String generateRoomCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}