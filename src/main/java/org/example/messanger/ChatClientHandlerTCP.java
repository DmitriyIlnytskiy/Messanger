package org.example.messanger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//allows the server to handle multiple clients concurrently
//the server's representative for each connected client
public class ChatClientHandlerTCP implements Runnable{
    private Socket clientSocket;
    private ChatServerTCP server;

    private User user;

    //ObjectOutputStream converts the message object into a stream of bytes
    private ObjectOutputStream outputStream;
    //ObjectInputStream is used to deserialize Java objects received from a network connection
    private ObjectInputStream inputStream;


    public ChatClientHandlerTCP(Socket clientSocket, ChatServerTCP server, User user,  ObjectInputStream inputStream, ObjectOutputStream outputStream)
    {
        this.user = user;
        this.clientSocket = clientSocket;
        this.server = server;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
//        try {
//            //Java's serialization requires the ObjectOutputStream to be created before ObjectInputStream
//            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
//
//        }catch (IOException  e)
//        {
//            e.printStackTrace();
//            closeAll(clientSocket, outputStream, inputStream);
//        }
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

    @Override
    public void run() {

        try {
            //client waits until run() starts before creating the input stream, ensuring proper data flow.
            //inputStream = new ObjectInputStream(clientSocket.getInputStream());
            while (clientSocket.isConnected()) {
                if(inputStream == null)
                {
                    System.out.println("ClientHandler: run(): ObjectInputStream = null");
                    break;
                }

                Object received;
                try {
                    received = inputStream.readObject();
                    System.out.println("ChatClientHandlerTCP: " + user.getName() + " : Received object of type: " + received.getClass().getName());
                } catch (java.io.EOFException e) {
                    System.out.println("ChatClientHandlerTCP: Server closed the connection.");
                    break;
                }

                switch (received)
                {
                    case Messageable baseMessage:
                        server.broadcastMessage(this, baseMessage);
                        break;
                    case Chat chat:
                        server.broadcastChatUpdate(chat);
                        break;
                    case EditRequest editRequest:
                        server.editResponse(this, editRequest);
                        break;
                    case DeleteRequest deleteRequest:
                        server.deleteResponse(this, deleteRequest);
                        break;
                    default:
                        System.out.println("Received wrong type");
                }
            }
        }catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }finally {
            closeAll(clientSocket, outputStream, inputStream);
            server.removeClient(this);
        }
    }
    public void sendMessageToClient(Messageable message)
    {
        System.out.println("ClientHandler: sendMessageToClient: " + user.getName());
        if(outputStream !=null)
        {
            try {
                outputStream.writeObject(message);
                outputStream.flush();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else System.out.println("ClientHandler: sendMessageToClient: ObjectOutputStream = null");
    }
    public void sendChatUpdateToClient(Chat chat) {
        //If the same Chat or BaseMessage object is sent again through ObjectOutputStream,
        // Java's serialization system remembers it and sends a reference token instead of full data.
        //Cloning ensures a new object reference and forces full serialization.

        if (outputStream != null) {
            System.out.println("ClientHandler: sendChatUpdateToClient: " + user.getName());
            try {
                // Deep clone messages
                List<Messageable> clonedMessages = new ArrayList<>();
                for (Messageable message : chat.getMessages()) {
                    if (message instanceof BaseMessage) {
                        clonedMessages.add(((BaseMessage) message).clone());
                    } else {
                        // fallback: just add as-is if not cloneable (optional)
                        clonedMessages.add(message);
                    }
                }

                // Optional: clone users if needed (usually not necessary unless users are mutable/shared)
                List<User> clonedUsers = new ArrayList<>(chat.getUsers());

                // Construct a new Chat instance with cloned data
                Chat clonedChat = new Chat(chat.getChatName(), clonedUsers, clonedMessages);

                // Reset stream to force object re-serialization
                outputStream.reset();  // VERY important!
                outputStream.writeObject(clonedChat);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("ClientHandler: sendChatUpdateToClient: ObjectOutputStream = null");
        }
    }
    public void sendJoinGreetingsToClient(String joinGreetings)
    {
        if(outputStream != null) {
            System.out.println("ClientHandler: sendJoinGreetingsToClient: " + user.getName() + ":" + joinGreetings);
            try {
                outputStream.writeObject(joinGreetings);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("ClientHandler: sendJoinLabelToClient: ObjectOutputStream = null");
    }

    public void setUserToClient(User user)
    {
        if(outputStream != null) {
            System.out.println("ClientHandler: sendJoinGreetingsToClient: " + user.getName() + ":" + user);
            try {
                outputStream.writeObject(user);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("ClientHandler: setUserForClient: ObjectOutputStream = null");
    }
    public void giveResponseToClient(ServerResponse serverResponse)
    {
        if(outputStream != null) {
            System.out.println("ClientHandler: giveResponseToClient: " + serverResponse.getInformation());
            try {
                outputStream.writeObject(serverResponse);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("ClientHandler: giveResponseToClient: ObjectOutputStream = null");
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null || this.getClass() != obj.getClass()) return false;
        //comparing Users
        else if(this.getUser().equals(((ChatClientHandlerTCP) obj).getUser())) return true;
        else return false;
    }

    public void start()
    {
        new Thread(this).start();
    }

}
