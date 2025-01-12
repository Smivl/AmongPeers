import Map.GameMap;
import PlayerM.Player;

import java.net.URI;
import java.util.*;

import PlayerM.PlayerView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

public class Main extends Application {

    private GameMap map;
    private Player player;

    private Map<String, PlayerView> otherPlayers = new HashMap<String, PlayerView>(){};

    private Map<String, Space> playerSpaces = new HashMap<>();
    private static SpaceRepository spaceRepository = new SpaceRepository();
    private static SequentialSpace serverSpace;

    private long previousFrameTime = 0;

    public static void main(String[] args) {

        // SERVER STUFF
        Scanner scanner = new Scanner(System.in);

        System.out.println("Are you hosting? (y/n)");

        // Player is hosting
        if (Objects.equals(scanner.nextLine(), "y")){
            // SETUP SERVER
            serverSpace = new SequentialSpace();
            spaceRepository.add("server", serverSpace);

            System.out.println("Please insert URI of the server (enter for default):");
            String uri = scanner.nextLine();
            if (uri.isEmpty()){
                uri = "tcp://localhost:9001/?keep";
            }

            try{
                URI serverURI = new URI(uri);
                spaceRepository.addGate(serverURI);
                System.out.println("Server now online:\nAddress: " + serverURI);

            }catch(Exception e){
                System.out.println(e.getMessage());
            }

        }else{
            // JOIN SERVER
            System.out.println("Please insert URI of the server you want to join (enter for default):");
            String uri = scanner.nextLine();
            if (uri.isEmpty()){
                uri = "tcp://localhost:9001/?keep";
            }

            System.out.println("Please enter your username:");
            String name = scanner.nextLine();

            System.out.println("Joining server.");
        }





        launch(args);
    }

    public void start(Stage primaryStage) {

        Pane root = new Pane();

        // Create Scene and set background to black
        int WIDTH = 1280;
        int HEIGHT = 720;

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.BLACK);

        this.map = new GameMap(scene);

        this.player = new Player(4900.0, 1500.0, Color.BEIGE);
        this.player.setServerSpace(serverSpace);

        System.out.println("Please enter your username:");
        String name = scanner.nextLine();





        // Add player to map and add map to root
        this.map.getView().getChildren().add(this.player.getView());
        root.getChildren().add(this.map.getView());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        primaryStage.setTitle("Among Peers");
        primaryStage.setScene(scene);
        primaryStage.show();

        AnimationTimer gameLoop = new AnimationTimer() {

            public void handle(long currentFrameTime) {
                if (previousFrameTime == 0) {
                    previousFrameTime = currentFrameTime;
                } else {
                    long delta_nano = currentFrameTime - previousFrameTime;
                    previousFrameTime = currentFrameTime;

                    double delta = (double)delta_nano / 1.0E9;

                    player.onUpdate(delta, map);
                    map.onUpdate(delta);

                    map.getView().render(player.getView());
                }
            }
        };
        gameLoop.start();
    }

    private void handleKeyReleased(KeyEvent event) {
        this.player.handleKeyReleased(event);
    }

    private void handleKeyPressed(KeyEvent event) {
        this.player.handleKeyPressed(event);
    }
}
