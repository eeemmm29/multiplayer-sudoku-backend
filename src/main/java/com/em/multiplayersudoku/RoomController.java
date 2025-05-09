package com.em.multiplayersudoku;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RoomController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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