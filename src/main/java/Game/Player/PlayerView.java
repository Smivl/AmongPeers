package Game.Player;

import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.awt.*;

public class PlayerView extends BorderPane {
    public PlayerView(){

        Text tasksLabel = new Text("Tasks 0/5");
        tasksLabel.setFont(new Font(50));
        this.setTop(tasksLabel);

        //this.setStyle("-fx-background-color: rgba(0,0,0,0.3)");
    }
}
