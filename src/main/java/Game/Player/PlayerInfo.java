package Game.Player;

import javafx.scene.paint.Color;

public class PlayerInfo{
    public Color color;
    public double[] position;
    public double[] velocity;
    public boolean isAlive;

    public PlayerInfo(Color color, double[] position, double[] velocity, boolean isAlive){
        this.color = color;
        this.position = position;
        this.velocity = velocity;
        this.isAlive = isAlive;
    }
}