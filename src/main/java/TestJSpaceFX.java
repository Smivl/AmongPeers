import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.jspace.*;

import javafx.application.Application;
import javafx.event.*;
import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.security.Key;
import java.util.*;
import java.util.List;


public class TestJSpaceFX extends Application {

    public static Space positionSpace;

    public static void main(String[] args) throws InterruptedException {

        SpaceRepository repo = new SpaceRepository();
        positionSpace = new SequentialSpace();
        repo.add("position", positionSpace);
        repo.addGate("tcp://localhost:9001/?keep");

        launch(args);
    }


    final private int WIDTH = 500;
    final private int HEIGHT = 500;

    private int counter = 0;
    private double[] position = new double[]{(double)WIDTH/2,(double) HEIGHT/2};
    private List<Double> velocity = new ArrayList<>(Arrays.asList(0.0,0.0));

    final private Circle circle = new Circle();
    final private Circle player2 = new Circle();

    final private Button button = new Button();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Among Peers");

        new Thread(this::handleOtherPlayer).start();

        this.button.setText("Im a counter! Click ME!!!");
        this.button.setOnAction(this::handleClick);

        Group root = new Group();
        root.getChildren().add(this.circle);
        this.circle.setRadius(50);
        this.circle.setCenterX(this.position[0]);
        this.circle.setCenterY(this.position[1]);
        this.circle.setFill(Color.BLACK);

        root.getChildren().add(this.player2);
        this.player2.setRadius(50);
        this.player2.setCenterX(0);
        this.player2.setCenterY(0);
        this.player2.setFill(Color.GREEN);


        Timeline t = new Timeline(new KeyFrame(Duration.millis(16), e-> {
            this.circle.setCenterX(this.circle.getCenterX() + this.velocity.get(0) * 2);
            this.circle.setCenterY(this.circle.getCenterY() + this.velocity.get(1) * 2);

            if (!Arrays.equals(this.position, new double[]{this.circle.getCenterX(), this.circle.getCenterY()})){
                try {
                    this.position = new double[]{this.circle.getCenterX(), this.circle.getCenterY()};
                    positionSpace.put("Server", this.position[0], this.position[1]);
                } catch (Exception ec) {
                    System.out.println(ec.getMessage());
                }
            }

        }));

        t.setCycleCount(Timeline.INDEFINITE);
        t.play();

        //StackPane root = new StackPane();
        //root.getChildren().add(this.button);

        // Create scene
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void handleClick(ActionEvent event) {
        this.counter++;
        this.button.setText("" + this.counter);
    }

    private void handleKeyReleased(KeyEvent event) {


        // EDIT VELOCITY
        if (event.getCode() == KeyCode.W) {
            this.velocity.set(1, 0.0);
        }
        if (event.getCode() == KeyCode.A) {
            this.velocity.set(0, 0.0);
        }
        if (event.getCode() == KeyCode.S) {
            this.velocity.set(1, 0.0);
        }
        if (event.getCode() == KeyCode.D) {
            this.velocity.set(0, 0.0);
        }

    }
    private void handleKeyPressed(KeyEvent event) {


        // EDIT VELOCITY
        if (event.getCode() == KeyCode.W) {
            this.velocity.set(1, -1.0);
        }
        if (event.getCode() == KeyCode.A) {
            this.velocity.set(0, -1.0);
        }
        if (event.getCode() == KeyCode.S) {
            this.velocity.set(1, 1.0);
        }
        if (event.getCode() == KeyCode.D) {
            this.velocity.set(0, 1.0);
        }

    }

    private void handleOtherPlayer(){
        while(true){
            try{
                Object[] res = positionSpace.get(new ActualField("Client"), new FormalField(Double.class), new FormalField(Double.class));
                double x = (double)res[1];
                double y = (double)res[2];

                this.player2.setCenterX(x);
                this.player2.setCenterY(y);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}