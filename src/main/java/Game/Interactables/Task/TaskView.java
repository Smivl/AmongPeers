package Game.Interactables.Task;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class TaskView extends VBox {
    private ProgressBar progressBar;
    private Timeline timeline;

    public TaskView(String info, int seconds, Runnable callback){

        Label text = new Label(info);
        text.setTextFill(Color.WHITE);

        progressBar = new ProgressBar(0);
        progressBar.setMinWidth(350);

        setPadding(new Insets(50));
        setSpacing(20);
        getChildren().addAll(text, progressBar);

        this.setStyle(
                "-fx-background-color: rgb(1,1,1,0.6);"
        );

        setAlignment(Pos.CENTER);
        setMaxHeight(Region.USE_PREF_SIZE);
        setMaxWidth(Region.USE_PREF_SIZE);

        double intervalMillis = 100;
        int totalUpdates = (int) (seconds * 1000 / intervalMillis);

        timeline = new Timeline(new KeyFrame(Duration.millis(intervalMillis), event -> {
            double progress = progressBar.getProgress();
            // Increment progress based on total updates needed.
            progressBar.setProgress(progress + 1.0 / totalUpdates);
        }));

        timeline.setOnFinished(e -> callback.run());

        // Set the cycle count so that it runs exactly for the specified duration.
        timeline.setCycleCount(totalUpdates);
        timeline.play();

    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
