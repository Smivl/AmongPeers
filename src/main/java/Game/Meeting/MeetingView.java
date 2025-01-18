package Game.Meeting;

import Game.Player.PlayerInfo;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.function.Function;

public class MeetingView extends StackPane {
    private Chat chat = new Chat();
    // private VoteView voteView = new VoteView();
    private Scene scene;

    public MeetingView(Scene scene){
        this.scene = scene;
    }

    public void addSendMessageFunction(Function<String,Void> sendMessageFunction){
        chat.addSendMessageFunction(sendMessageFunction);
    }

    public void addVoteForFunction(Function<String,Void> voteForFunction){
        // voteView.addVoteForFunction(voteForFunction);
        chat.addVoteForFunction(voteForFunction);
    }

    public void initialize(){
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

    public void addPlayersInfo(String s, PlayerInfo playerInfo) {
        chat.addPlayer(s, playerInfo);
    }

    public void killPlayer(String s){
        chat.killPlayer(s);
    }
}
