package com.em.multiplayersudoku;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.em.multiplayersudoku.domain.Difficulty;
import com.em.multiplayersudoku.domain.Room;

@Service
public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public synchronized String createRoom(Difficulty difficulty, int removeThreshold, int cooldownSeconds) {
        String code;
        do {
            code = generateRoomCode();
        } while (rooms.containsKey(code));

        Room room = new Room(code, difficulty, removeThreshold, cooldownSeconds);
        rooms.put(code, room);
        return code;
    }

    public Room getRoom(String code) {
        return rooms.get(code);
    }

    public void removeRoom(String code) {
        rooms.remove(code);
    }

    public boolean roomExists(String code) {
        return rooms.containsKey(code);
    }

    public void addPlayerToRoom(String code, String sessionId) {
        Room room = rooms.get(code);
        if (room != null)
            room.addPlayer(sessionId);
    }

    public void removePlayerFromAllRooms(String sessionId) {
        rooms.values().forEach(room -> room.removePlayer(sessionId));
    }

    // Find the room that contains the given sessionId
    public Room findRoomBySessionId(String sessionId) {
        for (Room room : rooms.values()) {
            if (room.getPlayers().contains(sessionId)) {
                return room;
            }
        }
        return null;
    }

    private String generateRoomCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // … persistence or cleanup methods as needed …
}
