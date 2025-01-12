package PlayerM;

import Map.GameMap;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jspace.Space;

public class Player {

    private final int SPEED = 400;
    private final PlayerView view;
    private final double[] position;
    private final double[] velocity;

    private Space playerSpace;
    private Space serverSpace;

    public PlayerView getView() {
        return this.view;
    }
    public void setPlayerSpace(Space space) { this.playerSpace = space; }
    public void setServerSpace(Space space) { this.serverSpace = space; }

    public Player(double x, double y, Color color) {

        this.view = new PlayerView(x, y, color);

        this.position = new double[]{x, y};
        this.velocity = new double[]{0.0, 0.0};
    }

    public void onUpdate(double delta, GameMap map) {

        double newX = this.position[0] + this.velocity[0] * delta;
        double newY = this.position[1] + this.velocity[1] * delta;

        this.view.render(newX, newY);

        // If no collision then move player position
        if (!map.checkCollision(this.view)) {
            if(this.position[0] != newX || this.position[1] != newY){
                // push updated movement to server
                try{
                    playerSpace.put("POSITION_CHANGE", newX, newY);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }

            this.position[0] = newX;
            this.position[1] = newY;


        }else{
            this.view.render(this.position[0], this.position[1]);
        }



    }

    public void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.W) {
            this.velocity[1] = 0.0;
        }

        if (event.getCode() == KeyCode.A) {
            this.velocity[0] = 0.0;
        }

        if (event.getCode() == KeyCode.S) {
            this.velocity[1] = 0.0;
        }

        if (event.getCode() == KeyCode.D) {
            this.velocity[0] = 0.0;
        }

    }

    public void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.W) {
            this.velocity[1] = -SPEED;
        }

        if (event.getCode() == KeyCode.A) {
            this.velocity[0] = -SPEED;
        }

        if (event.getCode() == KeyCode.S) {
            this.velocity[1] = SPEED;
        }

        if (event.getCode() == KeyCode.D) {
            this.velocity[0] = SPEED;
        }

    }
}
