package Game;

import Game.GameCharacter.CharacterView;
import Game.GameMap.GameMap;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import Server.ClientUpdate;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameController {

    private final Map<String, CharacterView> otherPlayerViews = new HashMap<>();

    private final Player player;
    private GameMap map;
    private boolean inMeeting = false;

    private Thread serverUpdateThread;
    private final String name;
    private Space serverspace;

    public GameController(String name, Space serverSpace, URI uri){
        this.name = name;

        player = new Player(name, serverSpace, uri);

        player.join();
        player.init();
    }

    public void start(Scene scene){
        map = new GameMap(scene);

        player.setController(this);

        // Add player to map and add map to root
        map.getView().getChildren().add(player.getCharacterView());

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

        map.getView().render(player.getCharacterView());

    }


    // CLIENT SIDE
    private void serverUpdates(){
        while (true){
            try {
                Object[] update = player.getPlayerSpace().get(new FormalField(ServerUpdate.class));

                switch ((ServerUpdate) update[0]) {
                    case POSITION: {
                        Object[] newPosition = player.getPlayerSpace().get(new ActualField(ServerUpdate.POSITION), new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));

                        Platform.runLater(() -> handlePositionUpdate((String) newPosition[1], (double[]) newPosition[2], (double[]) newPosition[3]));
                        break;
                    }
                    case PLAYER_JOINED: {
                        Object[] newPlayerObject = player.getPlayerSpace().get(
                                new ActualField(ServerUpdate.PLAYER_JOINED),
                                new FormalField(String.class),
                                new FormalField(PlayerInfo.class)
                        );

                        Platform.runLater(() -> handleJoinedUpdate((String) newPlayerObject[1], (PlayerInfo) newPlayerObject[2]));
                        break;
                    }
                    case KILLED: {
                        Object[] killedInfo = player.getPlayerSpace().get(new ActualField(ServerUpdate.KILLED), new FormalField(String.class));
                        Platform.runLater(() -> handleKilledUpdate((String) killedInfo[1]));
                        break;
                    }
                    case MEETING_START: {
                        inMeeting = true;
                        Object[] caller = player.getPlayerSpace().get(new ActualField(ServerUpdate.MEETING_START), new FormalField(String.class));
                        String callerName = (String) caller[1];
                        System.out.println(callerName + " requested chat. Start chatting...");
                        new Thread(() -> {
                                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                                while(inMeeting){
                                    try {
                                        String message = input.readLine();
                                        player.getPlayerSpace().put(ClientUpdate.MESSAGE);
                                        player.getPlayerSpace().put(ClientUpdate.MESSAGE, message);
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }).start();
                        break;
                    }
                    case MESSAGE: {
                        Object[] message = player.getPlayerSpace().get(new ActualField(ServerUpdate.MESSAGE), new FormalField(String.class), new FormalField(String.class));
                        System.out.println(message[1] + " : " + message[2]);
                        break;
                    }
                    case MEETING_DONE: {
                        inMeeting = false;

                        Object[] t = player.getPlayerSpace().get(new ActualField(ServerUpdate.MEETING_DONE), new FormalField(String.class));
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

    public CharacterView getPlayerToKill(CharacterView killer){

        CharacterView result = null;
        double min_dist = 0;

        for(CharacterView player : otherPlayerViews.values()){

            if (player.getIsImposter()) continue;

            double dist = Math.sqrt(Math.pow(killer.getCenterX()-player.getCenterX(), 2)+Math.pow(killer.getCenterY()-player.getCenterY(), 2));
            if(dist < 100 && (result == null || dist < min_dist)){
                result = player;
                min_dist = dist;
            }
        }

        return result;
    }

    public void handlePositionUpdate(String playerName, double[] position, double[] velocity){
        CharacterView characterView = otherPlayerViews.get(playerName);
        characterView.render(position, velocity);
    }

    public void handleKilledUpdate(String playerName){
        if(playerName.equals(name)) player.onKilled();

        for(CharacterView characterView : otherPlayerViews.values()){
            if(characterView.getName().equals(playerName)){
                characterView.onKilled();
            }

            characterView.setVisible(!player.getInfo().isAlive || characterView.getIsAlive());
        }

    }

    public void handleJoinedUpdate(String newPlayerName, PlayerInfo newPlayerInfo){
        PlayerInfo mainPlayerInfo = player.getInfo();

        CharacterView newPlayer = new CharacterView(
                newPlayerName,
                newPlayerInfo,
                (newPlayerInfo.isImposter && mainPlayerInfo.isImposter) ? Color.RED : Color.WHITE
        );

        otherPlayerViews.put(newPlayerName, newPlayer);

        // add player and set visibility
        map.addPlayer(newPlayer, !mainPlayerInfo.isAlive || newPlayer.getIsAlive());
    }

    public void handleKeyReleased(KeyEvent event) {
        player.handleKeyReleased(event);
    }

    public void handleKeyPressed(KeyEvent event) {
        player.handleKeyPressed(event);
    }
}
