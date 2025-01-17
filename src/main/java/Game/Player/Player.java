package Game.Player;

import Game.GameCharacter.CharacterView;
import Game.GameController;
import Game.GameMap.GameMap;
import Server.ClientUpdate;
import Server.Request;
import Server.Response;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.net.URI;
import java.util.Arrays;

public class Player {

    private final int SPEED = 650;
    private boolean wDown, aDown, sDown, dDown;

    private GameController controller;

    private PlayerView playerView;
    private CharacterView characterView;

    private final BooleanProperty canKill = new SimpleBooleanProperty(false);
    private final BooleanProperty canReport = new SimpleBooleanProperty(false);

    private PlayerInfo playerInfo;
    private final String name;

    private Space playerSpace;
    private final Space serverSpace;

    private final URI serverURI;


    public PlayerInfo getInfo() { return playerInfo; }
    public PlayerView getPlayerView() { return playerView; }
    public CharacterView getCharacterView() { return characterView; }
    public Space getPlayerSpace() { return playerSpace; }

    public void setController(GameController controller) { this.controller = controller; }


    public Player(String name, Space serverSpace, URI serverURI){

        this.name = name;

        this.serverSpace = serverSpace;
        this.serverURI = serverURI;
    }

    public void join() {
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

                    break;
                }
                case CONFLICT:{
                    System.out.println("Player name already exists! Pick another name.");
                    break;
                }
                case PERMISSION_DENIED:{
                    System.out.println("Permission denied to join server!");
                    break;
                }
                case ERROR:
                case FAILURE:{
                    System.out.println("Server does not exist!");
                    break;
                }
            }


        }catch (Exception e){
            System.out.println("Error in join");
            System.out.println(e.getMessage());
        }

    }

    // Aka start game (after joining)
    public void init(){
        try{
            Object[] playerInfo = playerSpace.get(new ActualField(ServerUpdate.PLAYER_INIT), new FormalField(PlayerInfo.class));
            PlayerInfo info = (PlayerInfo) playerInfo[1];

            this.playerInfo = info;
            this.playerView = new PlayerView(
                    info,
                    new BooleanProperty[]{ // order is: use, map, report, kill, sabotage
                            new SimpleBooleanProperty(true),
                            new SimpleBooleanProperty(true),
                            canReport,
                            canKill,
                            new SimpleBooleanProperty(true)
                    },
                    new Runnable[]{ // order is: use, map, report, kill, sabotage
                            this::onUseClicked,
                            this::onMapClicked,
                            this::onReportClicked,
                            this::onKillClicked,
                            this::onSabotageClicked
                    }
            );


            this.characterView = new CharacterView(name, info, info.isImposter ? Color.RED : Color.WHITE);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void onKilled() {
        playerInfo.isAlive = false;
        this.characterView.onKilled();
    }

    public void onUpdate(double delta, GameMap map){
        double newX = playerInfo.position[0] + playerInfo.velocity[0] * delta;
        double newY = playerInfo.position[1] + playerInfo.velocity[1] * delta;

        this.characterView.render(new double[]{newX, newY}, playerInfo.velocity);

        // If no collision then move player position
        if (!map.checkCollision(this.characterView)) {

            if(playerInfo.position[0] != newX || playerInfo.position[1] != newY){
                // push updated movement to server
                try{
                    playerSpace.put(ClientUpdate.POSITION);
                    playerSpace.put(ClientUpdate.POSITION, new double[]{newX, newY}, playerInfo.velocity);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }


            playerInfo.position[0] = newX;
            playerInfo.position[1] = newY;


        }else{
            this.characterView.render(playerInfo.position, playerInfo.velocity);

        }

        // set conditions for killing
        canKill.set(
            !(
                playerInfo.isAlive &&
                playerInfo.isImposter &&
                controller.getPlayerToKill(this.characterView) != null
            )
        );

        canReport.set(
            !(
                map.checkCollisionsWithBodies(this.characterView) &&
                playerInfo.isAlive
            )
        );
    }

    public void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case W: {
                wDown = true;
                break;
            }
            case A: {
                aDown = true;
                break;
            }
            case S: {
                sDown = true;
                break;
            }
            case D: {
                dDown = true;
                break;
            }
            case F1: { // Call meeting
                if(playerInfo.isAlive) {
                    try {
                        playerSpace.put(ClientUpdate.MEETING);
                        playerSpace.put(ClientUpdate.MEETING, name);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                return;
            }
        }

        updateVelocity();
    }

    public void handleKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case W: {
                wDown = false;
                break;
            }
            case A: {
                aDown = false;
                break;
            }
            case S: {
                sDown = false;
                break;
            }
            case D: {
                dDown = false;
                break;
            }
        }

        updateVelocity();
    }

    private void updateVelocity() {
        double dx = 0;
        double dy = 0;

        if (wDown) dy -= 1;
        if (sDown) dy += 1;
        if (aDown) dx -= 1;
        if (dDown) dx += 1;

        double length = Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            dx = dx / length * SPEED;
            dy = dy / length * SPEED;
        } else{

            // Notify that we have stopped moving! Only does once!
            Platform.runLater(() -> {
                try {
                    playerSpace.put(ClientUpdate.POSITION);
                    playerSpace.put(ClientUpdate.POSITION, playerInfo.position, playerInfo.velocity);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        playerInfo.velocity[0] = dx;
        playerInfo.velocity[1] = dy;
    }

    private void onUseClicked(){
        System.out.println("Use not implemented yet");
    }

    private void onMapClicked(){
        System.out.println("Map not implemented yet");
    }

    private void onReportClicked(){
        System.out.println("Report clicked");
    }

    private void onKillClicked(){
        System.out.println("Kill clicked");

        CharacterView playerKilled = controller.getPlayerToKill(this.characterView);

        if (playerKilled != null) {
            playerInfo.position = new double[]{playerKilled.getCenterX(), playerKilled.getCenterY()};
            this.characterView.render(playerInfo.position, playerInfo.velocity);

            try {
                playerSpace.put(ClientUpdate.POSITION);
                playerSpace.put(ClientUpdate.POSITION, playerInfo.position, playerInfo.velocity);

                playerSpace.put(ClientUpdate.KILL);
                playerSpace.put(ClientUpdate.KILL, name, playerKilled.getName());
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void onSabotageClicked(){
        System.out.println("Sabotage not implemented yet");
    }
}
