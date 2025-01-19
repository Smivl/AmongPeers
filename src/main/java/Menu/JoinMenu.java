package Menu;

import Server.ServerScan;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinMenu extends VBox {
    Label errorMessage;
    TextField nameField;

    List<Button> servers = new ArrayList<>();

    MenuManager menuManager;

    public JoinMenu(MenuManager menuManager) {
        setSpacing(10);
        Label title = new Label("Join Game");

        this.menuManager = menuManager;

        this.nameField = new TextField();
        nameField.setPromptText("Enter your name");

        errorMessage = new Label();
        errorMessage.setVisible(false);


        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        getChildren().addAll(title, backButton, nameField, errorMessage);
    }

    public void addServer(Map.Entry<String, DatagramPacket> entry){
        Button button = new Button(entry.getKey());
        button.setOnAction(e -> {
            menuManager.transitionToLobbyMenu(
                    false,
                    nameField.getText(),
                    String.valueOf(entry.getValue().getAddress()),
                    entry.getValue().getPort()
            );
        });
        servers.add(button);
        getChildren().add(button);
    }

    public void clearServers(){
        getChildren().removeAll(servers);
        servers.clear();
    }

    public void hideErrorMessage(){
        errorMessage.setVisible(false);
    }

    public void displayErrorMessage(String message){
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
