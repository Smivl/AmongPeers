package PlayerView;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.jspace.Space;

import java.util.Random;

public class Sprite extends Circle {
    private double SPEED = 3;
    private double[] velocity = new double[]{0,0};

    {
        setRadius(100);
    }

    public void move(double[] position){
        setCenterX(position[0]);
        setCenterX(position[1]);
    }

    public void setAsMain(Space positionSpace) {
        setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()){
                case W : {
                    velocity[1] = -SPEED;
                }
                case A : {
                    velocity[0] = -SPEED;
                }
                case S : {
                    velocity[1] = SPEED;
                }
                case D : {
                    velocity[0] = SPEED;
                }
            }
            double[] newPosition = new double[]{getCenterX() + velocity[0], getCenterY() + velocity[1]};
            move(newPosition);
            try {
                positionSpace.put("POSITION_CHANGE", newPosition);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
