package org.example.messanger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.example.messanger.ChatServerTCP.PORT;
import static org.example.messanger.ChatServerTCP.serverAdress;

public class MessangerApp extends Application {
    private Chat chat         = new Chat("Chat with GUI");
    private VBox messageList  = new VBox(10);
    private Popup messageMenu = new Popup();
    private Messageable clickedMessage;
    private Label userNameLabel;
    private User user;
    private ChatClientTCP client;
    // using CompletableFuture fixed race condition by explicitly waiting for the User object to be received before executing UI code
    // in receiveChatFromServer that depended on User
    // Receiving the User and the initial Chat object from the server are ASYNCHRONOUS operations happening on a background thread, so
    // Without explicit synchronization, the receiveChatFromServer method INVOKED and try to use the USER field in MessangerApp BEFORE
    // the receiveUserFromServer method had a chance to receive and SET USER field. This led to the NullPointerException
    private CompletableFuture<User> userFuture = new CompletableFuture<>();

    public void start(Stage primaryStage)
    {
        userNameLabel = new Label("Client: Waiting for input");
        // ScrollPane for wrap messageList(create scrollable interface)
        ScrollPane messageScrollPane = new ScrollPane(messageList); // Wrap messageList in ScrollPane
        messageScrollPane.setPrefHeight(300);
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScrollPane.setFitToWidth(true);

        VBox scrollableMessageList = new VBox(messageScrollPane);
        scrollableMessageList.setPadding(new Insets(5, 10, 5, 10)); // 5 pixels top/bottom, 10 pixels left/right

        client = new ChatClientTCP(serverAdress, PORT, this);

        TextField messageField = new TextField();

        HBox sendButtons = new HBox(10);
        sendButtons.setPadding(new Insets(5, 10, 5, 10)); // 5 pixels top/bottom, 10 pixels left/right

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
        sendTextMessageButton.setOnAction(     e -> sendMessage(messageField,"Text"));
        sendImageMessageButton.setOnAction(    e -> sendMessage(messageField,"Image"));
        sendVoiceMessageButton.setOnAction(    e -> sendMessage(messageField,"Voice"));
        sendFileMessageButton.setOnAction(     e -> sendMessage(messageField,"File"));
        sendLocationMessageButton.setOnAction( e -> sendMessage(messageField,"Location"));
        sendContactMessageButton.setOnAction(  e -> sendMessage(messageField,"Contact"));

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
        saveButton.setPadding(new Insets(5, 10, 5, 10)); // 5 pixels top/bottom, 10 pixels left/right
        Button loadButton = new Button("Load from File");
        loadButton.setPadding(new Insets(5, 10, 5, 10)); // 5 pixels top/bottom, 10 pixels left/right

        saveButton.setOnAction(e->handleSaveToFile(fileTextField.getText().trim()));
        loadButton.setOnAction(e->handleLoadFromFile(fileTextField.getText().trim()));

        //layout
        VBox root = new VBox(10);
        // userName Label initialized in method when user is verified on server and received on client side
        root.getChildren().addAll( scrollableMessageList, userNameLabel, new Label("Write your message") ,
                messageField,
                sendButtons,
                new Label("File Name:"), fileTextField, saveButton,
                loadButton
                //statusLabel
        );

        // Scene and Stage setup
        Scene scene = new Scene(root, 550, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Messenger");

        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(400);


        primaryStage.show();

        //Future
        Platform.runLater(this::validateNewUser);
    }

    public void setUser(User user)
    {
        this.user = user;
        Platform.runLater(()-> {
            //compllete future
            //userFuture.complete(user);
            // Update the UI label when server send user
            userNameLabel.setText("Your name is : " + user.getName() + " (ID: " + user.getId() + ")");
        });
    }

    public void receiveMessageFromServer(Messageable message)
    {
        //Wrap the UI updates within the receiveChatFromServer() method inside a Platform.runLater() call
        //modify the JavaFX messageList from a background thread (the startListening() thread), is not allowed.
        Platform.runLater(()-> {
            chat.addMessage(message);
            messageList.getChildren().add(createMessageStackPane(message));
        });

    }
    public void receiveChatFromServer(Chat updatedChat) {
            Platform.runLater(() -> {
                System.out.println("Client (" + user.getName() + "): receiveChatFromServer called. Chat object: " + updatedChat);
                System.out.println("Client (" + user.getName() + "): Number of messages in updatedChat: " + updatedChat.getMessages().size());
                //dynamically decide whether to update INCREAMENTALLY or redraw EVERYTHING
                /*if (helperCompareCurrentAndNewChatState(updatedChat)) {
                    this.chat = updatedChat;
                    updateMessageListIncrementally(updatedChat); // For minor edits
                    System.out.println("Client (" + user.getName() + "): receiveChatFromServer: helperCompareCurrentAndNewChatState(redraw minor change) finished");
                } else {

                }*/
                this.chat = updatedChat;
                //debug
                for (Messageable msg : updatedChat.getMessages()) {
                    System.out.println("Client DEBUG: Message ID " + msg.getMessageId() + " content: " + msg.render());
                }
                updateMessageDisplay(); // For major changes
                System.out.println("Client (" + user.getName() + "): receiveChatFromServer: updateMessageDisplay(redraw all) finished");
                System.out.println("Client (" + user.getName() + "): receiveChatFromServer finished");
            });
    }
    private boolean helperCompareCurrentAndNewChatState(Chat newChat)
    {
        if (chat == null || newChat == null) return false;
        List<Messageable> currentMessages = chat.getMessages();
        List<Messageable> newMessages = newChat.getMessages();

        // If size differs, full redraw
        if (currentMessages.size() != newMessages.size()) return false;

        // Check if messages are the same objects or IDs match
        for (int i = 0; i < currentMessages.size(); i++) {
            if (currentMessages.get(i).getMessageId() != newMessages.get(i).getMessageId()) {
                return false;
            }
        }
        return true;
    }
    private void updateMessageListIncrementally(Chat updatedChat)
    {
        for (Messageable updatedMessage : updatedChat.getMessages()) {
            updateMessageList(updatedMessage);
        }
        System.out.println("Client: ("+user.getName()+") updateMessageListIncrementally ");
    }

    private void updateMessageDisplay() {
        if (user == null) {
            System.out.println("Client: updateMessageDisplay called before user was received");
            return;
        }
        messageList.getChildren().clear();
        for (Messageable message : chat.getMessages()) {
            messageList.getChildren().add(createMessageStackPane(message));
        }
    }

    public void receiveJoinGreetings(String joinGreetings)
    {
        Platform.runLater(()-> {
            Label label = new Label(joinGreetings);
            StackPane wrapper = new StackPane(label);
            messageList.getChildren().add(wrapper);
        });
    }

    private void validateNewUser()
    {
        userNameLabel.setText("Your name is : Waiting for input..."); // Initialize the Label
        while (true) //when user types name -> return from here
        {
            TextInputDialog dialog = new TextInputDialog("author");
            dialog.setTitle("Authorname Input");
            dialog.setHeaderText("Enter your authorname");
            dialog.setContentText("Authorname:");

            //wrap data for avoiding null-checks
            Optional<String> result = dialog.showAndWait();


            if (result.isPresent() && !result.get().trim().isEmpty()) {
                userNameLabel.setText("Your name is : Waiting for server...");
                String proposedName = result.get().trim();

                userFuture = client.sendUserNameToServer(proposedName);
                try {
                    User validatedUser = userFuture.get(5, TimeUnit.SECONDS); // Wait for server response

                    if (validatedUser != null) {
                        setUser(validatedUser); // Complete
                        return; // Exit
                    } else {
                        showError("User Name \"" + proposedName + "\" already exists! Please try again.");
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    showError("Error communicating with the server: " + e.getMessage());
                }
            }
            else {
                showError("Authorname input canceled or empty. Please try again.");
                // delay to avoid excessive looping
                try {
                    Thread.sleep(500); // 0.5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleDelete()
    {
        if (clickedMessage == null) return;
        if (!clickedMessage.getUser().equals(user)) {
            showError("You can only delete your own messages.");
            return;
        }

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
        clickedMessage = null;
    }


    private void handleEdit()
    {
        if(clickedMessage == null) return;
        if (!clickedMessage.getUser().equals(user)) {
            showError("You can only edit your own messages.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(clickedMessage.showData());
        dialog.setTitle("Edit Message");
        dialog.setHeaderText("Enter new message content:");
        dialog.setContentText("Message:");

        dialog.showAndWait();

        client.sendRequestToServer(new EditRequest(user,clickedMessage,dialog.getEditor().getText()));

        messageMenu.hide();
        clickedMessage = null;
    }

//    private void updateMessageList(Messageable updatedMessage)
//    {
//        System.out.println("Client: ("+user.getName()+") updateMessageList ");
//        for (Node node : messageList.getChildren()) {
//            if(node instanceof StackPane messagePane) {
//                // Find Label by message ID
//                Label messageLabel = (Label) messagePane.lookup("#" + updatedMessage.getMessageId());
//                System.out.println("Client: ("+user.getName()+") updateMessageList: message ID: " + updatedMessage.getMessageId());
//
//                if (messageLabel != null) {
//                    String previous_state = messageLabel.getText();
//                    //if same, do not change
//                    if (!previous_state.equals(updatedMessage.render())) {
//                        messageLabel.setText(updatedMessage.render());
//                        showSuccess("Message Updated");
//                    }
//                    return;
//                }
//            }
//        }
//
//    }
private void updateMessageList(Messageable updatedMessage) {
    System.out.println("Client: (" + user.getName() + ") updateMessageList");

    for (Node node : messageList.getChildren()) {
        if (node instanceof StackPane messagePane) {
            for (Node child : messagePane.getChildren()) {
                if (child instanceof Label messageLabel) {
                    if (messageLabel.getId() != null && messageLabel.getId().equals(Integer.toString(updatedMessage.getMessageId()))) {

                        System.out.println("    Client: updateMessageList: (" + user.getName() + ") Found message label for ID: " + updatedMessage.getMessageId());

                        String previous_state = messageLabel.getText();
                        if (!previous_state.equals(updatedMessage.render())) {

                            System.out.println("    Client: updateMessageList: Previous: " + previous_state);
                            System.out.println("    Client: updateMessageList: Updated : " + updatedMessage.render());

                            messageLabel.setText(updatedMessage.render());
                            showSuccess("Message Updated");
                        }
                        System.out.println("    Client: updateMessageList: Previous == Updated? " + previous_state.equals(updatedMessage.render()));
                        return;
                    }
                }
            }
        }
    }

    System.out.println("    Client: (" + user.getName() + ") Message label NOT found for ID: " + updatedMessage.getMessageId());
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
                Chat loadedChat = Chat.loadFromFile(fileName);
                if (loadedChat != null) {
                    chat = loadedChat;
                    messageList.getChildren().clear();

                    for (Messageable message : chat.getMessages()) {
                        messageList.getChildren().add(createMessageStackPane(message));
                    }
                    showSuccess("Loaded");
                }
                else
                    showError("Failed to load chat");

            }
        }
    }

    private void sendMessage(TextField messageField, String type) {

        if(messageField.getText().isEmpty())
            showError("Message field cannot be empty!");

        else {
            String messageContent = messageField.getText();
            Date date = new Date();

            Messageable message;
            switch (type) {
                case "Text":
                    message = new TextMessage(user, date, messageContent);
                    break;
                case "Image":
                    message = new ImageMessage(user, date, messageContent);
                    break;
                case "Voice":
                    message = new VoiceMessage(user, date, messageContent);
                    break;
                case "File":
                    message = new FileMessage(user, date, messageContent);
                    break;
                case "Location":
                    message = new LocationMessage(user, date, messageContent);
                    break;
                case "Contact":
                    message = new ContactMessage(user, date, messageContent);
                    break;
                default:
                    return;
            }

            //send to server
            client.sendMessageToServer(message);
            //adding message to local Chat
            //chat.addMessage(message);
            //adding message to (VBox) - GUI list
            //messageList.getChildren().add(createMessageStackPane(message));

            messageField.clear();
        }
    }

    //Creating GUI message(clickable)
    private StackPane createMessageStackPane(Messageable message)
    {
        System.out.println("Client: createMessageStackPane: rendering message ID " + message.getMessageId() + ": " + message.render());

        StackPane messageStackPane = new StackPane();
        Rectangle background = new Rectangle(200,60, Color.GRAY);

        String displayText = "Unknown User";
        if (message.getUser() != null) {
            displayText = message.render();
        } else {
            System.err.println("Warning: Message with ID " + message.getMessageId() + " has a null User.");
        }
        Label text = new Label(displayText);
        //setting id of message to label for uniquely connect each message with each StackPane
        text.setId(Integer.toString(message.getMessageId()));

        messageStackPane.getChildren().addAll(background, text);

        messageStackPane.setOnMouseClicked(event -> {
            clickedMessage = message;
            // event.getScreenX() and event.getScreenY() are used to position the Popup relative to the screen, not the window
            if(message.getUser().equals(user)) {
                messageMenu.show(messageStackPane, event.getScreenX(), event.getScreenY());
            }
        });

        return messageStackPane;
    }

    public void showSuccess(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Success: " + message);
        alert.showAndWait();

    }

    public void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Error: " + message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
