package Map;

import PlayerM.PlayerView;
import utils.PlayerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class GameMap {

    private GameMapView view;
    private final List<Shape> collisionShapes = new ArrayList<Shape>() {};

    private Map<String, PlayerView> playerViews = new HashMap<>();

    public GameMapView getView() {
        return this.view;
    }

    public GameMap(Scene scene) {
        this.view = new GameMapView(scene);

        // Cafeteria to Upper engine walls
        this.createCollisionRectangle(2327, 537, 1553, 495, 0);

        // Med bay walls
        this.createCollisionRectangle(3482, 1335, 281,158,0);

        //
        this.createCollisionRectangle(5553, 2492, 559, 85, 0);

        // Cafeteria walls
        this.createCollisionRectangle(3793, 341, 520, 50, -45);
        this.createCollisionRectangle(4217, 170, 1175, 60, 0);
        this.createCollisionRectangle(5251, 427, 780, 50, 45);
        this.createCollisionRectangle(5889, 697, 496,340, 0);
        this.createCollisionRectangle(3756, 1335, 132, 592,0);
        this.createCollisionRectangle(3700, 1976, 694, 216, 45);
        this.createCollisionRectangle(4070, 2213, 675, 730, 0);
        this.createCollisionRectangle(4998, 2212, 1574,305,0);
        this.createCollisionRectangle(5389, 1945, 690, 148, -45);
        this.createCollisionRectangle(5902, 1345, 680, 500,0);

        // Cafeteria tables
        this.createCollisionCircle(4850, 1258, 230, 175);
        this.createCollisionCircle(4394, 779, 232,167);
        this.createCollisionCircle(4394, 1714, 232,167);
        this.createCollisionCircle(5288, 769, 225, 168);
        this.createCollisionCircle(5288, 1716, 225, 168);

        // Weapony walls
        this.createCollisionRectangle(6385, 652, 34, 258,0);
        this.createCollisionRectangle(6416, 652, 63, 220,0);
        this.createCollisionRectangle(6379, 594, 385, 47,0);

        this.createCollisionCircle(6668, 1103, 55, 50);

        this.view.getChildren().addAll(this.collisionShapes);
    }

    public void onUpdate(double delta) {
    }

    public void handlePositionUpdate(String playerName, double[] newPosition, double[] velocity){
        PlayerView playerView = playerViews.get(playerName);
        playerView.render(newPosition[0], newPosition[1], velocity);
    }

    public void handlePlayerJoin(String playerName, PlayerInfo playerInfo){
        PlayerView newPlayer = new PlayerView(playerName, playerInfo.position[0], playerInfo.position[1], playerInfo.velocity, playerInfo.color);
        view.getChildren().add(newPlayer);
        playerViews.put(playerName, newPlayer);

    }

    public boolean checkCollision(PlayerView playerView) {
        Bounds playerBounds = playerView.getBoundsInParent();
        Rectangle playerShape = new Rectangle(playerBounds.getMinX(), playerBounds.getMinY(), playerBounds.getWidth(), playerBounds.getHeight());
        this.view.getChildren().add(playerShape);

        for (Shape shape : collisionShapes){
            Shape intersection = Shape.intersect(playerShape, shape);
            if(!intersection.getBoundsInLocal().isEmpty()){
                this.view.getChildren().remove(playerShape);
                return true;
            }
        }

        this.view.getChildren().remove(playerShape);
        return false;
    }

    private void createCollisionRectangle(int x, int y, int width, int height, int angle) {

        Rectangle wall = new Rectangle(x, y, width, height);
        wall.setRotate(angle);
        wall.setFill(Color.TRANSPARENT);
        wall.setStroke(Color.RED);
        this.collisionShapes.add(wall);
    }

    private void createCollisionCircle(int x, int y, int radiusX, int radiusY) {

        Ellipse ellipse = new Ellipse(x, y, radiusX, radiusY);
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(Color.RED);
        this.collisionShapes.add(ellipse);

    }

}