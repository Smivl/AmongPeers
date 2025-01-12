import Map.GameMap;
import PlayerM.Player;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.*;

import PlayerM.PlayerView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jspace.*;
import utils.*;

import javax.sound.midi.Soundbank;

public class Main extends Application {

    private static boolean isServer;

    private static GameMap map;
    private static Player player;

    private Map<String, PlayerView> otherPlayers = new HashMap<String, PlayerView>(){};

    private static Map<String, Space> playerSpaces = new HashMap<>();
    private static Map<String, PlayerInfo> playerInfos = new HashMap<>();

    private static SpaceRepository spaceRepository = new SpaceRepository();
    private static Space serverSpace;
    private static URI serverURI;

    private long previousFrameTime = 0;

    public static void main(String[] args) {

        ColorAdapter.init();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Are you hosting? (y/n)");

        // Player is hosting
        if (Objects.equals(scanner.nextLine(), "y")){

            isServer = true;

            System.out.println("Please insert URI of the server (enter for default):");
            String uri = scanner.nextLine();
            if (uri.isEmpty()){
                uri = "tcp://localhost:9001/?keep";
            }


            // Open connection to server
            try{
                // SETUP SERVER
                serverSpace = new SequentialSpace();
                spaceRepository.add("server", serverSpace);

                serverURI = new URI(uri);
                spaceRepository.addGate(serverURI);

                new Thread(Main::server).start();

                System.out.println("Server now online:\nAddress: " + serverURI);

            }catch(Exception e){
                System.out.println(e.getMessage());
                return;
            }

        }
        else{

            isServer = false;

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

        // Create player
        player = new Player(name);
        player.setServerSpace(serverSpace);
        player.setServerURI(serverURI);

        // Get player to join server and then initialize the player
        player.join();
        player.init();
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
        map = new GameMap(scene);


        // Add player to map and add map to root
        map.getView().getChildren().add(player.getView());
        root.getChildren().add(map.getView());

        new Thread(this::serverUpdates).start();
        /*
        Task<Void> serverUpdates = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true){
                    Object[] update = player.getPlayerSpace().get(new FormalField(ServerUpdate.class));

                    switch ((ServerUpdate) update[0]){
                        case POSITION:{
                            Object[] newPosition = player.getPlayerSpace().get(new ActualField(ServerUpdate.POSITION), new FormalField(String.class), new FormalField(Object.class));
                            Platform.runLater(() -> map.handlePositionUpdate((String) newPosition[1], (double[]) newPosition[2]));
                            break;
                        }case PLAYER_JOINED:{
                            Object[] newPlayer = player.getPlayerSpace().get(new ActualField(ServerUpdate.PLAYER_JOINED), new FormalField(String.class), new FormalField(PlayerInfo.class));
                            Platform.runLater(() -> map.handlePlayerJoin((String) newPlayer[1], (PlayerInfo) newPlayer[2]));
                            break;
                        }case PLAYER_LEFT:{
                            System.out.println("Player left!");
                            break;
                        }case PLAYER_INIT:{
                            System.out.println("ERROR: got PLAYER_INIT after player has been initialized!");
                            break;
                        }
                    }
                }
            }
        };

        new Thread(serverUpdates).start();

         */

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

    @Override
    public void stop(){

        spaceRepository.shutDown();

        System.exit(0);
    }

    // CLIENT SIDE
    private void serverUpdates(){
        while (true){
            try {
                Object[] update = player.getPlayerSpace().get(new FormalField(ServerUpdate.class));

                switch ((ServerUpdate) update[0]) {
                    case POSITION: {
                        Object[] newPosition = player.getPlayerSpace().get(new ActualField(ServerUpdate.POSITION), new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));
                        Platform.runLater(() -> map.handlePositionUpdate((String) newPosition[1], (double[]) newPosition[2], (double[]) newPosition[3]));
                        break;
                    }
                    case PLAYER_JOINED: {
                        Object[] newPlayer = player.getPlayerSpace().get(new ActualField(ServerUpdate.PLAYER_JOINED), new FormalField(String.class), new FormalField(PlayerInfo.class));
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



    // SERVER SIDE
    private static void server(){
        while (true){
            try {
                Object[] request = serverSpace.get(new FormalField(Request.class));
                switch ((Request) request[0]){
                    case JOIN: {
                        handleJoinRequest();
                        break;
                    }
                    case LEAVE:
                    case KICK: {
                        System.out.println("Unsupported request!");
                        break;
                    }
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private static void handleJoinRequest() {
        try {
            // read request
            String nameRequest = (String)(serverSpace.get(new ActualField(Request.JOIN), new FormalField(String.class))[1]);

            if (playerSpaces.containsKey(nameRequest)){
                // player name exists already
                serverSpace.put(nameRequest, Response.CONFLICT);
            }
            else {
                // player name does not exist

                // create new channel
                Space privateChannel = new SequentialSpace();
                playerSpaces.put(nameRequest, privateChannel);
                spaceRepository.add(nameRequest, privateChannel);

                new Thread(() -> handlePlayer(nameRequest)).start();

                // accept request
                serverSpace.put(nameRequest, Response.ACCEPTED);

                initializePlayer(nameRequest);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void initializePlayer(String nameRequest){
        try{

            PlayerInfo newPlayerInfo = new PlayerInfo(Color.GREEN, new double[]{4900, 1500}, new double[]{0,0});
            playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_INIT, newPlayerInfo);

            for (String playerName : playerInfos.keySet()){
                playerSpaces.get(playerName).put(ServerUpdate.PLAYER_JOINED);
                playerSpaces.get(playerName).put(ServerUpdate.PLAYER_JOINED, nameRequest, newPlayerInfo);

                playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_JOINED);
                playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_JOINED, playerName, playerInfos.get(playerName));
            }

            playerInfos.put(nameRequest, newPlayerInfo);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static void handlePlayer(String playerName) {
        while (true){
            try{

                /*
                    Note:
                        It could be a good idea to run broadcastUpdate after the switch case (to show every player the new update).
                        Then use the switch case to perform server actions based on the update (for example to handle leaving of a player).
                 */

                Object[] update = playerSpaces.get(playerName).get(new ActualField("SERVER"), new FormalField(ClientUpdate.class));
                switch ((ClientUpdate) update[1]){
                    case POSITION:{
                        broadcastClientUpdate(playerName);
                        break;
                    }
                }


            }catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    // todo: Make this more modular so it can broadcast all types of updates
    private static void broadcastClientUpdate(String playerName){
        try {
            Object[] update = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.POSITION), new FormalField(Object.class), new FormalField(Object.class));

            playerInfos.get(playerName).position = (double[]) update[1];
            playerInfos.get(playerName).velocity = (double[]) update[2];

            for(String name : playerSpaces.keySet()){
                if (!name.equals(playerName)){
                    playerSpaces.get(name).put(ServerUpdate.POSITION);
                    playerSpaces.get(name).put(ServerUpdate.POSITION, playerName, update[1], update[2]);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        player.handleKeyReleased(event);
    }

    private void handleKeyPressed(KeyEvent event) {
        player.handleKeyPressed(event);
    }
}
