package Game.Meeting;

import Game.Player.PlayerInfo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Chat extends StackPane {

    // view Stuff
    private VBox messagesContainer;
    private VBox chatArea;
    private ScrollPane scrollPane;
    private TextField inputField;

    // logic stuff
    private Function<String,Void> sendMessageFunction;
    private Function<String,Void> voteForFunction;
    private Map<String, PlayerInfo> playerInfoMap = new HashMap<>();
    private boolean hasVoted;


    // logic functions
    public void addSendMessageFunction(Function<String,Void> sendMessageFunction){
        this.sendMessageFunction = sendMessageFunction;
    }

    public void addVoteForFunction(Function<String, Void> voteForFunction) {
        this.voteForFunction = voteForFunction;
    }

    public void addPlayer(String playerName, PlayerInfo playerInfo){
        System.out.println("Added player " + playerName);
        playerInfoMap.put(playerName, playerInfo);
    }

    public void removePlayer(String playerName, PlayerInfo playerInfo){
        playerInfoMap.remove(playerName);
    }

    public void killPlayer(String playerName){
        playerInfoMap.get(playerName).isAlive = false;
    }

    public void processInput(){
        // read input
        String input = inputField.getText();

        // case 1: voting command
        if (input!= null && input.trim().startsWith("/vote")){
            if (hasVoted){
                addPrivateMessage("You have already voted already!");
            }
            String[] parts = input.trim().split("\\s+");

            if (parts.length == 2){
                if (!playerInfoMap.containsKey(parts[1])){
                    addPrivateMessage("The player " + parts[1] + " does not exist.");
                } else if (!playerInfoMap.get(parts[1]).isAlive) {
                    addPrivateMessage("The player " + parts[1] + " is dead and cannot be voted.");
                } else {
                    voteForFunction.apply(parts[1]);
                }
            } else {
                addPrivateMessage("Wrong syntax. Usage: /vote [playerName]");
            }
        }else if (input != null && !input.trim().isEmpty()) {
            sendMessageFunction.apply(input);

            inputField.clear();
        }
        inputField.clear();
    }

    // view functions
    public void initialize(){

        hide();

        // styling
        getStyleClass().add("chat-background");
        Image backgroundImage = new Image("Chat/chatBackground.png");
        ImageView bgView = new ImageView(backgroundImage);
        bgView.setPreserveRatio(true);

        maxWidthProperty().bind(bgView.fitWidthProperty());
        maxHeightProperty().bind(bgView.fitHeightProperty());

        bgView.fitHeightProperty().bind(((MeetingView) getParent()).heightProperty().multiply(0.87));
        setAlignment(Pos.CENTER_LEFT);


        // Scrollable area for messages
        chatArea = new VBox(5);
        chatArea.setPadding(new Insets(10));
        chatArea.maxWidthProperty().bind(widthProperty().multiply(0.8));
        chatArea.maxHeightProperty().bind(heightProperty().multiply(0.8));
        chatArea.setAlignment(Pos.BOTTOM_CENTER);

        messagesContainer = new VBox(10); // 10 px spacing
        messagesContainer.setPadding(new Insets(10));
        messagesContainer.setAlignment(Pos.BOTTOM_CENTER);
        messagesContainer.getStyleClass().add("message-container");

        scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("chat-scroll");

//        messagesContainer.maxWidthProperty().bind(scrollPane.widthProperty());
//        messagesContainer.maxHeightProperty().bind(scrollPane.heightProperty());

        // 2) An input area at the bottom
        inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.getStyleClass().add("my-text-field");

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");

        HBox inputBox = new HBox(5, inputField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #23272A;");

        // Make TextField take all available space
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setMaxWidth(Double.MAX_VALUE);

        // Add elements to chat area
        chatArea.getChildren().addAll(scrollPane, inputBox);
        getChildren().addAll(bgView, chatArea);
        setAlignment(chatArea, Pos.BOTTOM_CENTER);

        // Event handlers
        sendButton.setOnAction(e -> processInput());
        // Pressing ENTER in the text field also sends
        inputField.setOnAction(e -> processInput());
    }

    public void show() {
        inputField.requestFocus();
        this.setVisible(true);
        this.setDisable(false);
    }

    public void hide() {
        this.setVisible(false);
        this.setDisable(true);
    }


    public void addMessage(String playerName, String message, Color nameLabelColor){
        Platform.runLater(() -> {
            MessageBox messageBox = new MessageBox(playerName, message, nameLabelColor);
            messagesContainer.getChildren().add(messageBox);
            messageBox.setFillWidth(true);
            scrollPaneToBottom();
        });

    }

    public void addPrivateMessage(String message){
        Platform.runLater(() -> {
            MessageBox messageBox = new MessageBox("Private message", message, Color.RED);
            messagesContainer.getChildren().add(messageBox);
            messageBox.setFillWidth(true);
            scrollPaneToBottom();
        });
    }

    private void scrollPaneToBottom() {
        scrollPane.setVvalue(Double.MAX_VALUE);
    }
}

class MessageBox extends VBox {

    MessageBox(String name, String message, Color nameLabelColor){
            getStyleClass().add("message-box");

            Label nameLabel = new Label(name);
            nameLabel.setTextFill(nameLabelColor);
            nameLabel.getStyleClass().add("name-label");

            Label msgLabel = new Label(message);
            msgLabel.setWrapText(true);
            msgLabel.getStyleClass().add("message-bubble");
            msgLabel.setMaxWidth(250); // Limit message bubble width
            msgLabel.setPadding(new Insets(5, 10, 5, 10)); // Padding for bubble
            msgLabel.setStyle("-fx-background-color: #7289DA; -fx-text-fill: white; -fx-background-radius: 10;");

            getChildren().addAll(nameLabel, msgLabel);
            setAlignment(Pos.TOP_LEFT); // Align to the left by default
            setPadding(new Insets(5));
    }



}
