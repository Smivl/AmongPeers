import Game.GameController;
import Game.GameMap.GameMap;
import Game.GameCharacter.GameCharacter;

import java.net.URI;
import java.util.*;

import Game.GameCharacter.GameCharacterView;
import Game.Player.PlayerInfo;
import Server.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jspace.*;
import utils.*;

public class Main extends Application {


    private static GameController gameController;
    private static Server server;

    private static Space serverSpace;
    private static URI serverURI;

    private long previousFrameTime = 0;

    public static void main(String[] args) {

        ColorAdapter.init();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Are you hosting? (y/n)");

        // Player is hosting
        if (Objects.equals(scanner.nextLine(), "y")){


            // Create new SERVER

            System.out.println("Please insert URI of the server (enter for default):");
            String uri = scanner.nextLine();
            if (uri.isEmpty()){
                uri = "tcp://localhost:9001/?keep";
            }


            // Open connection to server
            try{
                // SETUP SERVER
                serverSpace = new SequentialSpace();
                serverURI = new URI(uri);

                server = new Server(serverURI, serverSpace);
                server.start();

            }catch(Exception e){
                System.out.println(Arrays.toString(e.getStackTrace()));
                return;
            }

        }
        else{

            // JOIN SERVER
            System.out.println("Please insert URI of the server you want to join (enter for default):");
            String uri = scanner.nextLine();
            if (uri.isEmpty()){
                uri = "tcp://localhost:9001/?keep";
            }

            // Connect to server
            try{

                serverURI = new URI(uri);
                serverSpace = new RemoteSpace(
                        serverURI.getScheme() + "://" +
                            serverURI.getHost() + ":" +
                            serverURI.getPort() + "/" +
                            "server" + "?" +
                            serverURI.getQuery()
                );

            }catch (Exception e) {
                System.out.println(e.getMessage());
            }

            System.out.println("Connected to server.");
        }

        // Get username
        System.out.println("Please enter your username:");
        String name = scanner.nextLine();

        gameController = new GameController(name, serverSpace, serverURI);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Pane root = new Pane();

        // Create Scene and set background to black
        int WIDTH = 1280;
        int HEIGHT = 720;

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.BLACK);

        gameController.start(scene);

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

                    gameController.onUpdate(delta);
                }
            }
        };
        gameLoop.start();
    }

    @Override
    public void stop(){
        server.shutdown();
        System.exit(0);
    }

    /*
    // CLIENT SIDE
    private void serverUpdates(){
        while (true){
            try {
                Object[] update = character.getPlayerSpace().get(new FormalField(ServerUpdate.class));

                switch ((ServerUpdate) update[0]) {
                    case POSITION: {
                        Object[] newPosition = character.getPlayerSpace().get(new ActualField(ServerUpdate.POSITION), new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));
                        Platform.runLater(() -> map.handlePositionUpdate((String) newPosition[1], (double[]) newPosition[2], (double[]) newPosition[3]));
                        break;
                    }
                    case PLAYER_JOINED: {
                        Object[] newPlayer = character.getPlayerSpace().get(new ActualField(ServerUpdate.PLAYER_JOINED), new FormalField(String.class), new FormalField(PlayerInfo.class));
                        Platform.runLater(() -> map.handlePlayerJoin((String) newPlayer[1], (PlayerInfo) newPlayer[2]));
                        break;
                    }
                    case PLAYER_LEFT: {
                        System.out.println("Player left!");
                        break;
                    }
                    case PLAYER_INIT: {
                        System.out.println("ERROR: got PLAYER_INIT after player has been initialized!");
                        break;
                    }
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

     */


    private void handleKeyReleased(KeyEvent event) {
        gameController.handleKeyReleased(event);
    }

    private void handleKeyPressed(KeyEvent event) {
        gameController.handleKeyPressed(event);
    }
}
