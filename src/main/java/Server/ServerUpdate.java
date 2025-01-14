package Server;

// In-game server updates
public enum ServerUpdate {
    POSITION,
    PLAYER_JOINED,
    PLAYER_LEFT,
    VOTE, MESSAGE, MEETING_START, MEETING_DONE,
    // not broadcast updates:
    PLAYER_INIT,
}
