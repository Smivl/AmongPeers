package Game.GameMap;

import Game.GameCharacter.CharacterView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class GameMapView extends Pane {

    private ImageView mapImage;
    private Scene scene;
    private static final Image[] deathFrames = new Image[] {
            new Image("dead1.png"),
            new Image("dead2.png"),
            new Image("dead3.png"),
            new Image("dead4.png"),
            new Image("dead5.png"),
            new Image("dead6.png"),
            new Image("dead7.png"),
            new Image("dead8.png"),
            new Image("dead9.png"),
            new Image("dead10.png"),
            new Image("dead11.png"),
            new Image("dead12.png"),
            new Image("dead13.png"),
            new Image("dead14.png"),
            new Image("dead15.png"),
            new Image("dead16.png"),
            new Image("dead17.png"),
            new Image("dead18.png"),
            new Image("dead19.png"),
            new Image("dead20.png"),
            new Image("dead21.png"),
            new Image("dead22.png"),
            new Image("dead23.png"),
            new Image("dead24.png"),
            new Image("dead25.png"),
            new Image("dead26.png"),
            new Image("dead27.png"),
            new Image("dead28.png"),
            new Image("dead29.png"),
            new Image("dead30.png"),
            new Image("dead31.png"),
            new Image("dead32.png"),
            new Image("dead33.png")
    };

    public GameMapView(Scene scene) {
        this.scene = scene;
        this.mapImage = new ImageView(new Image("map.png"));
        this.getChildren().add(this.mapImage);
    }

    public void onPlayerKilled(ImageView body){

        final int[] currentFrameIndex = {0};

        Timeline deathAnim = new Timeline(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                body.setImage(deathFrames[currentFrameIndex[0]]);
                currentFrameIndex[0] = (currentFrameIndex[0] + 1) % deathFrames.length;
            }
        }));
        deathAnim.setCycleCount(33);
        deathAnim.playFromStart();

        this.getChildren().add(body);
    }

    public void render(CharacterView player) {

        double offsetX = this.scene.getWidth() / 2.0 - player.getCenterX();
        double offsetY = this.scene.getHeight() / 2.0 - player.getCenterY();
        this.setTranslateX(offsetX);
        this.setTranslateY(offsetY);

    }
}
