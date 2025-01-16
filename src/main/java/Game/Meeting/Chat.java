package Game.Meeting;

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

import java.util.function.Function;

public class Chat extends StackPane {

    private VBox messagesContainer; // 5 px spacing
    private VBox chatArea;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Function<String,Void> sendMessageFunction;

    public Chat(Function<String,Void> sendMessageFunction){
        this.sendMessageFunction = sendMessageFunction;
    }

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
        sendButton.setOnAction(e -> sendMessage());
        // Pressing ENTER in the text field also sends
        inputField.setOnAction(e -> sendMessage());
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

    public void sendMessage(){
        String text = inputField.getText();
        if (text != null && !text.trim().isEmpty()) {
            // 1) Send message to the network (or your game logic)
            //    e.g., player.getCharacter().getPlayerSpace().put(...)
            //    or however you've set up your chat protocol

            sendMessageFunction.apply(text);

            // 2) Clear the input field
            inputField.clear();
        }
    }

    public void addMessage(String playerName, String message, Color nameLabelColor){
        MessageBox messageBox = new MessageBox(playerName, message, nameLabelColor);
        messagesContainer.getChildren().add(messageBox);
        messageBox.setFillWidth(true);
        scrollPaneToBottom();
    }

    private void scrollPaneToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
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
