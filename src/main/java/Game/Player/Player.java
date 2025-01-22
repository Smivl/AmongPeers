package Game.Player;

import Game.GameCharacter.CharacterView;
import Game.GameController;
import Game.GameMap.GameMap;
import Game.Interactables.Interactable;
import Game.Interactables.Sabotage.Sabotage;
import Game.Interactables.Sabotage.SabotageType;
import Game.Interactables.Task.TaskType;
import Server.ClientUpdate;
import Server.ServerUpdate;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.Arrays;
import java.util.Map;

public class Player {

    private final int SPEED = 650;
    private final int KILL_COOLDOWN_DURATION = 25;
    private final int SABOTAGE_COOLDOWN_DURATION = 40;

    private boolean inputLocked = true;
    private boolean wDown, aDown, sDown, dDown;


    // Current cooldown counters
    private DoubleProperty killCooldown = new SimpleDoubleProperty(0.0);
    private DoubleProperty sabotageCooldown = new SimpleDoubleProperty(0.0);

    private Map.Entry<Shape, Interactable> interactableInFocus = null;
    private Map.Entry<Shape, Interactable> ventInFocus = null;

    private GameController controller;
    private GameMap map;

    private PlayerView playerView;
    private CharacterView characterView;

    // property decides whether button should be disabled
    private final BooleanProperty canKill = new SimpleBooleanProperty(false);
    private final BooleanProperty canReport = new SimpleBooleanProperty(false);
    private final BooleanProperty canInteract = new SimpleBooleanProperty(false);
    private final BooleanProperty canSabotage = new SimpleBooleanProperty(false);
    private final BooleanProperty canVent = new SimpleBooleanProperty(false);


    private TaskType[] taskList;
    private double[] spawnPoint;
    private PlayerInfo playerInfo;
    private final String name;

    private Space playerSpace;
    private Space sabotageSpace;

    public TaskType[] getTasks() { return taskList; }
    public PlayerInfo getInfo() { return playerInfo; }
    public PlayerView getPlayerView() { return playerView; }
    public CharacterView getCharacterView() { return characterView; }
    public GameMap getMap() { return map; }
    public void setInputLocked(boolean lock) { this.inputLocked = lock; }
    public void setController(GameController controller) { this.controller = controller; }
    public void setMap(GameMap map) {this.map = map;}

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
                            canSabotage,
                            canVent
                    },
                    new Runnable[]{ // order is: interact, map, report, kill, sabotage, vent, settings
                            this::onInteractClicked,
                            this::onMapClicked,
                            this::onReportClicked,
                            this::onKillClicked,
                            this::onSabotageClicked,
                            this::onVentClicked,
                            this::onSettingsClicked,
                            this::onInteractPressed,
                            this::onInteractReleased
                    },
                    new DoubleProperty[]{
                            killCooldown,
                            sabotageCooldown
                    }
            );

            this.characterView = new CharacterView(name, info, info.isImposter ? Color.RED : Color.WHITE);

            if(info.isImposter){
                resetCooldowns();
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void onKilled() {
        playerInfo.isAlive = false;
        this.characterView.onKilled();
    }

    public void onUpdate(double delta){

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

            canReport.set(
                map.checkCollisionWithBodies(this.characterView) &&
                playerInfo.isAlive
            );
            interactableInFocus = map.getInteractable(this.characterView);
            canInteract.set(
                interactableInFocus != null &&
                interactableInFocus.getValue().canInteract(playerInfo)
            );

            if (playerInfo.isImposter) {
                canKill.set(
                    playerInfo.isAlive &&
                    controller.getPlayerToKill(this.characterView) != null &&
                    killCooldown.get() <= 0
                );

                ventInFocus = map.getVent(this.characterView);
                canVent.set(
                    ventInFocus != null &&
                    ventInFocus.getValue().canInteract(playerInfo)
                );

                canSabotage.set(
                    playerInfo.isAlive &&
                    !map.getSabotageActive() &&
                    sabotageCooldown.get() <= 0
                );

                killCooldown.set(killCooldown.get()-delta);
                sabotageCooldown.set(sabotageCooldown.get()-delta);
            }
        }else{
            canKill.set(false);
            canReport.set(false);
            canInteract.set(false);
            canVent.set(false);
            canSabotage.set(false);
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

    public void resetCooldowns(){
        this.killCooldown.set(KILL_COOLDOWN_DURATION);
        this.sabotageCooldown.set(SABOTAGE_COOLDOWN_DURATION);
    }

    private void onSettingsClicked(){
        System.out.println("Settings not implemented yet");
    }


    private void onInteractPressed(){
        if(getMap().getSabotageActive() && interactableInFocus != null && interactableInFocus.getValue() instanceof Sabotage){
            ((Sabotage) interactableInFocus.getValue()).setSabotageSpace(sabotageSpace);
            interactableInFocus.getValue().interact(this);
        }
    }

    private void onInteractReleased(){
        if(getMap().getSabotageActive() && interactableInFocus != null && interactableInFocus.getValue() instanceof Sabotage){
            interactableInFocus.getValue().stopInteraction(this);
        }
    }

    private void onInteractClicked(){

        if(interactableInFocus != null && !(interactableInFocus.getValue() instanceof Sabotage)){
            interactableInFocus.getValue().setPlayerSpace(playerSpace);
            interactableInFocus.getValue().setPlayerName(name);
            interactableInFocus.getValue().interact(this);
        }
    }

    private void onMapClicked(){
        System.out.println("Map not implemented yet");
    }

    private void onReportClicked(){
        try {
            playerSpace.put(ClientUpdate.REPORT);
            playerSpace.put(ClientUpdate.REPORT, name);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

    }

    private void onKillClicked(){

        CharacterView playerKilled = controller.getPlayerToKill(this.characterView);

        if (playerKilled != null) {
            playerInfo.position = new double[]{playerKilled.getCenterX(), playerKilled.getCenterY()};
            this.characterView.render(playerInfo.position, playerInfo.velocity);

            try {
                playerSpace.put(ClientUpdate.POSITION);
                playerSpace.put(ClientUpdate.POSITION, playerInfo.position, playerInfo.velocity);

                playerSpace.put(ClientUpdate.KILL);
                playerSpace.put(ClientUpdate.KILL, name, playerKilled.getName());

                killCooldown.set(KILL_COOLDOWN_DURATION);
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void onSabotageClicked(){
        System.out.println("Sabotage not implemented yet");
        try {
            playerSpace.put(ClientUpdate.SABOTAGE);
            playerSpace.put(ClientUpdate.SABOTAGE, name, SabotageType.NUCLEAR_MELTDOWN);

            sabotageCooldown.set(SABOTAGE_COOLDOWN_DURATION);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    private void onVentClicked(){
        System.out.println("Vent not implemented yet");

        if(ventInFocus != null){
            ventInFocus.getValue().interact(this);
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
        map.onTaskComplete(interactableInFocus);
        interactableInFocus = null;
        try{
            playerSpace.put(ClientUpdate.TASK_COMPLETE);
            playerSpace.put(ClientUpdate.TASK_COMPLETE, name);

        }catch (Exception e){

        }
    }

    public void fixLights(){
        try{
            sabotageSpace.put("lights");
            playerView.setCenter(null);
            setInputLocked(false);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    public void completeSubTask(TaskType taskType){
        map.onTaskComplete(interactableInFocus);
        interactableInFocus = null;
    }

    public void onSabotageStarted(Space sabotageSpace) {
        this.sabotageSpace = sabotageSpace;
    }

    public void onSabotageEnded() {
        this.sabotageSpace = null;
        playerView.setCenter(null);
        inputLocked = false;
    }
}
