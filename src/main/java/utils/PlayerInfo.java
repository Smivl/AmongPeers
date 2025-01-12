package utils;

import javafx.scene.paint.Color;

public class PlayerInfo{
    public Color color;
    public double[] position;
    public double[] velocity;

    public PlayerInfo(Color color, double[] position, double[] velocity){
        this.color = color;
        this.position = position;
        this.velocity = velocity;
    }
}