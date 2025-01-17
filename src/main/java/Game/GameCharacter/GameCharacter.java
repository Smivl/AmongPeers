package Game.GameCharacter;

import Game.GameController;
import Game.GameMap.GameMap;
import Game.Player.PlayerInfo;
import Server.ClientUpdate;
import Server.Request;
import Server.Response;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
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

    private final GameCharacterView view;
    private GameMap map;
    private GameController controller;

    private final String name;
    private final PlayerInfo playerInfo;
    private Space playerSpace;

    public GameCharacterView getView() {
        return this.view;
    }
    public Space getPlayerSpace() { return this.playerSpace; }

    public void setPlayerSpace(Space playerSpace) { this.playerSpace = playerSpace; }
    public void setController(GameController gameController) { this.controller = gameController; }
    public void setMap(GameMap gameMap) { this.map = gameMap; }

    public GameCharacter(String name, PlayerInfo info) {

        this.name = name;
        this.playerInfo = info;

        this.view = new GameCharacterView(name, info, info.isImposter ? Color.RED : Color.WHITE);
    }

    public void onKilled(){
        playerInfo.isAlive = false;
        this.view.onKilled();
    }

    public void onUpdate(double delta) {

        double newX = playerInfo.position[0] + playerInfo.velocity[0] * delta;
        double newY = playerInfo.position[1] + playerInfo.velocity[1] * delta;

        this.view.render(new double[]{newX, newY}, playerInfo.velocity);

        // If no collision then move player position
        if (!map.checkCollision(this.view)) {

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
            this.view.render(playerInfo.position, playerInfo.velocity);

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
            case F:{ // Kill player
                if(playerInfo.isAlive && playerInfo.isImposter) {
                    GameCharacterView playerKilled = controller.getPlayerToKill(this.view);

                    if (playerKilled != null) {
                        playerInfo.position = new double[]{playerKilled.getCenterX(), playerKilled.getCenterY()};
                        this.view.render(playerInfo.position, playerInfo.velocity);

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
                return;
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

}
