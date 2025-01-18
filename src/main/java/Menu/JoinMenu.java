package Menu;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class JoinMenu extends VBox {
    Label errorMessage;
    public JoinMenu(MenuManager menuManager) {
        setSpacing(10);

        Label title = new Label("Join Game");

        TextField ipField = new TextField();
        ipField.setPromptText("Enter Host IP");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        errorMessage = new Label();
        errorMessage.setVisible(false);

        Button joinButton = new Button("Join");
        joinButton.setOnAction(e -> {
            menuManager.transitionToLobbyMenu(false, nameField.getText(), ipField.getText());
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        getChildren().addAll(title, ipField, nameField, errorMessage, joinButton, backButton);
    }

    public void hideErrorMessage(){
        errorMessage.setVisible(false);
    }

    public void displayErrorMessage(String message){
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
