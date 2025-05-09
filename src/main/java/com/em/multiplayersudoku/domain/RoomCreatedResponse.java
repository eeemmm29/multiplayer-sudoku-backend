package com.em.multiplayersudoku.domain;

public class RoomCreatedResponse {
    private String roomCode;

    public RoomCreatedResponse(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() {
        return roomCode;
    }
}
