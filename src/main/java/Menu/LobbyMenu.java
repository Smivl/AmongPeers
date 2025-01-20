package Menu;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LobbyMenu extends VBox {
    private BooleanProperty isHosting = new SimpleBooleanProperty(false);

    public LobbyMenu(MenuManager menuManager) {
        setSpacing(10);

        Label title = new Label("Waiting...");
        title.getStyleClass().add("title");
        Button startButton = new Button("Start Game");

        startButton.setOnAction(e -> menuManager.startGame());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        getChildren().addAll(title, startButton, backButton);

        // Show the start button only if the user is the host
        startButton.visibleProperty().bind(isHosting);
    }

    public void setHosting(boolean isHosting) {
        this.isHosting.setValue(isHosting);
    }
}
