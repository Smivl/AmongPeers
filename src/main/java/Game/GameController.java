package Game;

import Game.GameMap.GameMap;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Meeting.MeetingView;
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

import java.net.URI;
import java.util.Objects;

public class GameController {

    private Player player;
    private GameMap map;
    private MeetingView meetingView;
    private boolean inMeeting = false;

    private Thread serverUpdateThread;
    private Space serverspace;

    public GameController(String name, Space serverSpace, URI uri){
        player = new Player(name, serverSpace, uri);
    }

    public void start(Scene scene){
        map = new GameMap(scene);
        meetingView = new MeetingView(scene,
                message -> {
                    try {
                        player.getCharacter().getPlayerSpace().put(ClientUpdate.MESSAGE);
                        player.getCharacter().getPlayerSpace().put(ClientUpdate.MESSAGE, message);;
                    } catch (Exception e){
                        e.printStackTrace(System.out);
                    }
                    return null;
                });

        // Add player to map and add map to root
        map.getView().getChildren().add(player.getCharacter().getView());

        ((StackPane)scene.getRoot()).getChildren().add(map.getView());
        ((StackPane) scene.getRoot()).getChildren().add(meetingView);

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
                    case MEETING_START: {
                        // inMeeting = true;
                        Object[] caller = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.MEETING_START), new FormalField(String.class));
                        String callerName = (String) caller[1];
                        System.out.println(callerName + " requested chat. Start chatting...");
//                        new Thread(() -> {
//                                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//                                while(inMeeting){
//                                    try {
//                                        String message = input.readLine();
//                                        player.getCharacter().getPlayerSpace().put(ClientUpdate.MESSAGE);
//                                        player.getCharacter().getPlayerSpace().put(ClientUpdate.MESSAGE, message);
//                                    } catch (IOException | InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }
//                            }).start();
                        Platform.runLater(() -> meetingView.show());
                        break;
                    }
                    case MESSAGE: {
                        Object[] message = player.getCharacter().getPlayerSpace().get(new ActualField(ServerUpdate.MESSAGE), new FormalField(String.class), new FormalField(String.class));
                        // System.out.println(message[1] + " : " + message[2]);
                        Platform.runLater(() -> meetingView.addMessage((String) message[1],(String) message[2], Color.BLUE));
                        break;
                    }
                    case MEETING_DONE: {
                        // inMeeting = false;
                        Platform.runLater(() -> meetingView.hide());
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
