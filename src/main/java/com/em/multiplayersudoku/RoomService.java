package com.em.multiplayersudoku;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.em.multiplayersudoku.domain.Difficulty;
import com.em.multiplayersudoku.domain.Room;

@Service
public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(String code, Difficulty difficulty, int removeThreshold, int cooldownSeconds) {
        Room room = new Room(code, difficulty, removeThreshold, cooldownSeconds);
        rooms.put(code, room);
        return room;
    }

    public Room getRoom(String code) {
        return rooms.get(code);
    }

    public void addPlayerToRoom(String code, String sessionId) {
        Room room = rooms.get(code);
        if (room != null)
            room.addPlayer(sessionId);
    }

    public void removePlayerFromAllRooms(String sessionId) {
        rooms.values().forEach(room -> room.removePlayer(sessionId));
    }

    // … persistence or cleanup methods as needed …
}
