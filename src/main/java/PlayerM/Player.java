package PlayerM;

import Map.GameMap;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import utils.*;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jspace.Space;

import javax.sound.midi.Soundbank;
import java.lang.annotation.Repeatable;
import java.net.URI;

public class Player {

    private final int SPEED = 400;
    private boolean wDown, aDown, sDown, dDown;

    private PlayerView view;

    private double[] position;
    private double[] velocity;

    private String name;

    private Space playerSpace;
    private Space serverSpace;

    private URI serverURI;

    public PlayerView getView() {
        return this.view;
    }
    public Space getPlayerSpace() { return this.playerSpace; }

    public void setServerSpace(Space space) { this.serverSpace = space; }
    public void setServerURI(URI uri) { this.serverURI = uri; }

    public Player(String name) {

        this.name = name;
        this.velocity = new double[]{0.0, 0.0};
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

            this.view = new PlayerView(name, info.position[0], info.position[1], info.velocity,info.color);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void onUpdate(double delta, GameMap map) {

        double newX = this.position[0] + this.velocity[0] * delta;
        double newY = this.position[1] + this.velocity[1] * delta;

        this.view.render(newX, newY, velocity);

        // If no collision then move player position
        if (!map.checkCollision(this.view)) {
            if(this.position[0] != newX || this.position[1] != newY){
                // push updated movement to server
                try{
                    playerSpace.put("SERVER", ClientUpdate.POSITION);
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
        }

        this.velocity[0] = dx;
        this.velocity[1] = dy;
    }




}
