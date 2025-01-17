package Game.Player;

import Game.GameCharacter.CharacterType;

public class PlayerInfo{
    public CharacterType color;
    public double[] position;
    public double[] velocity;
    public boolean isAlive;
    public boolean isImposter;

    public PlayerInfo(CharacterType color, double[] position, double[] velocity, boolean isAlive, boolean isImposter){
        this.color = color;
        this.position = position;
        this.velocity = velocity;
        this.isAlive = isAlive;
        this.isImposter = isImposter;
    }
}