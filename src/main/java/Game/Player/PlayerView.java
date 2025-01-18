package Game.Player;

import Game.GameController;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.function.Function;


public class PlayerView extends BorderPane {

    private Button killButton;
    private Button mapButton;
    private Button reportButton;
    private Button sabotageButton;
    private Button useButton;


    public PlayerView(PlayerInfo info, BooleanProperty[] booleanProperties, Runnable[] callbackFunctions){

        this.setPadding(new Insets(25));

        Text tasksLabel = new Text("Tasks 0/5");
        tasksLabel.setFont(new Font(50));
        this.setTop(tasksLabel);

        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT); // aligns children to the right
        bottomBox.setSpacing(10);                 // optional spacing between children


        if(info.isImposter){
            // add imposter buttons
            killButton = new Button();
            killButton.setGraphic(new ImageView(new Image("killIcon.png")));
            killButton.setStyle("-fx-background-color: transparent; "
                    + "-fx-border-color: transparent; "
                    + "-fx-background-radius: 0;");

            killButton.disableProperty().bind(booleanProperties[3]);
            killButton.setOnAction(e -> {callbackFunctions[3].run();});


            sabotageButton = new Button();
            sabotageButton.setGraphic(new ImageView(new Image("sabotageIcon.png")));
            sabotageButton.setStyle("-fx-background-color: transparent; "
                    + "-fx-border-color: transparent; "
                    + "-fx-background-radius: 0;");

            sabotageButton.disableProperty().bind(booleanProperties[4]);
            sabotageButton.setOnAction(e -> {callbackFunctions[4].run();});

            bottomBox.getChildren().addAll(killButton, sabotageButton);

        }

        useButton = new Button();
        useButton.setGraphic(new ImageView(new Image("useIcon.png")));
        useButton.setStyle("-fx-background-color: transparent; "
                + "-fx-border-color: transparent; "
                + "-fx-background-radius: 0;");

        useButton.disableProperty().bind(booleanProperties[0]);
        useButton.setOnAction(e -> {callbackFunctions[0].run();});

        bottomBox.getChildren().add(useButton);



        VBox rightBox = new VBox();
        rightBox.setSpacing(10);
        rightBox.setPadding(new Insets(25, 0, 25, 0));
        rightBox.setAlignment(Pos.TOP_CENTER);

        mapButton = new Button("Map");

        mapButton.disableProperty().bind(booleanProperties[1]);
        mapButton.setOnAction(e -> {callbackFunctions[1].run();});

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        reportButton = new Button();
        reportButton.setGraphic(new ImageView(new Image("reportIcon.png")));
        reportButton.setStyle("-fx-background-color: transparent; "
                + "-fx-border-color: transparent; "
                + "-fx-background-radius: 0;");

        reportButton.disableProperty().bind(booleanProperties[2]);
        reportButton.setOnAction(e -> {callbackFunctions[2].run();});

        rightBox.getChildren().addAll(mapButton, spacer, reportButton);

        this.setBottom(bottomBox);
        this.setRight(rightBox);
        //this.setStyle("-fx-background-color: rgba(0,0,0,0.3)");
    }

}
