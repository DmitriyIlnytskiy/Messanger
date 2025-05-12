package org.example.messanger;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;

// the application's representative on the client side.
public class ChatClientTCP {

    private MessangerApp app;
    private User user;

    private Socket socket;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private CompletableFuture<User> usernameResultFuture;


    public ChatClientTCP(String serverAdress, int port, MessangerApp app)
    {
        this.app = app;
        try{
            System.out.println("Client: Connecting to server...");

            socket = new Socket(serverAdress, port);
            System.out.println("Client: Socket connected.");

            //Java's serialization requires the ObjectOutputStream to be created before ObjectInputStream
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Client: OutputStream created.");

            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Client: InputStream created.");

            startListening();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    private void startListening()
    {
        new Thread(()->{
            try{
                while (!socket.isClosed()) {
                    Object received = inputStream.readObject();
                    switch (received)
                    {
                        case UserIsValidResponse userValidResponse:
                            if (usernameResultFuture != null) {
                                //success
                                if (userValidResponse.isSuccess()) {
                                    this.user = userValidResponse.getUser();
                                    usernameResultFuture.complete(userValidResponse.getUser());
                                }
                                //failure
                                else {
                                    usernameResultFuture.complete(null);
                                }
                                usernameResultFuture = null;
                            }
                            break;
                        case Messageable baseMessage:
                            //Platform.runLater() ensures that the code is executed on the JavaFX Application Thread.
                            Platform.runLater(() -> app.receiveMessageFromServer(baseMessage));
                            System.out.println("Client: " + user.getName() + " :receiveMessageFromServer");
                            break;
                        case Chat chat:
                            Platform.runLater(() -> app.receiveChatFromServer(chat));
                            System.out.println("Client: " + user.getName() + " :receiveChatFromServer");
                            break;
                        case EditResponse response:
                            if (response.isSuccess()) {
                                System.out.println("Client: Edit successful: " + response.getInformation());
                                Platform.runLater(() -> app.showSuccess("Edit successful"));
                            } else {
                                System.out.println("Client: Edit failed: " + response.getInformation());
                                Platform.runLater(() -> app.showError("Edit failed"));
                            }
                            break;
                        case DeleteResponse response:
                            if (response.isSuccess()) {
                                System.out.println("Client: Delete successful: " + response.getInformation());
                                Platform.runLater(() -> app.showSuccess("Delete successful"));
                            } else {
                                System.out.println("Client: Delete failed: " + response.getInformation());
                                Platform.runLater(() -> app.showError("Delete failed"));
                            }
                            break;
                        case String str:
                            Platform.runLater(()->app.receiveJoinGreetings(str));
                            System.out.println("Client: receiveJoinGreetings: " + str);
                            break;
                        default:
                            System.out.println("Client: Received wrong type: " + received.getClass());
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                if (e instanceof EOFException) {
                    System.out.println("Client: Server closed the connection");
                }
                e.printStackTrace();
            }finally {
                closeAll(socket,outputStream,inputStream);
            }
        }).start();


    }
    private void closeAll(Socket socket, ObjectOutputStream out, ObjectInputStream in)
    {
        System.out.println("Close ALL");
        try {
            if (socket != null) socket.close();

            if(out != null) out.close();

            if(in != null) in.close();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public CompletableFuture<User> sendUserNameToServer(String userName)
    {
        //make sure to cancel any stale futures
        if (usernameResultFuture != null && !usernameResultFuture.isDone()) {
            usernameResultFuture.complete(null);
        }

        usernameResultFuture = new CompletableFuture<>();
        try{
            outputStream.writeObject(userName);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usernameResultFuture;
    }
    public void sendMessageToServer(Messageable message)
    {
        try {
            outputStream.writeObject(message);
            outputStream.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendRequestToServer(ClientRequest request)
    {
        try {
            outputStream.writeObject(request);
            outputStream.flush();

        } catch (SocketException e) {
            System.err.println("Client: Socket closed while sending request.");
        } catch (IOException e) {
            System.err.println("Client: IOException while sending request.");
            e.printStackTrace();
        }
    }

    //                   //TEST//                      //

    /*public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int port = 12345;

        // Launch a minimal JavaFX application
        Application.launch(TestClientApp.class, args);
    }

    public static class TestClientApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            // Create a dummy MessangerApp and User for testing
            MessangerApp dummyApp = new MessangerApp() {
                @Override
                public void start(Stage primaryStage) {
                    // Do nothing for testing
                }

                @Override
                public void receiveMessageFromServer(BaseMessage message) {
                    System.out.println("Received message: " + message.render());
                }

                @Override
                public void receiveChatFromServe(Chat updatedChat) {
                    System.out.println("Received chat update");
                }
            };

            User dummyUser = new User("TestClient");
            System.out.println(dummyUser.getName());

            ChatClientTCP client = new ChatClientTCP("localhost", 12345, dummyApp, dummyUser);

            // Send a test message
            client.sendMessageToServer(new TextMessage(dummyUser, new Date(),"Hello from test client."));
        }
    }
    */
}
