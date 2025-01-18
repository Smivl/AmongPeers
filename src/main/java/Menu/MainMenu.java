package Menu;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MainMenu extends VBox {
    public MainMenu(MenuManager menuManager) {
        setSpacing(20);

        // Join Game Button
        Button joinButton = new Button("Join Game");
        joinButton.setOnAction(e -> menuManager.transitionToJoinMenu());

        // Host Game Button
        Button hostButton = new Button("Host Game");
        hostButton.setOnAction(e -> menuManager.transitionToHostMenu());

        getChildren().addAll(joinButton, hostButton);
    }
}
