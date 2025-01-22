import Game.GameController;


import Menu.MenuManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.*;

public class Main extends Application {

    private static Stage stage;
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    public static MenuManager menuManager;

    public static void main(String[] args) {
        ColorAdapter.init();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initial menu
        Main.stage = primaryStage;
        stage.setTitle("Among Peers");

        Scene scene = new Scene(new StackPane(), WIDTH, HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("/stylesheet.css").toExternalForm());
        scene.setFill(Color.BLACK);

        menuManager = new MenuManager(scene);
        menuManager.transitionToMainMenu();

        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void stop(){
        //server.shutdown();
        System.exit(0);
    }
}
