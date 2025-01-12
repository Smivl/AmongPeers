package PlayerM;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class PlayerView extends StackPane {

    private final ImageView characterImage;

    private double centerX;
    private double centerY;

    private double[] velocity;

    public ImageView getCharacterImage() { return characterImage; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }

    public PlayerView(String name, double x, double y, double[] velocity, Color color) {

        this.centerX = x;
        this.centerY = y;

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

    }

    public void render(double x, double y, double[] velocity) {

        this.centerX = x;
        this.centerY = y;
        this.velocity = velocity;

        setLayoutX(centerX - (getPrefWidth()/2));
        setLayoutY(centerY - (getPrefHeight()/2));

        if(velocity[0] < 0) this.characterImage.setScaleX(-1);
        if(velocity[0] > 0) this.characterImage.setScaleX(1);

    }
}
