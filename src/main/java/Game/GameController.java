package Game;

import Game.GameMap.GameMap;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.net.URI;

public class GameController {

    private Player player;
    private GameMap map;

    private Thread serverUpdateThread;

    public GameController(String name, Space serverSpace, URI uri){
        player = new Player(name, serverSpace, uri);
    }

    public void start(Scene scene){
        map = new GameMap(scene);

        // Add player to map and add map to root
        map.getView().getChildren().add(player.getCharacter().getView());

        if (scene.getRoot() instanceof Pane){
            ((Pane) scene.getRoot()).getChildren().add(map.getView());
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

    public void handleKeyReleased(KeyEvent event) {
        player.getCharacter().handleKeyReleased(event);
    }

    public void handleKeyPressed(KeyEvent event) {
        player.getCharacter().handleKeyPressed(event);
    }
}
