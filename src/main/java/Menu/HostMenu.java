package Menu;

import Server.ServerBroadcast;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class HostMenu extends VBox {
    public HostMenu(MenuManager menuManager) {
        setSpacing(10);

        Label title = new Label("Host Game");
        title.getStyleClass().removeAll();
        title.getStyleClass().add("title");

        Label ipLabel = new Label();

        String ip = ServerBroadcast.getIPAddress();

        if (ip == null) ipLabel.setText("Unable to find IP address");
        else ipLabel.setText("Your IP: " + ServerBroadcast.getIPAddress());

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Button startButton = new Button("Start Hosting");
        startButton.setDisable(true);

        nameField.textProperty().addListener((e, oldValue, newValue) -> {
            if (newValue.isEmpty() || newValue.trim().split("\\s+").length!=1 || !newValue.trim().matches("[a-zA-Z0-9]+")){
                startButton.setDisable(true);
            } else {
                startButton.setDisable(false);
            }
        });

        startButton.setOnAction(e -> {
            menuManager.transitionToLobbyMenu(true, nameField.getText(), ip, ServerBroadcast.setupServerPort());
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        getChildren().addAll(title, ipLabel, nameField, startButton, backButton);
    }
}
