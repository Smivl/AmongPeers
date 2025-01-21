package Menu;

import Server.ServerScan;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinMenu extends VBox {
    Label errorMessage;
    TextField nameField;
    TextField IPField;

    List<Button> servers = new ArrayList<>();

    MenuManager menuManager;

    public JoinMenu(MenuManager menuManager) {
        setSpacing(10);
        Label title = new Label("Join Game");
        title.getStyleClass().removeAll();
        title.getStyleClass().add("title");

        this.menuManager = menuManager;

        this.nameField = new TextField();
        nameField.setPromptText("Enter your name");

        errorMessage = new Label();
        errorMessage.setVisible(false);

        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP");

        TextField portField = new TextField();
        portField.setPromptText("Enter port");

        Button joinManual = new Button("Join");
        joinManual.setOnAction(e -> {
            menuManager.transitionToLobbyMenu(
                    false,
                    nameField.getText(),
                    ipField.getText(),
                    Integer.parseInt(portField.getText())
            );
        });

        portField.textProperty().addListener((e, old, newV) ->{
            if (newV.matches("[0-9]+")){
                joinManual.setDisable(false);
                joinManual.setText("Join");
            }else{
                joinManual.setDisable(true);
                joinManual.setText("Port should be a number!");
            }
        });

        HBox manualHbox = new HBox();
        manualHbox.setSpacing(10);
        manualHbox.getChildren().addAll(ipField, portField, joinManual);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        Button refreshButton = new Button("Refresh servers");
        refreshButton.setOnAction(e -> new Thread(this::refreshServers).start());

        HBox hBox = new HBox(10);
        hBox.getChildren().addAll( backButton, refreshButton);
        getChildren().addAll(title, hBox, nameField, manualHbox, errorMessage);
    }

    public void refreshServers(){
        //clearServers();

        Map<String, DatagramPacket> servers = ServerScan.scanForServers();
        clearServers();
        if(servers != null) {
            for(Map.Entry<String, DatagramPacket> server : servers.entrySet()) addServer(server);
        }
    }

    public void hideErrorMessage(){
        errorMessage.setVisible(false);
    }

    public void displayErrorMessage(String message){
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    private void addServer(Map.Entry<String, DatagramPacket> entry){

        String serverName = entry.getKey().substring("SERVER_AVAILABLE:".length(), entry.getKey().indexOf("SERVER_IP:"));
        String serverIP = entry.getKey().substring(entry.getKey().indexOf("SERVER_IP:")+"SERVER_IP:".length());

        Button button = new Button(serverName);
        button.setOnAction(e -> {
            menuManager.transitionToLobbyMenu(
                    false,
                    nameField.getText(),
                    serverIP,
                    entry.getValue().getPort()
            );
        });
        servers.add(button);
        Platform.runLater(() -> getChildren().add(button));
    }

    private void clearServers(){
        List<Button> currentServers = List.copyOf(servers);
        Platform.runLater(() ->{
            getChildren().removeAll(currentServers);
        });
        servers.clear();
    }
}
