package org.example.messanger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.util.Date;

public class MessangerApp extends Application {
    private Chat chat         = new Chat("Chat with UI");
    //private Label statusLabel = new Label();
    private VBox messageList  = new VBox(10);
    private Popup messageMenu = new Popup();

    private BaseMessage clickedMessage;

    public void start(Stage primaryStage)
    {

        TextField authorField  = new TextField();
        TextField messageField = new TextField();

        HBox sendButtons = new HBox(10);
        Button sendTextMessageButton     = new Button("Send Text");
        Button sendImageMessageButton    = new Button("Send Image");
        Button sendVoiceMessageButton    = new Button("Send Voice");
        Button sendFileMessageButton     = new Button("Send File");
        Button sendLocationMessageButton = new Button("Send Location");
        Button sendContactMessageButton  = new Button("Send Contact");
        sendButtons.getChildren().addAll(
                sendTextMessageButton,
                sendImageMessageButton,
                sendVoiceMessageButton,
                sendFileMessageButton,
                sendLocationMessageButton,
                sendContactMessageButton
                );


        //Button actions
        sendTextMessageButton.setOnAction(     e -> sendMessage(authorField, messageField, messageList, "Text"));
        sendImageMessageButton.setOnAction(    e -> sendMessage(authorField, messageField, messageList, "Image"));
        sendVoiceMessageButton.setOnAction(    e -> sendMessage(authorField, messageField, messageList, "Voice"));
        sendFileMessageButton.setOnAction(     e -> sendMessage(authorField, messageField, messageList, "File"));
        sendLocationMessageButton.setOnAction( e -> sendMessage(authorField, messageField, messageList, "Location"));
        sendContactMessageButton.setOnAction(  e -> sendMessage(authorField, messageField, messageList, "Contact"));

        //Popup menu
        HBox menuItems = new HBox(5);
        Button menuButtonEdit = new Button("Edit");
        Button menuButtonDelete = new Button("Delete");
        menuItems.getChildren().addAll(menuButtonEdit, menuButtonDelete);
        messageMenu.getContent().add(menuItems);
        //hide popup menu when clicked on different element
        messageMenu.setAutoHide(true);

        menuButtonEdit.setOnAction(e->handleEdit());
        menuButtonDelete.setOnAction(e->handleDelete());

        // Work with files
        TextField fileTextField = new TextField();

        Button saveButton = new Button("Save to File");
        Button loadButton = new Button("Load from File");

        saveButton.setOnAction(e->handleSaveToFile(fileTextField.getText().trim()));
        loadButton.setOnAction(e->handleLoadFromFile(fileTextField.getText().trim()));



        //layout
        VBox root = new VBox(10);
        root.getChildren().addAll( messageList, new Label("Author Name:"), authorField, new Label("Write your message") ,
                messageField,
                sendButtons,
                new Label("File Name:"), fileTextField, saveButton,
                loadButton
                //statusLabel
        );

        // Scene and Stage setup
        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Messenger");

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(900);

        //scene.getStylesheets().add("styles.css");

        primaryStage.show();
    }


    private void handleDelete()
    {
        if (clickedMessage == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Message");
        confirmation.setHeaderText("Confirm deletion");
        confirmation.setContentText("Are you sure you want to delete this message?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                //delete from chat
                chat.getMessages().remove(clickedMessage);

                // Remove the StackPane from messageList
                for (int i = 0; i < messageList.getChildren().size(); i++) {
                    StackPane messagePane = (StackPane) messageList.getChildren().get(i);
                    Label messageLabel = (Label) messagePane.lookup("#" + clickedMessage.getMessageId());
                    if (messageLabel != null) {
                        //delete from messageList
                        messageList.getChildren().remove(messagePane);
                        break;
                    }
                }
                messageMenu.hide();
            }
        });
    }

    private void handleEdit()
    {
        if(clickedMessage == null) return;

        TextInputDialog dialog = new TextInputDialog(clickedMessage.showData());
        dialog.setTitle("Edit Message");
        dialog.setHeaderText("Enter new message content:");
        dialog.setContentText("Message:");

        dialog.showAndWait();

        switch (clickedMessage) {
            case TextMessage textMessage:
            {
                textMessage.setContent(dialog.getEditor().getText());
                updateMessageList(textMessage);
                break;
            }
            case ContactMessage contactMessage:
            {
                contactMessage.setContact(dialog.getEditor().getText());
                updateMessageList(contactMessage);
                break;
            }
            case ImageMessage imageMessage:
            {
                imageMessage.setImageUrl(dialog.getEditor().getText());
                updateMessageList(imageMessage);
                break;
            }
            case VoiceMessage voiceMessage:
            {
                voiceMessage.setAudioUrl(dialog.getEditor().getText());
                updateMessageList(voiceMessage);
                break;
            }
            case LocationMessage locationMessage:
            {
                locationMessage.setLocation(dialog.getEditor().getText());
                updateMessageList(locationMessage);
                break;
            }
            case FileMessage fileMessage:
            {
                fileMessage.setFileName(dialog.getEditor().getText());
                updateMessageList(fileMessage);
                break;
            }
            default:
            {
                showError("Unknown message type");
            }
            messageMenu.hide();
        }
    }

    private void updateMessageList(BaseMessage message)
    {
        for (int i = 0; i < messageList.getChildren().size(); i++) {
            StackPane messagePane = (StackPane) messageList.getChildren().get(i);
            // Find Label by message ID
            Label messageLabel = (Label) messagePane.lookup("#" + message.getMessageId());

            if (messageLabel != null) {
                messageLabel.setText(message.render());
                showSuccess("Message Updated");
                break;
            }
        }
    }

    private void handleSaveToFile(String fileName)
    {
        if(chat.getMessages().isEmpty())
            showError("Message list is empty");
        else if(fileName.isEmpty())
            showError("File name is empty");
        else
        {
            chat.saveToFile(fileName);
            showSuccess("Saved");
        }
    }
    private void handleLoadFromFile(String fileName)
    {
        if(fileName.isEmpty())
            showError("File name is empty");
        else
        {
            File file = new File(fileName);
            if (!file.exists()) {
                showError("File does not exist!");
            } else {
                Chat loadedChat = chat.loadFromFile(fileName);
                if (loadedChat != null) {
                    chat = loadedChat;
                    messageList.getChildren().clear();

                    for (BaseMessage message : chat.getMessages()) {
                        messageList.getChildren().add(createMessageStackPane(message));
                    }
                    showSuccess("Loaded");
                }
                else
                    showError("Failed to load chat");

            }
        }
    }

    private void sendMessage(TextField authorField, TextField messageField, VBox messageList, String type) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);

        String authorName = authorField.getText().trim();
        if (authorName.isEmpty()) {
            alert.setContentText("Author's name cannot be empty!");
            alert.showAndWait();
        }
        else if(messageField.getText().isEmpty())
        {
            alert.setContentText("Message field cannot be empty!");
            alert.showAndWait();
        }
        else {
            String messageContent = messageField.getText();
            String date = new Date().toString();

            BaseMessage message;
            switch (type) {
                case "Text":
                    message = new TextMessage(authorName, date, messageContent);
                    break;
                case "Image":
                    message = new ImageMessage(authorName, date, messageContent);
                    break;
                case "Voice":
                    message = new VoiceMessage(authorName, date, messageContent);
                    break;
                case "File":
                    message = new FileMessage(authorName, date, messageContent);
                    break;
                case "Location":
                    message = new LocationMessage(authorName, date, messageContent);
                    break;
                case "Contact":
                    message = new ContactMessage(authorName, date, messageContent);
                    break;
                default:
                    return;
            }

            //adding message to Chat
            chat.addMessage(message);
            //adding message to (VBox) - UI list
            messageList.getChildren().add(createMessageStackPane(message));

            messageField.clear();
        }
    }
    //Creating UI message(clickable)
    private StackPane createMessageStackPane(BaseMessage message)
    {
        StackPane messageStackPane = new StackPane();
        Rectangle background = new Rectangle(200,60, Color.GRAY);
        Label text = new Label(message.render());

        //setting id of message to label for uniquely connect each message with each StackPane
        text.setId(Integer.toString(message.getMessageId()));

        messageStackPane.getChildren().addAll(background, text);

        messageStackPane.setOnMouseClicked(event -> {
            clickedMessage = message;
            // event.getScreenX() and event.getScreenY() are used to position the Popup relative to the screen, not the window
            messageMenu.show(messageStackPane, event.getScreenX(), event.getScreenY());
        });

        return messageStackPane;
    }

    private void showSuccess(String message) {
        //.setText(message);
        //statusLabel.setTextFill(Color.GREEN);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Success: " + message);
        alert.showAndWait();

    }

    private void showError(String message) {
        //statusLabel.setText("Error: " + message);
        //.setTextFill(Color.RED);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Error: " + message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
