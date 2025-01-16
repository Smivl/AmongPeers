package Game.GameCharacter;

import Game.GameMap.GameMap;
import Game.Player.PlayerInfo;
import Server.ClientUpdate;
import Server.Request;
import Server.Response;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import javafx.scene.input.KeyEvent;
import org.jspace.Space;

import java.net.URI;
import java.util.Arrays;

public class GameCharacter {

    private final int SPEED = 650;
    private boolean wDown, aDown, sDown, dDown;

    private GameCharacterView view;
    private GameMap map;

    private double[] position;
    private double[] velocity;

    private String name;
    private boolean isAlive;

    private Space playerSpace;
    private Space serverSpace;

    private URI serverURI;

    public GameCharacterView getView() {
        return this.view;
    }
    public Space getPlayerSpace() { return this.playerSpace; }
    public boolean getIsAlive() { return this.isAlive; }

    public void setServerSpace(Space space) { this.serverSpace = space; }
    public void setServerURI(URI uri) { this.serverURI = uri; }

    public void setMap(GameMap map) { this.map = map; }

    public GameCharacter(String name) {

        this.name = name;
        this.velocity = new double[]{0.0, 0.0};
        this.isAlive = true;
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

    // Call init before starting after join
    public void init(){
        try{
            Object[] playerInfo = playerSpace.get(new ActualField(ServerUpdate.PLAYER_INIT), new FormalField(PlayerInfo.class));
            PlayerInfo info = (PlayerInfo) playerInfo[1];

            this.position = info.position;

            this.view = new GameCharacterView(name, info.position[0], info.position[1], info.velocity,info.color, info.isAlive);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void onKilled(){
        this.isAlive = false;
        this.view.onKilled();
    }

    public void onUpdate(double delta) {

        double newX = this.position[0] + this.velocity[0] * delta;
        double newY = this.position[1] + this.velocity[1] * delta;

        this.view.render(newX, newY, velocity);

        // If no collision then move player position
        if (!map.checkCollision(this.view)) {

            if(this.position[0] != newX || this.position[1] != newY){
                // push updated movement to server
                try{
                    playerSpace.put(ClientUpdate.POSITION);
                    playerSpace.put(ClientUpdate.POSITION, new double[]{newX, newY}, this.velocity);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }


            this.position[0] = newX;
            this.position[1] = newY;


        }else{
            this.view.render(this.position[0], this.position[1], this.velocity);



        }
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
            } case F:{
                if(isAlive) {
                    GameCharacterView playerKilled = map.getPlayerToKill(this.view);

                    if (playerKilled != null) {
                        this.position = new double[]{playerKilled.getCenterX(), playerKilled.getCenterY()};
                        this.view.render(playerKilled.getCenterX(), playerKilled.getCenterY(), this.velocity);

                        try {
                            playerSpace.put(ClientUpdate.POSITION);
                            playerSpace.put(ClientUpdate.POSITION, new double[]{this.position[0], this.position[1]}, this.velocity);

                            playerSpace.put(ClientUpdate.KILL);
                            playerSpace.put(ClientUpdate.KILL, name, playerKilled.getName());
                        } catch (Exception e) {
                            System.out.println(Arrays.toString(e.getStackTrace()));
                        }
                    }
                }
                return;

            }
            case F1: {
                if(isAlive) {
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
                    playerSpace.put(ClientUpdate.POSITION, this.position, this.velocity);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        this.velocity[0] = dx;
        this.velocity[1] = dy;
    }



}
