package Game.GameCharacter;

import Game.Player.PlayerInfo;
import javafx.animation.Animation;
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


public class CharacterView extends StackPane {

    private final ImageView characterImage;
    private String name;

    private boolean isAlive;
    private boolean isImposter;
    private double centerX;
    private double centerY;
    private double[] velocity;

    private static final Image[] walkFrames = new Image[] {
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

    private static final Image[] ghostFrames = new Image[] {
            new Image("ghost1.png"),
            new Image("ghost2.png"),
            new Image("ghost3.png"),
            new Image("ghost4.png"),
            new Image("ghost5.png"),
            new Image("ghost6.png"),
            new Image("ghost7.png"),
            new Image("ghost8.png"),
            new Image("ghost9.png"),
            new Image("ghost10.png"),
            new Image("ghost11.png"),
            new Image("ghost12.png"),
            new Image("ghost13.png"),
            new Image("ghost14.png"),
            new Image("ghost15.png"),
            new Image("ghost16.png"),
            new Image("ghost17.png"),
            new Image("ghost18.png"),
            new Image("ghost19.png"),
            new Image("ghost20.png"),
            new Image("ghost21.png"),
            new Image("ghost22.png"),
            new Image("ghost23.png"),
            new Image("ghost24.png"),
            new Image("ghost25.png"),
            new Image("ghost26.png"),
            new Image("ghost27.png"),
            new Image("ghost28.png"),
            new Image("ghost29.png"),
            new Image("ghost30.png"),
            new Image("ghost31.png"),
            new Image("ghost32.png"),
            new Image("ghost33.png"),
            new Image("ghost34.png"),
            new Image("ghost35.png"),
            new Image("ghost36.png"),
            new Image("ghost37.png"),
            new Image("ghost38.png"),
            new Image("ghost39.png"),
            new Image("ghost40.png"),
            new Image("ghost41.png"),
            new Image("ghost42.png"),
            new Image("ghost43.png"),
            new Image("ghost44.png"),
            new Image("ghost45.png"),
            new Image("ghost46.png"),
            new Image("ghost47.png"),
            new Image("ghost48.png")
    };

    private final Image idleImage = new Image("idle.png");

    // Animation fields
    private int currentFrameIndex = 0;
    private Timeline movementAnimation;

    public boolean getIsImposter() { return  isImposter; }
    public boolean getIsAlive() { return isAlive; }
    public String getName() { return name; }
    public ImageView getCharacterImage() { return characterImage; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }

    public CharacterView(String name, PlayerInfo info, Color nameColor) {

    this.isImposter = info.isImposter;
        this.isAlive = info.isAlive;
        this.centerX = info.position[0];
        this.centerY = info.position[1];
        this.velocity = info.velocity;
        this.name = name;

        characterImage = new ImageView();

        characterImage.setFitWidth(90);
        characterImage.setPreserveRatio(true);

        /*
        // todo: Adjust color of character here!

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(0.5);

        characterImage.setEffect(colorAdjust);
         */

        Text nameplate = new Text(name);
        nameplate.setFill(nameColor);
        nameplate.setFont(new Font(20));

        getChildren().addAll(characterImage, nameplate);

        setPrefSize(characterImage.getFitWidth(), characterImage.getFitHeight());

        setLayoutX(centerX - (getPrefWidth()/2));
        setLayoutY(centerY - ((double) 129 /2));

        // Flip image based on velocity (facing direction we are going)
        if(velocity[0] < 0) this.characterImage.setScaleX(-1);
        if(velocity[0] > 0) this.characterImage.setScaleX(1);

        setupMovementAnimation();
        render(info.position, info.velocity);
    }

    private void setupMovementAnimation() {

        movementAnimation = new Timeline(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if(isAlive){
                    characterImage.setImage(walkFrames[currentFrameIndex]);
                    currentFrameIndex = (currentFrameIndex + 1) % walkFrames.length;
                }else{
                    characterImage.setImage(ghostFrames[currentFrameIndex]);
                    currentFrameIndex = (currentFrameIndex + 1) % ghostFrames.length;
                }

            }
        }));
        movementAnimation.setCycleCount(Timeline.INDEFINITE);
    }

    public void onKilled(){
        this.currentFrameIndex = 0;
        this.isAlive = false;
        movementAnimation.playFromStart();
    }

    public void render(double[] position, double[] velocity) {

        this.centerX = position[0];
        this.centerY = position[1];
        this.velocity = velocity;

        setLayoutX(centerX - (getPrefWidth()/2));
        setLayoutY(centerY - ((double) 129 /2));

        if(velocity[0] < 0) this.characterImage.setScaleX(-1);
        if(velocity[0] > 0) this.characterImage.setScaleX(1);

        if(isAlive){
            // Determine if character is moving or idle
            if (velocity[0] != 0 || velocity[1] != 0) {
                // Character is moving, start walking animation if not already running
                if (!movementAnimation.getStatus().equals(Timeline.Status.RUNNING)) {
                    movementAnimation.playFromStart();
                }
            } else {
                // Character is idle, stop walking animation and show idle image
                if (movementAnimation.getStatus().equals(Timeline.Status.RUNNING)) {
                    movementAnimation.stop();
                }
                characterImage.setImage(idleImage);
                currentFrameIndex = 0;
            }
        } else{
            if(!movementAnimation.getStatus().equals(Animation.Status.RUNNING)){
                movementAnimation.playFromStart();
            }
        }
    }

}
