package PlayerM;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlayerView extends Circle {
    public PlayerView(double x, double y, Color color) {
        this.setRadius(50.0);
        this.setCenterX(x);
        this.setCenterY(y);
        this.setFill(color);
    }

    public void render(double x, double y) {
        this.setCenterX(x);
        this.setCenterY(y);
    }
}
