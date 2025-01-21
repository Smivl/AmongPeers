package Menu;

import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LobbyMenu extends VBox {
    private BooleanProperty isHosting = new SimpleBooleanProperty(false);
    private StringProperty IP = new SimpleStringProperty("IP Address: 0.0.0.0");
    private StringProperty Port = new SimpleStringProperty("Port number: 000000");;

    public LobbyMenu(MenuManager menuManager) {
        setSpacing(10);

        Label title = new Label("Waiting...");
        title.getStyleClass().add("title");
        Button startButton = new Button("Start Game");

        startButton.setOnAction(e -> menuManager.startGame());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> menuManager.transitionToMainMenu());

        HBox hBox = new HBox(10);

        Label ip = new Label();
        ip.textProperty().bind(IP);

        Label port = new Label();
        port.textProperty().bind(Port);

        hBox.getChildren().addAll(ip, port);
        hBox.setAlignment(Pos.CENTER);

        getChildren().addAll(title, startButton, backButton, hBox);

        // Show the start button only if the user is the host
        startButton.visibleProperty().bind(isHosting);
    }

    public void setHosting(boolean isHosting) {
        this.isHosting.setValue(isHosting);
    }

    public void setURI(String ip, int port) {
        this.IP.set("IP Address: " + ip);
        this.Port.set("Port number: " + port);
    }
}
