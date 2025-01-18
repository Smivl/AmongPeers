package Menu;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostMenu extends VBox {
    public HostMenu(MenuManager menuManager) {
        setSpacing(10);

        Label title = new Label("Host Game");

        Label ipLabel = new Label();
        try {
            ipLabel.setText("Your IP: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            ipLabel.setText("Unable to fetch IP");
        }

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
            try {
                menuManager.transitionToLobbyMenu(true, nameField.getText(), InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        getChildren().addAll(title, ipLabel, nameField, startButton, backButton);
    }
}
