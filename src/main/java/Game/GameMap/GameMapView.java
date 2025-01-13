package Game.GameMap;

import Game.GameCharacter.GameCharacterView;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class GameMapView extends Pane {

    private ImageView mapImage;
    private Scene scene;

    public GameMapView(Scene scene) {
        this.scene = scene;
        this.mapImage = new ImageView(new Image("map.png"));
        this.getChildren().add(this.mapImage);
    }

    public void render(GameCharacterView player) {

        double offsetX = this.scene.getWidth() / 2.0 - player.getCenterX();
        double offsetY = this.scene.getHeight() / 2.0 - player.getCenterY();
        this.setTranslateX(offsetX);
        this.setTranslateY(offsetY);

    }
}
