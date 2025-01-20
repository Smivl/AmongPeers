package Game;

import Game.GameCharacter.CharacterView;
import Game.GameMap.GameMap;
import Game.Interactables.Task.TaskType;
import Game.Player.Player;
import Game.Player.PlayerInfo;
import Game.Meeting.MeetingView;
import Server.ClientUpdate;
import Server.Request;
import Server.Response;
import Server.ServerUpdate;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class GameController {

    // game controls
    private final Map<String, CharacterView> otherPlayerViews = new HashMap<>();
    private final String name;
    private Player player;
    private GameMap map;
    private MeetingView meetingView;
    private long previousFrameTime = 0;

    private Map<String, double[]> spawnPoints = new HashMap<>();

    // server controls
    private Space serverSpace;
    private Space playerSpace;
    private URI serverURI;
    private Thread serverUpdateThread;

    public Player getPlayer() {
        return player;
    }

    public GameController(String name, URI serverURI){
        this.name = name;
        this.serverURI = serverURI;
        try {
            System.out.println(serverURI.getScheme() + "://" +
                    serverURI.getHost() + ":" +
                    serverURI.getPort() + "/" +
                    "server" + "?" +
                    serverURI.getQuery());
            this.serverSpace = new RemoteSpace(
                    serverURI.getScheme() + "://" +
                            serverURI.getHost() + ":" +
                            serverURI.getPort() + "/" +
                            "server" + "?" +
                            serverURI.getQuery());
        } catch (Exception e){
            e.printStackTrace(System.out);
        }
        // player.join();
        // player.init();
    }

    public Response join() {
        try{
            serverSpace.put(Request.JOIN);
            serverSpace.put(Request.JOIN, name);

            Object[] response = serverSpace.get(new ActualField(name), new FormalField(Response.class));

            switch ((Response) response[1]){
                case SUCCESS:
                case ACCEPTED:{
                    playerSpace = new RemoteSpace(
                            serverURI.getScheme() + "://" +
                                    serverURI.getHost() + ":" +
                                    serverURI.getPort() + "/" +
                                    name + "?" +
                                    serverURI.getQuery()
                    );
                    player = new Player(name, playerSpace);
                    playerSpace.put(ClientUpdate.READY_TO_START);
                    return (Response) response[1];
                }
                case CONFLICT:
                case PERMISSION_DENIED:
                case ERROR:
                case FAILURE:{
                    return (Response) response[1];
                }
            }
        }catch (Exception e){
            return Response.ERROR;
        }
        return null;
    }


    public void leave(){
        try{
            serverSpace.put(Request.LEAVE);
            serverSpace.put(Request.LEAVE, name);
        }catch (Exception e){
            System.out.println("Error in leave");
        }
    }

    public void waitForStart(Scene scene) throws InterruptedException {
        playerSpace.get(new ActualField(ServerUpdate.GAME_START));

        System.out.println("Game started");
        player.setInputLocked(false);

        Platform.runLater(() -> start(scene));
    }

    private void start(Scene scene) {
        player.init();
        map = new GameMap(scene, player.getInfo(), player.getTasks());

        meetingView = new MeetingView(scene);

        meetingView.addSendMessageFunction(
                message -> {
                    try {
                        playerSpace.put(ClientUpdate.MESSAGE);
                        playerSpace.put(ClientUpdate.MESSAGE, message);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                    return null;
                }
        );

        meetingView.addVoteForFunction(
                playerName -> {
                    try {
                        playerSpace.put(ClientUpdate.VOTE);
                        playerSpace.put(ClientUpdate.VOTE, playerName);
                    } catch (InterruptedException e){
                        e.printStackTrace(System.out);
                    }
                    return null;
                }
        );

        meetingView.initialize();

        player.setController(this);

        // Add player to map and add map to root
        map.getView().getChildren().add(player.getCharacterView());


        StackPane root = new StackPane();
        scene.setRoot(root);
        root.getChildren().clear();
        root.getChildren().addAll(map.getView(), player.getPlayerView(), meetingView);

        player.getPlayerView().prefWidthProperty().bind(root.widthProperty());
        player.getPlayerView().prefHeightProperty().bind(root.heightProperty());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        serverUpdateThread = new Thread(this::serverUpdates);
        serverUpdateThread.start();

        AnimationTimer gameLoop = new AnimationTimer() {

            public void handle(long currentFrameTime) {
                if (previousFrameTime == 0) {
                    previousFrameTime = currentFrameTime;
                } else {
                    long delta_nano = currentFrameTime - previousFrameTime;
                    previousFrameTime = currentFrameTime;

                    double delta = (double)delta_nano / 1.0E9;

                    onUpdate(delta);
                }
            }
        };
        gameLoop.start();
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
                Object[] update = playerSpace.get(new FormalField(ServerUpdate.class));
                switch ((ServerUpdate) update[0]) {
                    case POSITION: {
                        Object[] newPosition = playerSpace.get(new ActualField(ServerUpdate.POSITION), new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));

                        Platform.runLater(() -> handlePositionUpdate((String) newPosition[1], (double[]) newPosition[2], (double[]) newPosition[3]));
                        break;
                    }
                    case PLAYER_JOINED: {
                        Object[] newPlayerObject = playerSpace.get(
                                new ActualField(ServerUpdate.PLAYER_JOINED),
                                new FormalField(String.class),
                                new FormalField(PlayerInfo.class)
                        );

                        String newPlayerName = (String) newPlayerObject[1];
                        PlayerInfo newPlayerInfo = (PlayerInfo) newPlayerObject[2];

                        spawnPoints.put(newPlayerName, newPlayerInfo.position);

                        meetingView.addPlayersInfo(newPlayerName, newPlayerInfo);
                        Platform.runLater(() -> handleJoinedUpdate(newPlayerName, newPlayerInfo));
                        break;
                    }
                    case KILLED: {
                        Object[] killedInfo = playerSpace.get(new ActualField(ServerUpdate.KILLED), new FormalField(String.class));

                        String playerKilled = (String) killedInfo[1];

                        // meetingView.killPlayer(playerKilled); TO FIX WHEN PLAYER GETS KILLED

                        Platform.runLater(() -> handleKilledUpdate(playerKilled));
                        break;
                    }
                    case MEETING_START: {

                        Object[] caller = playerSpace.get(new ActualField(ServerUpdate.MEETING_START), new FormalField(String.class));
                        String callerName = (String) caller[1];
                        System.out.println(callerName + " requested chat. Start chatting...");
                        Platform.runLater(() -> {
                            meetingView.show();
                            resetPlayerPositions();
                        });

                        player.setInputLocked(true);
                        break;
                    }
                    case MESSAGE: {
                        Object[] message = playerSpace.get(new ActualField(ServerUpdate.MESSAGE), new FormalField(String.class), new FormalField(String.class));
                        Platform.runLater(() -> meetingView.addMessage((String) message[1],(String) message[2], Color.BLUE));
                        break;
                    }
                    case MEETING_DONE: {
                        Platform.runLater(() -> meetingView.hide());
                        Object[] t = playerSpace.get(new ActualField(ServerUpdate.MEETING_DONE), new FormalField(String.class));

                        if (Objects.equals((String) t[1], "NO_ELIMINATION")){
                            System.out.println("No one was eliminated");
                        } else {
                            System.out.println(t[1] + " was eliminated");
                            Platform.runLater(() -> handleVotedOffUpdate((String) t[1]));
                        }

                        // reset players position
                        player.setInputLocked(false);
                        break;
                    }
                    case PLAYER_LEFT: {
                        Object[] playerLeft = playerSpace.get(new ActualField(ServerUpdate.PLAYER_LEFT), new FormalField(String.class));

                        Platform.runLater(() -> handleLeftUpdate((String) playerLeft[1]));
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
                    case GAME_START: {
                        player.setInputLocked(false);
                        break;
                    }
                    case SABOTAGE:
                    case TASK_COMPLETE: break;
                }
            }catch (Exception e){
                e.printStackTrace(System.out);
                break;
            }
        }
    }

    public CharacterView getPlayerToKill(CharacterView killer){

        CharacterView result = null;
        double min_dist = 0;

        for(CharacterView player : otherPlayerViews.values()){

            if (player.getIsImposter() || !player.getIsAlive()) continue;

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

    public void handleVotedOffUpdate(String playerName){
        if(playerName.equals(name)) player.onKilled();

        for(CharacterView characterView : otherPlayerViews.values()){
            if(characterView.getName().equals(playerName)) characterView.onKilled();
            characterView.setVisible(!player.getInfo().isAlive || characterView.getIsAlive());
        }
    }

    public void handleKilledUpdate(String playerName){
        if(playerName.equals(name)) {
            player.onKilled();
            map.onPlayerKilled(new double[]{player.getInfo().position[0], player.getInfo().position[1]});
        }

        for(CharacterView characterView : otherPlayerViews.values()){
            if(characterView.getName().equals(playerName)){
                characterView.onKilled();
                map.onPlayerKilled(new double[]{characterView.getCenterX(), characterView.getCenterY()});
            }

            characterView.setVisible(!player.getInfo().isAlive || characterView.getIsAlive());
        }
    }

    public void handleJoinedUpdate(String newPlayerName, PlayerInfo newPlayerInfo) {
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

    public void handleLeftUpdate(String playerName){
        map.removePlayer(otherPlayerViews.get(playerName));
        otherPlayerViews.remove(playerName);
    }

    public void handleKeyReleased(KeyEvent event) {
        player.handleKeyReleased(event);
    }

    public void handleKeyPressed(KeyEvent event) {
        player.handleKeyPressed(event);
    }

    private void resetPlayerPositions(){
        System.out.println("reset Pos");
        player.resetPosition();
        for(String player : otherPlayerViews.keySet()){
            otherPlayerViews.get(player).render(spawnPoints.get(player), new double[]{0,0});
        }

        // Clear bodies
        map.onReset();
    }
}
