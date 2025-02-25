package Game;

import Game.GameCharacter.CharacterView;
import Game.GameMap.GameMap;
import Game.Interactables.Interactable;
import Game.Interactables.Sabotage.SabotageType;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import utils.Audios;
import utils.Endgame;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class GameController {

    // Frame limiter
    private static double LIGHTS_VISION = 250;
    private static double IMPOSTER_VISION = 950;
    private static double DEFAULT_VISION = 650;
    private static final int TARGET_FPS = 30; // Desired frames per second
    private static final long FRAME_INTERVAL = 1_000_000_000 / TARGET_FPS; // Frame interval in nanoseconds

    // game controls
    private final Map<String, CharacterView> otherPlayerViews = new HashMap<>();
    private final String name;
    private Player player;
    private GameMap map;
    private MeetingView meetingView;
    private long previousFrameTime = 0;
    private MediaPlayer backgroundNoise = Audios.BACKGROUND.getMediaPlayer(); {backgroundNoise.setCycleCount(MediaPlayer.INDEFINITE);}
    private Map<String, double[]> spawnPoints = new HashMap<>();


    private double visionRadius = DEFAULT_VISION;
    private Rectangle fovCover;

    // server controls
    private Space serverSpace;
    private Space playerSpace;
    private URI serverURI;
    private Thread serverUpdateThread;

    // flow control
    private boolean running;
    private AnimationTimer gameLoop;
    private Runnable backToMainMenu;
    private Scene scene;

    public Player getPlayer() {
        return player;
    }

    public GameController(String name, URI serverURI, Runnable backToMainMenu){
        this.name = name;
        this.serverURI = serverURI;
        this.backToMainMenu = backToMainMenu;
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
        this.scene = scene;
        running = true;

        player.init();
        map = new GameMap(scene, player.getInfo(), player.getTasks());

        meetingView = new MeetingView(scene, name, player.getInfo());
        meetingView.initialize(
                message -> {
                    try {
                        playerSpace.put(ClientUpdate.MESSAGE);
                        playerSpace.put(ClientUpdate.MESSAGE, message);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                    return null;
                },
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

        player.setController(this);
        player.setMap(map);

        // Add player to map and add map to root
        map.getView().getChildren().add(player.getCharacterView());


        StackPane root = new StackPane();
        scene.setRoot(root);

        if(player.getInfo().isImposter) visionRadius = IMPOSTER_VISION;

        fovCover = new Rectangle(scene.getWidth(), scene.getHeight());
        fovCover.widthProperty().bind(scene.widthProperty());
        fovCover.heightProperty().bind(scene.heightProperty());

        scene.widthProperty().addListener((e, o, n) ->{ updatePlayerVision(); });
        scene.heightProperty().addListener((e, o, n) ->{ updatePlayerVision(); });

        updatePlayerVision();

        root.getChildren().clear();
        root.getChildren().addAll(map.getView(), fovCover, player.getPlayerView(), meetingView);

        player.getPlayerView().prefWidthProperty().bind(root.widthProperty());
        player.getPlayerView().prefHeightProperty().bind(root.heightProperty());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        serverUpdateThread = new Thread(this::serverUpdates);
        serverUpdateThread.start();

        gameLoop = new AnimationTimer() {

            public void handle(long currentFrameTime) {
                if (previousFrameTime == 0) {
                    previousFrameTime = currentFrameTime;
                } else {
                    if (currentFrameTime - previousFrameTime >= FRAME_INTERVAL) {
                        long delta_nano = currentFrameTime - previousFrameTime;
                        previousFrameTime = currentFrameTime; // Update the last update time
                        double delta = (double)delta_nano / 1.0E9;
                        onUpdate(delta);
                    }
                }
            }
        };
        gameLoop.start();

        backgroundNoise.play();
    }

    public void onUpdate(double delta){

        player.onUpdate(delta);
        map.onUpdate(delta);

        map.getView().render(player.getCharacterView());

    }

    private void updatePlayerVision(){

        RadialGradient gradient = new RadialGradient(
                /* focusAngle  = */ 0,
                /* focusDistance = */ 0,
                /* centerX = */ scene.getWidth()/2,
                /* centerY = */ scene.getHeight()/2,
                /* radius  = */ visionRadius,
                /* proportional = */ false,
                /* cycleMethod  = */ CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(0.6, Color.color(0, 0, 0, 0.375)),   // fully transparent in center
                new Stop(1.0, Color.color(0, 0, 0, 1.0))   // and fully dark near the edge
        );
        fovCover.setFill(gradient);
    }

    // CLIENT SIDE
    private void serverUpdates(){
        while (running){
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

                        meetingView.killPlayer(playerKilled);

                        Platform.runLater(() -> handleKilledUpdate(playerKilled));
                        break;
                    }
                    case MEETING_START: {
                        Audios.MEETING.getMediaPlayer().play();
                        Object[] caller = playerSpace.get(new ActualField(ServerUpdate.MEETING_START), new FormalField(String.class));
                        String callerName = (String) caller[1];

                        player.setInputLocked(true);
                        Platform.runLater(() -> {
                            meetingView.show();
                            meetingView.addServerMessage(callerName + " requested requested a meeting.");
                            resetPlayerPositions();
                        });


                        break;
                    }
                    case MESSAGE: {
                        Object[] message = playerSpace.get(new ActualField(ServerUpdate.MESSAGE), new FormalField(String.class), new FormalField(String.class));
                        Platform.runLater(() -> meetingView.addMessage((String) message[1],(String) message[2]));
                        break;
                    }
                    case MEETING_DONE: {

                        Object[] t = playerSpace.get(new ActualField(ServerUpdate.MEETING_DONE), new FormalField(String.class));

                        if (Objects.equals((String) t[1], "NO_ELIMINATION")){
                            Platform.runLater(() -> meetingView.addServerMessage("No one was eliminated"));
                        } else {
                            System.out.println(t[1] + " was eliminated");
                            Platform.runLater(() -> meetingView.addServerMessage(t[1] + " was eliminated."));
                        }
                        handleVotedOffUpdate((String)t[1]);
                        Thread.sleep(3000);
                        Platform.runLater(() -> {
                            meetingView.hide();
                            resetPlayerPositions();
                        });
                        // reset players position

                        player.setInputLocked(false);
                        player.resetCooldowns();

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
                        String playerVoter = (String) (playerSpace.get(new ActualField(ServerUpdate.VOTE), new FormalField(String.class), new FormalField(String.class))[1]);
                        Platform.runLater(() -> meetingView.addServerMessage(playerVoter + " cast their vote..."));
                        break;
                    }
                    case GAME_START: {
                        player.setInputLocked(false);
                        player.resetCooldowns();
                        break;
                    }
                    case TASK_COMPLETE: {
                        Object[] task_complete = playerSpace.get(new ActualField(ServerUpdate.TASK_COMPLETE), new FormalField(String.class), new FormalField(Double.class));
                        Platform.runLater(() -> player.getPlayerView().setTaskProgressBar((double) task_complete[2]));
                        break;
                    }
                    case SABOTAGE_STARTED:{
                        Object[] sabotage = playerSpace.get(new ActualField(ServerUpdate.SABOTAGE_STARTED), new FormalField(String.class), new FormalField(SabotageType.class));

                        SabotageType sabotageType = (SabotageType) sabotage[2];

                        Space sabotageSpace  = new RemoteSpace(
                                serverURI.getScheme() + "://" +
                                        serverURI.getHost() + ":" +
                                        serverURI.getPort() + "/sabotage?" +
                                        serverURI.getQuery()
                        );

                        if(!player.getInfo().isImposter && sabotageType == SabotageType.LIGHTS){
                            visionRadius = LIGHTS_VISION;
                        }

                        Platform.runLater(() -> {
                            map.onSabotageStarted(sabotageType);
                            player.onSabotageStarted(sabotageSpace, sabotageType);
                            updatePlayerVision();
                        });
                        break;
                    }
                    case SABOTAGE_UPDATE:{
                        Object[] sabotageUpdateInfo = playerSpace.get(new ActualField(ServerUpdate.SABOTAGE_UPDATE), new FormalField(String.class), new FormalField(Integer.class));

                        Platform.runLater(() -> player.getPlayerView().sabotageUpdate((int)sabotageUpdateInfo[2]));
                        break;
                    }
                    case SABOTAGE_ENDED:{
                        playerSpace.get(new ActualField(ServerUpdate.SABOTAGE_ENDED), new FormalField(String.class));

                        if(!player.getInfo().isImposter){
                            visionRadius = DEFAULT_VISION;
                        }

                        Platform.runLater(() -> {
                            map.onSabotageEnded();
                            player.onSabotageEnded();
                            updatePlayerVision();
                        });

                        break;
                    }
                    case IMPOSTERS_WIN:{
                        GameOver(Endgame.IMPOSTER);
                        break;
                    }
                    case CREWMATES_WIN:{
                        GameOver(Endgame.CREWMATE);
                        break;
                    }
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
        characterView.setWalkingVolume(position, new double[]{player.getCharacterView().getCenterX(), player.getCharacterView().getCenterY()});
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
            player.getCharacterView().setKillingVolume(player.getInfo().position, player.getInfo().position);
            player.onKilled();
            map.onPlayerKilled(new double[]{player.getInfo().position[0], player.getInfo().position[1]});
            meetingView.onKilled();
            return;
        }

        meetingView.killPlayer(playerName);
        for(CharacterView characterView : otherPlayerViews.values()){
            if(characterView.getName().equals(playerName)){
                characterView.setKillingVolume(new double[]{characterView.getCenterX(), characterView.getCenterY()}, player.getInfo().position);
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

    public void leaveGame(){
        try {
            playerSpace.put(ClientUpdate.LEAVE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetPlayerPositions(){
        player.resetPosition();
        for(String player : otherPlayerViews.keySet()){
            otherPlayerViews.get(player).render(spawnPoints.get(player), new double[]{0,0});
        }

        // Clear bodies
        map.onReset();
    }

    private void GameOver(Endgame endgame){

        this.running = false;
        gameLoop.stop();

        GameOverMenu endMenu = new GameOverMenu(endgame.getMessage(), backToMainMenu);
        endMenu.getStyleClass().add("menu-box");
        scene.setRoot(endMenu);
        backgroundNoise.stop();
        endgame.getAudio().getMediaPlayer().play();
    }
}
