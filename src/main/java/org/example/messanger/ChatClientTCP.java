package org.example.messanger;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;

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
                    switch (received)
                    {
                        case User user:
                            this.user = user;
                            Platform.runLater(() -> app.receiveUserFromServer(user));
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
                        case ServerResponse response:
                            //server.editAnswer(request);
                            break;
                        case String str:
                            Platform.runLater(()->app.receiveJoinGreetings(str));
                            System.out.println("Client: receiveJoinGreetings: " + str);
                            break;
                        default:
                            System.out.println("Received wrong type");
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
