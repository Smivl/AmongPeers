package Game.Meeting;

import Game.Player.PlayerInfo;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.function.Function;

public class MeetingView extends StackPane {
    private Chat chat = new Chat();
    // private VoteView voteView = new VoteView();
    private Scene scene;

    public MeetingView(Scene scene, String name, PlayerInfo info){
        this.scene = scene;
        chat.addPlayer(name, info);
    }

    public void initialize(Function<String,Void> sendMessageFunction, Function<String,Void> voteForFunction){
        chat.addSendMessageFunction(sendMessageFunction);
        chat.addVoteForFunction(voteForFunction);

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
        System.out.println("Hello");
        chat.initialize();
    }

    public void addMessage(String playerName, String message) {
        chat.addMessage(playerName, message);
    }

    public void addServerMessage(String message) {
        chat.addServerMessage(message);
    }

    public void show() {
        this.setVisible(true);
        this.setDisable(false);
        chat.setDisable(false);
    }

    public void hide() {
        this.setVisible(false);
        this.setDisable(true);
    }

    public void addPlayersInfo(String s, PlayerInfo playerInfo) {
        chat.addPlayer(s, playerInfo);
    }

    public void killPlayer(String s){
        chat.killPlayer(s);
    }

    public void onKilled() {
        chat.onKilled();
    }
}
