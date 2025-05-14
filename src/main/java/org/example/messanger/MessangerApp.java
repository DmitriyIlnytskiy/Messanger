package org.example.messanger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalTime;
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

    private ThemeManager.Theme currentTheme = ThemeManager.Theme.LIGHT; // Initial theme
    private Button themeToggleButton;
    private Scene scene; // Store the scene reference

    // using CompletableFuture fixed race condition by explicitly waiting for the User object to be received before executing UI code
    // in receiveChatFromServer that depended on User
    // Receiving the User and the initial Chat object from the server are ASYNCHRONOUS operations happening on a background thread, so
    // Without explicit synchronization, the receiveChatFromServer method INVOKED and try to use the USER field in MessangerApp BEFORE
    // the receiveUserFromServer method had a chance to receive and SET USER field. This led to the NullPointerException
    private CompletableFuture<User> userFuture = new CompletableFuture<>();

    public void start(Stage primaryStage)
    {
        userNameLabel = new Label("Client: Waiting for input");
        userNameLabel.setStyle("-fx-font-weight: bold;"); // Basic styling
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

        styleButton(sendTextMessageButton);
        styleButton(sendImageMessageButton);
        styleButton(sendVoiceMessageButton);
        styleButton(sendFileMessageButton);
        styleButton(sendLocationMessageButton);
        styleButton(sendContactMessageButton);

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

        HBox fileButtons = new HBox(saveButton, loadButton);

        styleButton(saveButton);
        styleButton(loadButton);

        saveButton.setOnAction(e->handleSaveToFile(fileTextField.getText().trim()));
        loadButton.setOnAction(e->handleLoadFromFile(fileTextField.getText().trim()));

        // Theme toggle button
        themeToggleButton = new Button("Dark Mode");

        styleButton(themeToggleButton);

        themeToggleButton.setOnAction(e -> toggleTheme());

        //layout
        VBox root = new VBox(10);
        // userName Label initialized in method when user is verified on server and received on client side
        root.getChildren().addAll( scrollableMessageList, userNameLabel, new Label("Write your message") ,
                messageField,
                sendButtons,
                new Label("File Name:"), fileTextField,
                fileButtons,
                themeToggleButton
                //statusLabel
        );

        // Scene and Stage setup
        scene = new Scene(root, 650, 600);

        // Apply initial theme
        ThemeManager.applyTheme(scene, currentTheme);


        primaryStage.setScene(scene);
        primaryStage.setTitle("Messenger");

        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(400);


        primaryStage.show();

        //Future
        Platform.runLater(this::validateNewUser);
    }

    private void toggleTheme() {
        currentTheme = (currentTheme == ThemeManager.Theme.LIGHT) ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT;
        ThemeManager.applyTheme(scene, currentTheme);
        themeToggleButton.setText(currentTheme == ThemeManager.Theme.DARK ? "Light Mode" : "Dark Mode");
        // Re-style existing messages after theme change
        for (Node node : messageList.getChildren()) {
            if (node instanceof StackPane) {
                StackPane messagePane = (StackPane) node;
                if (messagePane.getUserData() != null) {
                    applyMessageStyle(messagePane);
                } else {
                    // Apply a default style for join messages, which don't have user data
                    for (Node child : messagePane.getChildren()) {
                        if (child instanceof Label) {
                            ((Label) child).setTextFill(currentTheme == ThemeManager.Theme.DARK ? Color.WHITE : Color.BLACK);
                        }
                    }
                }
            }
        }
    }

    private void applyMessageStyle(StackPane messagePane) {
        if (messagePane.getUserData() instanceof Messageable) { // check the instance type
            Messageable message = (Messageable) messagePane.getUserData();
            boolean isOwnMessage = message.getUser().equals(user);

            Color bubbleColor = isOwnMessage
                    ? (currentTheme == ThemeManager.Theme.DARK ? Color.web("#2e8b57") : Color.LIGHTGREEN)
                    : (currentTheme == ThemeManager.Theme.DARK ? Color.web("#505050") : Color.LIGHTGRAY);

            Color textColor = (currentTheme == ThemeManager.Theme.DARK) ? Color.WHITE : Color.BLACK;
            Color timestampColor = (currentTheme == ThemeManager.Theme.DARK) ? Color.LIGHTGRAY : Color.GRAY;

            System.out.println("applyMessageStyle: messageId=" + message.getMessageId() +
                    ", isOwnMessage=" + isOwnMessage +
                    ", currentTheme=" + currentTheme +
                    ", bubbleColor=" + bubbleColor +
                    ", textColor=" + textColor);


            // Traverse: StackPane (messagePane) -> HBox -> StackPane -> Region + VBox
            for (Node outer : messagePane.getChildren()) {
                if (outer instanceof HBox hbox) {
                    for (Node inner : hbox.getChildren()) {
                        if (inner instanceof StackPane bubble) {
                            for (Node bubbleChild : bubble.getChildren()) {
                                if (bubbleChild instanceof Region region &&
                                        "bubble-background".equals(region.getId())) {
                                    region.setBackground(new Background(new BackgroundFill(
                                            bubbleColor, new CornerRadii(12), Insets.EMPTY
                                    )));
                                } else if (bubbleChild instanceof VBox vbox) {
                                    for (Node content : vbox.getChildren()) {
                                        if (content instanceof Label label) {
                                            if (label.getFont().getSize() <= 11) {
                                                label.setTextFill(timestampColor); // timestamp
                                            } else {
                                                label.setTextFill(textColor); // message
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            System.out.println("applyMessageStyle: messagePane.getUserData() is NOT Messageable");
        }
    }


    // Add solid black border to all buttons:
    private void styleButton(Button button) {

        String buttonBorder = (currentTheme == ThemeManager.Theme.DARK) ? "-fx-border-color: white;" : "-fx-border-color: black;";
        button.setStyle(" -fx-border-width: 1px; -fx-padding: 5 10 5 10; -fx-background-color: transparent; -fx-border-radius: 15; " + buttonBorder);
    }
    /////////////////////////

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
            //applyMessageStyle(wrapper);
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

    private void handleDelete() {
        if (clickedMessage == null) return;

        // Check if the message belongs to the current user
        if (!clickedMessage.getUser().equals(user)) {
            showError("You can only delete your own messages.");
            return;
        }

        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Message");
        confirmation.setHeaderText("Confirm deletion");
        confirmation.setContentText("Are you sure you want to delete this message?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                // Create a DeleteRequest and send to the server
                client.sendRequestToServer(new DeleteRequest(user, clickedMessage));

                // Remove the StackPane from messageList (client-side UI update)
                for (int i = 0; i < messageList.getChildren().size(); i++) {
                    StackPane messagePane = (StackPane) messageList.getChildren().get(i);
                    Label messageLabel = (Label) messagePane.lookup("#" + clickedMessage.getMessageId());
                    if (messageLabel != null) {
                        // Remove the message from the UI
                        messageList.getChildren().remove(messagePane);
                        break;
                    }
                }

                // Hide the message menu (optional)
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
    private StackPane createMessageStackPane(Messageable message) {
        System.out.println("Client: createMessageStackPane: rendering message ID " + message.getMessageId() + ": " + message.render());

        // Message content and label
        String textContent = message.render();
        Label textLabel = new Label(textContent);
        textLabel.setId(Integer.toString(message.getMessageId()));
        textLabel.setFont(Font.font("Arial", 14));
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(300); // Control wrapping width
        textLabel.setTextFill((currentTheme == ThemeManager.Theme.DARK) ? Color.WHITE : Color.BLACK);
        textLabel.setPadding(new Insets(10)); // Padding inside the bubble


        // Timestamp label
        LocalTime time = message.getTimestamp().toLocalTime(); // assuming message.getTimestamp() returns LocalDateTime
        String formattedTime = String.format("%02d:%02d", time.getHour(), time.getMinute());
        Label timestampLabel = new Label(formattedTime);
        timestampLabel.setFont(Font.font("Arial", FontPosture.REGULAR, 10));
        timestampLabel.setTextFill((currentTheme == ThemeManager.Theme.DARK) ? Color.LIGHTGRAY : Color.GRAY);

        // Align timestamp to bottom-right inside the bubble
        HBox timestampContainer = new HBox(timestampLabel);
        timestampContainer.setAlignment(Pos.BOTTOM_RIGHT);
        timestampContainer.setPadding(new Insets(0, 5, 10, 5)); // Bottom-right padding

        // Combine message and timestamp vertically
        VBox messageBox = new VBox(4); // spacing between text and timestamp
        messageBox.getChildren().addAll(textLabel, timestampContainer);
        messageBox.setPadding(new Insets(10)); // Inner padding for background
        messageBox.setMaxWidth(220); // Cap max width for wrapping
        messageBox.setAlignment(Pos.BOTTOM_RIGHT); // Align timestamp properly


        // Measure the label's size AFTER applying font and wrapping
        Text tempText = new Text(textContent);
        tempText.setFont(textLabel.getFont());
        tempText.setWrappingWidth(300);
        tempText.setLineSpacing(textLabel.getLineSpacing());

        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(
                message.getUser().equals(user)
                        ? (currentTheme == ThemeManager.Theme.DARK ? Color.web("#2e8b57") : Color.LIGHTGREEN)
                        : (currentTheme == ThemeManager.Theme.DARK ? Color.web("#505050") : Color.LIGHTGRAY),
                new CornerRadii(12),
                Insets.EMPTY
        )));
        background.setMinHeight(Region.USE_PREF_SIZE);
        background.setMinWidth(Region.USE_PREF_SIZE);
        background.setId("bubble-background");

        StackPane bubble = new StackPane(background, messageBox);
        bubble.setPadding(new Insets(5));
        bubble.setUserData(message);//for comparing users

        // Wrap in an HBox to align left/right based on sender
        HBox container = new HBox(bubble);
        container.setAlignment(message.getUser().equals(user) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.setPadding(new Insets(5, 10, 5, 10));

        // Handle message context menu
        bubble.setOnMouseClicked(event -> {
            clickedMessage = message;
            if (message.getUser().equals(user)) {
                messageMenu.show(bubble, event.getScreenX(), event.getScreenY());
            }
        });

        return new StackPane(container); // Return wrapped in a StackPane if needed
    }





    /// ///////////////////////////
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
