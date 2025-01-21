package Game.Player;

import Game.GameCharacter.CharacterView;
import Game.GameController;
import Game.GameMap.GameMap;
import Game.Interactables.Interactable;
import Game.Interactables.Task.Task;
import Game.Interactables.Task.TaskType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Player {

    private final int SPEED = 650;
    private boolean inputLocked = true;
    private boolean wDown, aDown, sDown, dDown;

    private Interactable interactableInFocus = null;
    private Interactable ventInFocus = null;

    private GameController controller;

    private PlayerView playerView;
    private CharacterView characterView;

    private final BooleanProperty canKill = new SimpleBooleanProperty(false);
    private final BooleanProperty canReport = new SimpleBooleanProperty(false);
    private final BooleanProperty canInteract = new SimpleBooleanProperty(false);
    private final BooleanProperty canVent = new SimpleBooleanProperty(false);

    private TaskType[] taskList;
    private double[] spawnPoint;
    private PlayerInfo playerInfo;
    private final String name;

    private Space playerSpace;

    public TaskType[] getTasks() { return taskList; }
    public PlayerInfo getInfo() { return playerInfo; }
    public PlayerView getPlayerView() { return playerView; }
    public CharacterView getCharacterView() { return characterView; }
    public void setInputLocked(boolean lock) { this.inputLocked = lock; }
    public void setController(GameController controller) { this.controller = controller; }

    public Player(String name, Space playerSpace){

        this.name = name;
        this.playerSpace = playerSpace;
    }

    // Aka start game (after joining)
    public void init(){
        try{
            Object[] playerInfo = playerSpace.get(new ActualField(ServerUpdate.PLAYER_INIT), new FormalField(PlayerInfo.class), new FormalField(Object.class));
            PlayerInfo info = (PlayerInfo) playerInfo[1];

            taskList = (TaskType[]) playerInfo[2];

            this.spawnPoint = info.position.clone();
            this.playerInfo = info;
            this.playerView = new PlayerView(
                    info,
                    taskList,
                    new BooleanProperty[]{ // order is: interact, report, kill, sabotage, vent
                            canInteract,
                            canReport,
                            canKill,
                            new SimpleBooleanProperty(true),
                            canVent
                    },
                    new Runnable[]{ // order is: interact, map, report, kill, sabotage, vent, settings
                            this::onInteractClicked,
                            this::onMapClicked,
                            this::onReportClicked,
                            this::onKillClicked,
                            this::onSabotageClicked,
                            this::onVentClicked,
                            this::onSettingsClicked
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

        if(!inputLocked) {
            double newX = playerInfo.position[0] + playerInfo.velocity[0] * delta;
            double newY = playerInfo.position[1] + playerInfo.velocity[1] * delta;

            this.characterView.render(new double[]{newX, newY}, playerInfo.velocity);

            // If no collision then move player position
            if (!map.checkCollision(this.characterView)) {

                if (playerInfo.position[0] != newX || playerInfo.position[1] != newY) {
                    // push updated movement to server
                    try {
                        playerSpace.put(ClientUpdate.POSITION);
                        playerSpace.put(ClientUpdate.POSITION, new double[]{newX, newY}, playerInfo.velocity);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }


                playerInfo.position[0] = newX;
                playerInfo.position[1] = newY;


            } else {
                this.characterView.render(playerInfo.position, playerInfo.velocity);
            }

            canKill.set(
                    !(
                            playerInfo.isAlive &&
                                    playerInfo.isImposter &&
                                    controller.getPlayerToKill(this.characterView) != null
                    )
            );
            canReport.set(
                    !(
                            map.checkCollisionWithBodies(this.characterView) &&
                                    playerInfo.isAlive
                    )
            );
            interactableInFocus = map.getInteractable(this.characterView);
            canInteract.set(
                    !(
                            interactableInFocus != null &&
                            interactableInFocus.canInteract(playerInfo)
                    )
            );
            if (playerInfo.isImposter) {
                ventInFocus = map.getVent(this.characterView);
                canVent.set(
                        !(
                                ventInFocus != null &&
                                        ventInFocus.canInteract(playerInfo)
                        )
                );
            }
        }else{
            canKill.set(true);
            canReport.set(true);
            canInteract.set(true);
            canVent.set(true);
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

    public void resetPosition(){

        this.playerInfo.position = this.spawnPoint;
        this.playerInfo.velocity = new double[]{0.0, 0.0};

        this.characterView.render(playerInfo.position, playerInfo.velocity);
    }

    private void onSettingsClicked(){
        System.out.println("Settings not implemented yet");
    }

    private void onInteractClicked(){

        if(interactableInFocus != null){
            interactableInFocus.setPlayerSpace(playerSpace);
            interactableInFocus.setPlayerName(name);
            interactableInFocus.interact(this);
        }
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

    private void onVentClicked(){
        System.out.println("Vent not implemented yet");

        if(ventInFocus != null){
            ventInFocus.interact(this);
        }
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

    public void completeTask(TaskType taskType) {
        playerView.completeTask(taskType);
        try{
            playerSpace.put(ClientUpdate.TASK_COMPLETE);
            playerSpace.put(ClientUpdate.TASK_COMPLETE, name);
        }catch (Exception e){

        }
    }
}
