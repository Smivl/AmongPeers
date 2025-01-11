package PlayerView;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.jspace.Space;

import java.util.Arrays;

public class Sprite extends Circle {
    private double SPEED = 3;
    private double[] velocity = new double[]{0,0};


    {
        setRadius(100);
    }

    public void move(double[] position){
        setCenterX(position[0]);
        setCenterY(position[1]);
    }

    public void setAsMain(Space positionSpace) {
        Timeline t = getTimeline(positionSpace);
        t.play();
        
        getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
    }

    private Timeline getTimeline(Space positionSpace) {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(16), e ->{
            if (!Arrays.equals(velocity, new double[]{0,0})){
                System.out.println("moving");
                double[] newPosition = new double[]{getCenterX() + velocity[0], getCenterY() + velocity[1]};
                move(newPosition);
                try {
                    positionSpace.put("SERVER", "POSITION_CHANGE");
                    positionSpace.put("POSITION_CHANGE", newPosition);
                } catch (InterruptedException er) {
                    throw new RuntimeException(er);
                }
            }
        }));
        t.setCycleCount(Timeline.INDEFINITE);
        return t;
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        // EDIT VELOCITY
        if (keyEvent.getCode() == KeyCode.W){
            velocity[1] = -SPEED;
        } else if (keyEvent.getCode() == KeyCode.A) {
            velocity[0] = -SPEED;
        } else if (keyEvent.getCode() == KeyCode.S) {
            velocity[1] = SPEED;
        } else if (keyEvent.getCode() == KeyCode.D) {
            velocity[0] = SPEED;
        }
    }
    private void handleKeyReleased(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.W){
            velocity[1] = 0;
        } else if (keyEvent.getCode() == KeyCode.A) {
            velocity[0] = 0;
        } else if (keyEvent.getCode() == KeyCode.S) {
            velocity[1] = 0;
        } else if (keyEvent.getCode() == KeyCode.D) {
            velocity[0] = 0;
        }
    }
}

