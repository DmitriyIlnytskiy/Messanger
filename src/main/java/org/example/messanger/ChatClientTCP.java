package org.example.messanger;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Date;

// the application's representative on the client side.
public class ChatClientTCP {

    private MessangerApp app;
    private User user;

    private Socket socket;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;


    public ChatClientTCP(String serverAdress, int port, MessangerApp app, String userName)
    {
        this.app = app;
        try{
            System.out.println("Client: Connecting to server...");

            socket = new Socket(serverAdress, port);
            System.out.println("Client: Socket connected.");

            //Java's serialization requires the ObjectOutputStream to be created before ObjectInputStream
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Client: OutputStream created.");

            sendUserNameToServer(userName);

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
                    if(received instanceof User) {
                        this.user = (User) received;
                        Platform.runLater(() -> app.receiveUserFromServer((User) received));
                    }
                    else if (received instanceof BaseMessage) {
                        //Platform.runLater() ensures that the code is executed on the JavaFX Application Thread.
                        Platform.runLater(() -> app.receiveMessageFromServer((BaseMessage) received));
                        System.out.println("Client: " + user.getName() + " :receiveMessageFromServer");
                    }
                    else if (received instanceof Chat) {
                        Platform.runLater(() -> app.receiveChatFromServer((Chat) received));
                        System.out.println("Client: " + user.getName() + " :receiveChatFromServer");
                    }
                    else if(received instanceof String)
                    {
                        Platform.runLater(()->app.receiveJoinGreetings((String) received));
                        System.out.println("Client: receiveJoinLabel: " + ((String) received));
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

    public void sendUserNameToServer(String userName)
    {
        try{
            outputStream.writeObject(userName);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessageToServer(BaseMessage message)
    {
        try {
            outputStream.writeObject(message);
            outputStream.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
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
