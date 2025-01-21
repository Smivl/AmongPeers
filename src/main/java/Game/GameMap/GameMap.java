package Game.GameMap;

import Game.GameCharacter.CharacterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Game.Interactables.Interactable;
import Game.Interactables.Meeting;
import Game.Interactables.Task.*;
import Game.Interactables.Vent;
import Game.Player.PlayerInfo;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jspace.Tuple;


public class GameMap {

    private final GameMapView view;

    private final List<Shape> collisionShapes = new ArrayList<>() {};
    private final Map<Shape, Interactable> ventShapes = new HashMap<>(){};
    private final Map<Shape, Interactable> interactableShapes = new HashMap<>(){};

    private final List<ImageView> bodies = new ArrayList<>();

    public GameMapView getView() { return this.view; }

    public GameMap(Scene scene, PlayerInfo info, TaskType[] playerTasks) {
        this.view = new GameMapView(scene);

        // GLOBAL INTERACTABLES
        createInteractableCircle(4847, 1263, 264, 206, new Meeting());

        // IMPOSTER INTERACTABLES
        if (info.isImposter){
            // Vents
            createInteractableRectangle(5720, 1429, 99, 74, 0, new Vent());
            createInteractableRectangle(6542, 781, 99, 74, 0, new Vent());
            createInteractableRectangle(7838, 1951, 99, 74, 0, new Vent());
            createInteractableRectangle(7838, 2529, 99, 74, 0, new Vent());
            createInteractableRectangle(6644, 2536, 99, 74, 0, new Vent());
            createInteractableRectangle(6669, 3960, 99, 74, 0, new Vent());
            createInteractableRectangle(5411, 3169, 99, 74, 0, new Vent());
            createInteractableRectangle(3043, 2131, 99, 74, 0, new Vent());
            createInteractableRectangle(2697, 2630, 99, 74, 0, new Vent());
            createInteractableRectangle(2201, 925, 99, 74, 0, new Vent());
            createInteractableRectangle(1015, 1929, 99, 74, 0, new Vent());
            createInteractableRectangle(1211, 2630, 99, 74, 0, new Vent());
            createInteractableRectangle(2209, 3837, 99, 74, 0, new Vent());
            createInteractableRectangle(3193, 2824, 99, 74, 0, new Vent());

            this.view.getChildren().addAll(ventShapes.keySet());
        }
        // CREWMATE INTERACTABLES
        else {
            for(TaskType task : playerTasks){

                Task createdTask = TaskFactory.createTask(task);
                switch (task){
                    case WIRING:{
                        //    - Admin, Storage, Security, Cafeteria, Nav
                        createInteractableRectangle(5218,2536,69,49,0, createdTask);
                        createInteractableRectangle(4621,2951,69,49,0, createdTask);
                        createInteractableRectangle(2167,2210,69,49,0, createdTask);
                        createInteractableRectangle(4018,438,69,49,-45, createdTask);
                        createInteractableRectangle(7583,2065,69,49,0, createdTask);
                        break;
                    }case EMPTY_CHUTE:{
                        createInteractableRectangle(5848, 1989, 82, 55, -45, createdTask);
                        createInteractableRectangle(5110, 4418, 59, 84,0, createdTask);
                        break;
                    }case SUBMIT_SCAN:{
                        createInteractableCircle(3696, 2312, 42, 16, createdTask);
                        break;
                    }case UPLOAD_DATA:{
                        //    - Lights, Admin, Shields, Weaponry, Cafeteria, Nav
                        createInteractableRectangle(3215,2691, 68, 56, 0, createdTask);
                        createInteractableRectangle(5420,2505, 68, 56, 0, createdTask);
                        createInteractableRectangle(5669,3891, 68, 56, 0, createdTask);
                        createInteractableRectangle(6529,670, 68, 56, 0, createdTask);
                        createInteractableRectangle(5595,498, 68, 56, 45, createdTask);
                        createInteractableRectangle(8032,1822, 68, 56, 0, createdTask);
                        break;
                    }case EMPTY_GARBAGE:{
                        break;
                    }case CLEAN_O2_FILTER:{
                        createInteractableRectangle(6012, 1890, 68, 107, 0, createdTask);
                        break;
                    }
                }
            }
        }

        this.view.getChildren().addAll(interactableShapes.keySet());

        // Map collision
        {
            // Cafeteria to Upper engine walls
            this.createCollisionRectangle(2327, 537, 1553, 495, 0);

            // Med bay walls
            this.createCollisionRectangle(3482, 1335, 281, 158, 0);
            this.createCollisionRectangle(2323, 1335, 908, 158, 0);
            this.createCollisionRectangle(2858, 1487, 141, 1257, 0);
            this.createCollisionRectangle(2957, 2281, 133, 260, -38);
            this.createCollisionRectangle(3132, 2452, 978, 237, 0);
            this.createCollisionRectangle(3132, 2452, 978, 237, 0);

            // cameras
            this.createCollisionRectangle(2323, 1487, 535, 349, 0);
            this.createCollisionRectangle(2085, 1693, 307, 498, 0);
            this.createCollisionRectangle(2272, 1800, 295, 132, -45);
            this.createCollisionRectangle(2704, 1919, 271, 105, 45); // probably wrong
            this.createCollisionRectangle(2368, 2719, 490, 82, 0);
            this.createCollisionRectangle(2085, 2500, 307, 524, 0);

            // cameras table
            this.createCollisionRectangle(2680, 2255, 178, 110, 0);
            this.createCollisionRectangle(2769, 2365, 89, 62, 0);

            // lower engine to electrical / left electrical
            this.createCollisionRectangle(2327, 3024, 659, 309, 0);
            this.createCollisionRectangle(2967, 2689, 192, 1152, 0);
            this.createCollisionRectangle(2327, 3630, 355, 537, 0);
            this.createCollisionRectangle(2682, 4144, 1402, 126, 0);
            this.createCollisionRectangle(3417, 3645, 667, 207, 0);

            // electrical
            this.createCollisionRectangle(3858, 2858, 575, 276, -45);
            this.createCollisionRectangle(3845, 3102, 239, 543, 0);
            this.createCollisionRectangle(3638, 3457, 260, 43, -45);

            // deposit
            this.createCollisionRectangle(3977, 4362, 525, 83, 45);
            this.createCollisionRectangle(4435, 4560, 758, 59, 0);
            this.createCollisionRectangle(5169, 3732, 102, 887, 0);


            //
            this.createCollisionRectangle(5553, 2492, 559, 85, 0);

            // Cafeteria walls
            this.createCollisionRectangle(3793, 341, 520, 50, -45);
            this.createCollisionRectangle(4217, 170, 1175, 60, 0);
            this.createCollisionRectangle(5251, 427, 780, 50, 45);
            this.createCollisionRectangle(5889, 697, 496, 340, 0);
            this.createCollisionRectangle(3756, 1335, 132, 592, 0);
            this.createCollisionRectangle(3700, 1976, 694, 216, 45);
            this.createCollisionRectangle(4070, 2213, 675, 730, 0);
            this.createCollisionRectangle(4998, 2212, 1574, 305, 0);
            this.createCollisionRectangle(5389, 1945, 690, 148, -45);
            this.createCollisionRectangle(5902, 1345, 680, 500, 0);

            // Cafeteria tables
            this.createCollisionCircle(4850, 1258, 230, 175);
            this.createCollisionCircle(4394, 779, 232, 167);
            this.createCollisionCircle(4394, 1714, 232, 167);
            this.createCollisionCircle(5288, 769, 225, 168);
            this.createCollisionCircle(5288, 1716, 225, 168);

            // Weapony walls
            this.createCollisionRectangle(6385, 652, 34, 258, 0);
            this.createCollisionRectangle(6416, 652, 63, 220, 0);
            this.createCollisionRectangle(6379, 594, 385, 47, 0);

            this.createCollisionCircle(6668, 1103, 55, 50);
        }

        this.view.getChildren().addAll(this.collisionShapes);
    }

    public void onUpdate(double delta) {
    }

    public void addPlayer(CharacterView playerView, boolean isVisible){
        view.getChildren().add(playerView);
        playerView.setVisible(isVisible);

    }

    public void removePlayer(CharacterView characterView) {
        view.getChildren().remove(characterView);
    }

    public void onReset() {
        this.view.getChildren().removeAll(bodies);
        bodies.clear();
    }

    public void onPlayerKilled(double[] position) {
        Image i = new Image("dead1.png");
        ImageView newBody = new ImageView(i);

        newBody.setLayoutX(position[0] - (i.getWidth()/2));
        newBody.setLayoutY(position[1] - (i.getHeight()/4));

        bodies.add(newBody);

        newBody.setViewOrder(-position[1]);

        this.view.onPlayerKilled(newBody);
    }

    public boolean checkCollisionWithBodies(CharacterView characterView){
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

    public Interactable getInteractable(CharacterView characterView){
        return checkInteractableCollision(characterView, interactableShapes);
    }

    public Interactable getVent(CharacterView characterView){
        return checkInteractableCollision(characterView, ventShapes);
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

    private Interactable checkInteractableCollision(CharacterView characterView, Map<Shape, Interactable> interactables) {
        Bounds playerBounds = characterView.getBoundsInParent();
        Rectangle playerShape = new Rectangle(playerBounds.getMinX(), playerBounds.getMinY(), playerBounds.getWidth(), playerBounds.getHeight());
        this.view.getChildren().add(playerShape);

        for (Map.Entry<Shape, Interactable> entry : interactables.entrySet()){
            Shape intersection = Shape.intersect(playerShape, entry.getKey());
            if(!intersection.getBoundsInLocal().isEmpty()){
                this.view.getChildren().remove(playerShape);
                return entry.getValue();
            }
        }

        this.view.getChildren().remove(playerShape);
        return null;
    }

    private void createCollisionRectangle(int x, int y, int width, int height, int angle) {

        Rectangle wall = new Rectangle(x, y, width, height);
        wall.setRotate(angle);
        wall.setFill(Color.TRANSPARENT);
        //wall.setStroke(Color.RED);
        this.collisionShapes.add(wall);
    }

    private void createCollisionCircle(int x, int y, int radiusX, int radiusY) {

        Ellipse ellipse = new Ellipse(x, y, radiusX, radiusY);
        ellipse.setFill(Color.TRANSPARENT);
        //ellipse.setStroke(Color.RED);
        this.collisionShapes.add(ellipse);

    }

    private <I extends Interactable> void createInteractableRectangle(int x, int y, int width, int height, int angle, I interactable) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setRotate(angle);
        rect.setFill(Color.TRANSPARENT);
        //rect.setStroke(Color.BLUE); // debugging

        if(interactable instanceof Vent) this.ventShapes.put(rect, interactable);
        else this.interactableShapes.put(rect, interactable);
    }

    private <I extends Interactable> void createInteractableCircle(int x, int y, int radiusX, int radiusY, I interactable) {

        Ellipse ellipse = new Ellipse(x, y, radiusX, radiusY);
        ellipse.setFill(Color.TRANSPARENT);
        //ellipse.setStroke(Color.BLUE);

        this.interactableShapes.put(ellipse, interactable);

    }
}