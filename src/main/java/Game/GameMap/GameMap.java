package Game.GameMap;

import Game.GameCharacter.CharacterView;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;


public class GameMap {

    private final GameMapView view;
    private final List<Shape> collisionShapes = new ArrayList<>() {};
    private List<ImageView> bodies = new ArrayList<>();

    public GameMapView getView() { return this.view; }

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

    public void addPlayer(CharacterView playerView, boolean isVisible){
        view.getChildren().add(playerView);
        playerView.setVisible(isVisible);

    }

    public void onPlayerKilled(double[] position) {
        Image i = new Image("dead1.png");
        ImageView newBody = new ImageView(i);

        newBody.setLayoutX(position[0] - (i.getWidth()/2));
        newBody.setLayoutY(position[1] - (i.getHeight()/4));

        bodies.add(newBody);

        this.view.onPlayerKilled(newBody);
    }

    public boolean checkCollisionsWithBodies(CharacterView characterView){
        for (ImageView body : bodies){
            double centreX = body.getLayoutX() + 50;
            double centreY = body.getLayoutY() + 25;

            double dist = Math.sqrt(Math.pow(characterView.getCenterX()-centreX, 2)+Math.pow(characterView.getCenterY()-centreY, 2));
            if(dist < 150) {
                return true;
            }
        }

        return false;
    }

    public boolean checkCollision(CharacterView characterView) {
        Bounds playerBounds = characterView.getBoundsInParent();
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