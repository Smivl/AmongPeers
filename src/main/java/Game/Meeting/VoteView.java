package Game.Meeting;

import Game.Player.PlayerInfo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class VoteView extends GridPane {
    private Function<String,Void> voteFor;
    private Map<String, PlayerInfo> playerInfoMap = new HashMap<>();
    private final int COLS = 3;
    private boolean hasVoted = false;

    public VoteView(){
    }

    public void addPlayersInfo(String playerName, PlayerInfo playerInfo){
        this.playerInfoMap.replace(playerName, playerInfo);
    }

    public void killPlayer(String playerName){
        this.playerInfoMap.get(playerName).isAlive = false;
    }

    public void addVoteForFunction(Function<String, Void> voteForFunction) {
        voteFor = voteForFunction;
    }

    public void initialize(){

        this.getChildren().clear();

        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.CENTER);

        int i = 0;
        // Alive players first
        for (Map.Entry<String,PlayerInfo> entry : playerInfoMap.entrySet()){
            if (!entry.getValue().isAlive) continue; // skip dead

            PlayerBox playerBox = new PlayerBox(entry.getKey(), entry.getValue());
            this.add(playerBox, i%COLS, i/COLS); // column and row

            i++;
        }
        // Dead players
        for (Map.Entry<String,PlayerInfo> entry : playerInfoMap.entrySet()){
            if (entry.getValue().isAlive) continue; // skip dead

            PlayerBox playerBox = new PlayerBox(entry.getKey(), entry.getValue());
            this.add(playerBox, i%COLS, i/COLS); // column and row

            i++;
        }
    }

    class PlayerBox extends HBox {

        PlayerBox(String playerName, PlayerInfo playerInfo) {
            super(10);
            Circle circle = new Circle();
            circle.setRadius(20);
            circle.setFill(playerInfo.color.getColor());

            Label nameLabel = new Label(playerName);

            Button voteButton = new Button("✔");
            voteButton.setStyle("-fx-background-color: #43B581; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            voteButton.setPrefSize(30, 30);
            voteButton.setDisable(true);
            voteButton.setVisible(false);

            // event handles
            voteButton.setOnMouseEntered(e -> {
                if(!hasVoted && playerInfo.isAlive){
                    voteButton.setVisible(true);
                    voteButton.setDisable(false);
                }
            });
            voteButton.setOnMouseExited(e -> {
                voteButton.setVisible(false);
                voteButton.setDisable(true);
            });
            voteButton.setOnMousePressed(e -> {
                voteFor.apply(playerName);
                hasVoted = true;
            });


            // Combine into Player Box
            this.setAlignment(Pos.CENTER);
            this.setPadding(new Insets(10));
            this.setStyle("-fx-background-color: #2C2F33; -fx-border-color: #7289DA; -fx-border-radius: 5; -fx-background-radius: 5;");
            this.getChildren().addAll(circle, nameLabel, voteButton);


        }

    }
}
