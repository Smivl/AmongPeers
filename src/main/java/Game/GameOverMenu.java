package Game;

import Menu.MenuManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class GameOverMenu extends VBox {
    public GameOverMenu(String text,Runnable runnable) {
        setSpacing(20);

        Label title = new Label(text);
        title.getStyleClass().clear();
        title.getStyleClass().add("title");

        // Join Game Button
        Button returnToMenu = new Button("Return to menu!");
        returnToMenu.setOnAction(e -> runnable.run());

        getChildren().addAll(title, returnToMenu);
    }
}
