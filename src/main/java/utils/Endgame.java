package utils;

public enum Endgame {
    IMPOSTER(Audios.IMPOSTER_WIN, "Imposter won!"),
    CREWMATE(Audios.CREWMATE_WIN, "Crewmates won!");

    final Audios audio;
    final String message;

    Endgame(Audios audio, String message){
        this.audio = audio;
        this.message = message;
    }

    public Audios getAudio() {
        return audio;
    }

    public String getMessage() {
        return message;
    }
}
