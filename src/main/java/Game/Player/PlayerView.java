package Game.Player;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class PlayerView extends BorderPane {
    public PlayerView(){

        this.setPadding(new Insets(25));

        Text tasksLabel = new Text("Tasks 0/5");
        tasksLabel.setFont(new Font(50));
        this.setTop(tasksLabel);


        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT); // aligns children to the right
        bottomBox.setSpacing(10);                 // optional spacing between children

        Button killButton = new Button("Kill");
        killButton.setDisable(true);

        Button sabotageButton = new Button("Sabotage");
        sabotageButton.setDisable(true);

        bottomBox.getChildren().addAll(killButton, sabotageButton);

        VBox rightBox = new VBox();
        rightBox.setSpacing(10);
        rightBox.setPadding(new Insets(25));
        rightBox.setAlignment(Pos.TOP_CENTER);

        Button mapButton = new Button("Map");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button reportButton = new Button("Report");
        reportButton.setDisable(true);

        rightBox.getChildren().addAll(mapButton, spacer, reportButton);

        this.setBottom(bottomBox);
        this.setRight(rightBox);
        //this.setStyle("-fx-background-color: rgba(0,0,0,0.3)");
    }
}
