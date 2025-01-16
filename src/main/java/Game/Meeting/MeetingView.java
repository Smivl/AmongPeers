package Game.Meeting;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.function.Function;

public class MeetingView extends StackPane {
    private Chat chat;

    public MeetingView(Scene scene, Function<String,Void> sendMessageFunction){
        chat = new Chat(sendMessageFunction);
        initialize(scene);
    }

    private void initialize(Scene scene){
        hide();
        getStyleClass().add("tablet-background");

        setAlignment(this, Pos.CENTER);

        Image backgroundImage = new Image("Chat/tablet.png");
        ImageView bgView = new ImageView(backgroundImage);
        bgView.setPreserveRatio(true);

        maxWidthProperty().bind(bgView.fitWidthProperty());
        maxHeightProperty().bind(bgView.fitHeightProperty());

        bgView.fitHeightProperty().bind(
                scene.heightProperty().multiply(0.87));

        getChildren().addAll(bgView, chat);
        chat.initialize();
    }

    public void addMessage(String playerName, String message, Color color) {
        chat.addMessage(playerName, message, color);
    }

    public void show() {
        this.setVisible(true);
        this.setDisable(false);
        chat.show();
    }

    public void hide() {
        this.setVisible(false);
        this.setDisable(true);
    }

}
