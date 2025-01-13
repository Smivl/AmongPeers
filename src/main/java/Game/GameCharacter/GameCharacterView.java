package Game.GameCharacter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class GameCharacterView extends StackPane {

    private final ImageView characterImage;

    private double centerX;
    private double centerY;
    private double[] velocity;

    // Animation fields
    private final Image idleImage;
    private final Image[] walkFrames;
    private int currentFrameIndex = 0;
    private Timeline walkAnimation;

    public ImageView getCharacterImage() { return characterImage; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }

    public GameCharacterView(String name, double x, double y, double[] velocity, Color color) {

        this.centerX = x;
        this.centerY = y;
        this.velocity = velocity;

        idleImage = new Image("idle.png");

        // Load the walking frames (4 images)
        walkFrames = new Image[] {
                new Image("walk1.png"),
                new Image("walk2.png"),
                new Image("walk3.png"),
                new Image("walk4.png"),
                new Image("walk5.png"),
                new Image("walk6.png"),
                new Image("walk7.png"),
                new Image("walk8.png"),
                new Image("walk9.png"),
                new Image("walk10.png"),
                new Image("walk11.png"),
                new Image("walk12.png")
        };

        characterImage = new ImageView(new Image("idle.png"));

        characterImage.setFitWidth(90);
        characterImage.setPreserveRatio(true);


        /*

        // todo: Adjust color of character here!

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(0.5);

        characterImage.setEffect(colorAdjust);
         */

        Text nameplate = new Text(name);
        nameplate.setFill(Color.WHITE);
        nameplate.setFont(new Font(20));

        getChildren().addAll(characterImage, nameplate);

        setPrefSize(characterImage.getFitWidth(), characterImage.getFitHeight());

        setLayoutX(centerX - (getPrefWidth()/2));
        setLayoutY(centerY - (getPrefHeight()/2));

        // Flip image based on velocity (facing direction we are going)
        if(velocity[0] < 0) this.characterImage.setScaleX(-1);
        if(velocity[0] > 0) this.characterImage.setScaleX(1);

        setupWalkAnimation();
    }

    private void setupWalkAnimation() {

        walkAnimation = new Timeline(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                characterImage.setImage(walkFrames[currentFrameIndex]);
                currentFrameIndex = (currentFrameIndex + 1) % walkFrames.length;

            }
        }));
        walkAnimation.setCycleCount(Timeline.INDEFINITE);
    }


    public void render(double x, double y, double[] velocity) {

        this.centerX = x;
        this.centerY = y;
        this.velocity = velocity;

        setLayoutX(centerX - (getPrefWidth()/2));
        setLayoutY(centerY - (getPrefHeight()/2));

        if(velocity[0] < 0) this.characterImage.setScaleX(-1);
        if(velocity[0] > 0) this.characterImage.setScaleX(1);

        // Determine if character is moving or idle
        if (velocity[0] != 0 || velocity[1] != 0) {
            // Character is moving, start walking animation if not already running
            if (!walkAnimation.getStatus().equals(Timeline.Status.RUNNING)) {
                walkAnimation.playFromStart();
            }
        } else {
            // Character is idle, stop walking animation and show idle image
            if (walkAnimation.getStatus().equals(Timeline.Status.RUNNING)) {
                walkAnimation.stop();
            }
            characterImage.setImage(idleImage);
            currentFrameIndex = 0;
        }


    }
}
