package Server;

// In-game server updates
public enum ServerUpdate {
    POSITION,
    KILLED,
    SABOTAGE_STARTED, SABOTAGE_ENDED,
    TASK_COMPLETE,
    PLAYER_JOINED, PLAYER_LEFT,
    VOTE, MESSAGE, MEETING_START, MEETING_DONE,
    IMPOSTERS_WIN, CREWMATES_WIN,
    // not broadcast updates:
    PLAYER_INIT, GAME_START,
}
