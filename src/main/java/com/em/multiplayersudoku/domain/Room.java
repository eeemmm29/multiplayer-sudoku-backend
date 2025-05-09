package com.em.multiplayersudoku.domain;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String code;
    private final Difficulty difficulty;
    private final int removeThreshold;
    private final int cooldownSeconds;

    // session IDs of the two players
    private final Set<String> players = ConcurrentHashMap.newKeySet();

    // track last-used timestamps for power-ups per session
    private final ConcurrentHashMap<String, Instant> lastRemoveUsed = new ConcurrentHashMap<>();

    public Room(String code, Difficulty difficulty, int removeThreshold, int cooldownSeconds) {
        this.code = code;
        this.difficulty = difficulty;
        this.removeThreshold = removeThreshold;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getCode() {
        return code;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getRemoveThreshold() {
        return removeThreshold;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public Set<String> getPlayers() {
        return players;
    }

    public void addPlayer(String sessionId) {
        if (!players.contains(sessionId) && players.size() < 2) {
            players.add(sessionId);
        }
    }

    public void removePlayer(String sessionId) {
        players.remove(sessionId);
        lastRemoveUsed.remove(sessionId);
    }

    public boolean canUseRemove(String sessionId) {
        Instant last = lastRemoveUsed.get(sessionId);
        return last == null || Instant.now().isAfter(last.plusSeconds(cooldownSeconds));
    }

    public void recordRemoveUse(String sessionId) {
        lastRemoveUsed.put(sessionId, Instant.now());
    }

    // … other game-state methods (puzzle grid, steps behind, etc.) …
}
