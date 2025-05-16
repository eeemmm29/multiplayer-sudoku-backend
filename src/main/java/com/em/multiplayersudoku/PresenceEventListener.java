package com.em.multiplayersudoku;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.em.multiplayersudoku.domain.Room;

@Component
public class PresenceEventListener {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String dest = sha.getDestination();
        if (dest != null && dest.matches("/topic/room/\\w{4}")) {
            String code = dest.substring(dest.lastIndexOf('/') + 1);
            String sessionId = sha.getSessionId();
            roomService.addPlayerToRoom(code, sessionId);
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        roomService.removePlayerFromAllRooms(sessionId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Room room = roomService.findRoomBySessionId(sessionId);
        if (room != null) {
            room.removePlayer(sessionId);
            // Broadcast updated boards to all players in the room
            for (String player : room.getPlayers()) {
                Cell[][] myBoard = room.getBoardForPlayer(player);
                String other = room.getPlayers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
                Cell[][] opponentBoard = (other != null) ? room.getBoardForPlayer(other) : null;
                int playerCount = room.getPlayers().size();
                BoardsMessage boardsMessage = new BoardsMessage(myBoard, opponentBoard, playerCount);
                messagingTemplate.convertAndSend("/topic/room/" + room.getCode(), boardsMessage);
            }
        }
        roomService.removePlayerFromAllRooms(sessionId);
    }
}
