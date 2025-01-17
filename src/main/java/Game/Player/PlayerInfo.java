package Game.Player;

import Game.GameCharacter.GameCharacter;
import Game.GameCharacter.GameCharacterType;
import javafx.scene.paint.Color;

public class PlayerInfo{
    public GameCharacterType color;
    public double[] position;
    public double[] velocity;
    public boolean isAlive;
    public boolean isImposter;

    public PlayerInfo(GameCharacterType color, double[] position, double[] velocity, boolean isAlive, boolean isImposter){
        this.color = color;
        this.position = position;
        this.velocity = velocity;
        this.isAlive = isAlive;
        this.isImposter = isImposter;
    }
}