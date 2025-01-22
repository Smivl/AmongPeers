package utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public enum Audios {
    BACKGROUND("/audio/Skeld_Ambience_-_Among_Us.mp3"),
    FOOTSTEP("/audio/the-among-us-walking-sound-effect.mp3"),
    MEETING("/audio/emergency-meeting_NTlaXkd.mp3"),
    IMPOSTER_WIN("/audio/uh-oh_8lISF1g.mp3"),
    CREWMATE_WIN("/audio/yay_cRiHGGR.mp3");

    final Media sound;

    Audios(String path){
        sound = new Media(getClass().getResource(path).toExternalForm());
    }

    public MediaPlayer getMediaPlayer(){
        return new MediaPlayer(sound);
    }
}
