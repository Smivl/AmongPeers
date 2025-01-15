package Game;

import Game.GameMap.GameMap;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import Server.ClientUpdate;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Objects;
import java.util.Stack;

public class GameController {

    private Player player;
    private GameMap map;
    private boolean inMeeting = false;

    private Thread serverUpdateThread;
    private String name;
    private Space serverspace;

    public GameController(String name, Space serverSpace, URI uri){
        this.name = name;
        player = new Player(name, serverSpace, uri);
    }

    public void start(Scene scene){
        map = new GameMap(scene);

        player.getCharacter().setMap(map);


        // Add player to map and add map to root
        map.getView().getChildren().add(player.getCharacter().getView());

        if (scene.getRoot() instanceof StackPane){
            StackPane root = (StackPane) scene.getRoot();
            root.getChildren().addAll(map.getView(), player.getPlayerView());

            player.getPlayerView().prefWidthProperty().bind(root.widthProperty());
            player.getPlayerView().prefHeightProperty().bind(root.heightProperty());
        }

        serverUpdateThread = new Thread(this::serverUpdates);
        serverUpdateThread.start();
    }

    public void onUpdate(double delta){

        player.onUpdate(delta, map);
        map.onUpdate(delta);

        map.getView().render(player.getCharacter().getView());

    }


    // CLIENT SIDE
    private void serverUpdates(){
        while (true){
            try {
                Object[] update = player.getCharacter().getPlayerSpace().get(new FormalField(ServerUpdate.class));

                switch ((ServerUpdate) update[0]) {
                    case POSITION: {
                        Object[] newPosition = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.POSITION), new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));
                        Platform.runLater(() -> map.handlePositionUpdate((String) newPosition[1], (double[]) newPosition[2], (double[]) newPosition[3]));
                        break;
                    }
                    case PLAYER_JOINED: {
                        Object[] newPlayer = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.PLAYER_JOINED), new FormalField(String.class), new FormalField(PlayerInfo.class));
                        Platform.runLater(() -> map.handlePlayerJoin((String) newPlayer[1], (PlayerInfo) newPlayer[2]));
                        break;
                    }
                    case KILLED: {
                        Object[] killedInfo = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.KILLED), new FormalField(String.class));

                        String playerKilled = (String) killedInfo[1];

                        if(playerKilled.equals(name)) player.getCharacter().onKilled();

                        Platform.runLater(() -> map.handlePlayerKilled(playerKilled, player.getCharacter().getView().getIsAlive()));
                        break;
                    }
                    case MEETING_START: {
                        inMeeting = true;
                        Object[] caller = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.MEETING_START), new FormalField(String.class));
                        String callerName = (String) caller[1];
                        System.out.println(callerName + " requested chat. Start chatting...");
                        new Thread(() -> {
                                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                                while(inMeeting){
                                    try {
                                        String message = input.readLine();
                                        player.getCharacter().getPlayerSpace().put(ClientUpdate.MESSAGE);
                                        player.getCharacter().getPlayerSpace().put(ClientUpdate.MESSAGE, message);
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }).start();
                        break;
                    }
                    case MESSAGE: {
                        Object[] message = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.MESSAGE), new FormalField(String.class), new FormalField(String.class));
                        System.out.println(message[1] + " : " + message[2]);
                        break;
                    }
                    case MEETING_DONE: {
                        inMeeting = false;

                        Object[] t = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.MEETING_DONE), new FormalField(String.class));
                        if (Objects.equals((String) t[1], "NO_ELIMINATION")){
                            System.out.println("No one was eliminated");
                        } else {
                            System.out.println(t[1] + " was eliminated");
                        }
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
                    case VOTE: {
                        System.out.println("ERROR: vote not implemented yet");
                        break;
                    }
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    public void handleKeyReleased(KeyEvent event) {
        player.getCharacter().handleKeyReleased(event);
    }

    public void handleKeyPressed(KeyEvent event) {
        player.getCharacter().handleKeyPressed(event);
    }
}
