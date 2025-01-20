package Game.Player;

import Game.GameController;
import Game.Interactables.Task.Task;
import Game.Interactables.Task.TaskType;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.*;
import java.util.function.Function;


public class PlayerView extends BorderPane {

    private Button killButton;
    private Button mapButton;
    private Button reportButton;
    private Button sabotageButton;
    private Button useButton;
    private Button ventButton;
    private Button settingsButton;

    private Map<TaskType, Label> taskLabelList = new HashMap<>();

    private ProgressBar taskProgressBar;

    public PlayerView(PlayerInfo info, TaskType[] taskList, BooleanProperty[] booleanProperties, Runnable[] callbackFunctions){

        this.setPadding(new Insets(15));

        // PROGRESS BAR AND SETTINGS
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT); // aligns children to the right

        StackPane progressBarWithText = new StackPane();
        //progressBarWithText.setPadding(new Insets(0, 0, 0, 15));

        taskProgressBar = new ProgressBar();
        taskProgressBar.setProgress(0.0);
        taskProgressBar.setPrefWidth(550);
        taskProgressBar.setPrefHeight(45);

        taskProgressBar.getStylesheets().add("progressbar.css");

        Text tasksLabel = new Text("TOTAL TASKS COMPLETED");
        tasksLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 14));
        tasksLabel.setFill(Color.WHITE);
        tasksLabel.setStroke(Color.BLACK);
        tasksLabel.setStrokeWidth(.5);

        Region hspacer = new Region();
        HBox.setHgrow(hspacer, Priority.ALWAYS);

        settingsButton = createButtonWithIcon("settingsButtonIcon.png");
        settingsButton.setOnAction(e -> {callbackFunctions[6].run();});

        mapButton = createButtonWithIcon("mapButtonIcon.png");
        mapButton.setOnAction(e -> {callbackFunctions[1].run();});

        progressBarWithText.getChildren().addAll(taskProgressBar, tasksLabel);
        topBox.getChildren().addAll(progressBarWithText, hspacer, mapButton, settingsButton);

        this.setTop(topBox);

        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT); // aligns children to the right
        bottomBox.setSpacing(10);                 // optional spacing between children

        VBox rightBox = new VBox();
        rightBox.setSpacing(10);
        rightBox.setPadding(new Insets(25, 0, 0, 0));
        rightBox.setAlignment(Pos.TOP_CENTER);

        useButton = createButtonWithIcon("useIcon.png");
        useButton.disableProperty().bind(booleanProperties[0]);
        useButton.setOnAction(e -> {callbackFunctions[0].run();});

        reportButton = createButtonWithIcon("reportIcon.png");
        reportButton.disableProperty().bind(booleanProperties[1]);
        reportButton.setOnAction(e -> {callbackFunctions[2].run();});

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox leftPanel = new VBox();
        leftPanel.setSpacing(5);
        leftPanel.setStyle("-fx-padding: 10;" + "-fx-background-color: rgb(1,1,1,0.15);" + "-fx-font-size: 18;" + "-fx-font-weight: bold;");

        if(info.isImposter){

            // add imposter buttons
            killButton = createButtonWithIcon("killIcon.png");
            killButton.disableProperty().bind(booleanProperties[2]);
            killButton.setOnAction(e -> {callbackFunctions[3].run();});

            sabotageButton = createButtonWithIcon("sabotageIcon.png");
            sabotageButton.disableProperty().bind(booleanProperties[3]);
            sabotageButton.setOnAction(e -> {callbackFunctions[4].run();});

            ventButton = createButtonWithIcon("ventIcon.png");
            ventButton.disableProperty().bind(booleanProperties[4]);
            ventButton.setOnAction(e -> {callbackFunctions[5].run();});

            bottomBox.getChildren().addAll(killButton, sabotageButton, ventButton);

            HBox rightBottomBox = new HBox();
            rightBottomBox.setSpacing(10);

            rightBottomBox.getChildren().addAll(useButton, reportButton);
            rightBox.getChildren().addAll(spacer, rightBottomBox);

            Label imposterLabel = new Label("Fake tasks:");
            imposterLabel.setTextFill(Color.WHITE);
            leftPanel.getChildren().add(imposterLabel);
        } else{
            bottomBox.getChildren().add(useButton);
            rightBox.getChildren().addAll(spacer, reportButton);

            Label crewmateLabel = new Label("Tasks to complete:");
            crewmateLabel.setTextFill(Color.WHITE);
            leftPanel.getChildren().add(crewmateLabel);
        }


        for (TaskType task : taskList) {
            Label taskLabel = new Label(task.getDisplayName());
            taskLabel.setTextFill(Color.WHITE);
            leftPanel.getChildren().add(taskLabel);
            taskLabelList.put(task, taskLabel);
        }

        leftPanel.setMaxHeight(Region.USE_PREF_SIZE);
        leftPanel.setMaxWidth(Region.USE_PREF_SIZE);


        this.setLeft(leftPanel);
        this.setBottom(bottomBox);
        this.setRight(rightBox);
    }

    public void setTaskProgressBar(double progress){
        taskProgressBar.setProgress(progress);
    }

    public void completeTask(TaskType taskType){
        taskLabelList.get(taskType).setTextFill(Color.GREEN);
    }

    public void updateTaskProgress(TaskType taskType, double progress){
        taskLabelList.get(taskType).setTextFill(Color.YELLOW);
        System.out.println("UPDATED TO : " +progress);
    }

    private Button createButtonWithIcon(String iconPath){
        Button res = new Button();
        res.setGraphic(new ImageView(new Image(iconPath)));
        res.setStyle("-fx-background-color: transparent; "
                + "-fx-border-color: transparent; "
                + "-fx-background-radius: 0;");
        return res;
    }
}
