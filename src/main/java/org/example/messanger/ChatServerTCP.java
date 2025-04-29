package org.example.messanger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChatServerTCP {
    public static final int PORT            = 12345;
    public static final String serverAdress = "localhost";

    private ServerSocket serverSocket;
    private List<ChatClientHandlerTCP> clients = new LinkedList<>();
    private Chat chat;
    private int userCount = 0;

    public ChatServerTCP(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
        chat = new Chat("Chat with GUI");
    }
    public ChatServerTCP(int port)
    {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            chat = new Chat("Chat with GUI");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     public void startServer()
     {
         //prevent the server from blocking the main application thread and to allow it to listen for incoming client connections
         new Thread(()-> {//define Runnable
             try {
                 while (!serverSocket.isClosed()) {
                     //when the server executes accept() - it is a blocking operation,
                     // it will pause (block) the current thread until a client attempts to connect
                     Socket clientSocket = serverSocket.accept();
                     System.out.println("Server: Client connected.");

                     ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                     System.out.println("Server: ObjectOutputStream created.");

                     // Receive User from Client
                     ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                     System.out.println("Server: ObjectInputStream created.");

                     String userName = (String) inputStream.readObject();

                     ////////////////
                     // Assign unique ID from server
                     User user = new User(userName, ++userCount);
                     ////////////////
                     System.out.println("Server: Client \"" + user.getName() + "\"  has connected");

                     chat.addUser(user);

                     ChatClientHandlerTCP clientHandler = new ChatClientHandlerTCP(clientSocket, this, user, inputStream, outputStream);
                     if (clientHandler != null) {
                         clients.add(clientHandler);
                         setUserForClient(clientHandler,user);
                        //new thread for managing communication with a single connected client
                         clientHandler.start();
                     } else {
                         System.out.println("Failed to create client handler for " + user.getName());
                     }
                     broadcastJoinGreetings(clientHandler);
                     broadcastChatUpdate(chat);
                 }
             } catch (IOException  | ClassNotFoundException e) {
                 e.printStackTrace();
                 closeServerSocket();
             }finally {
                 closeServerSocket();
             }
         }).start();

     }
     public void broadcastMessage(ChatClientHandlerTCP sender, Messageable receivedMessage)
     {
         if(clients == null)
             return;

         User senderUser = sender.getUser();
         Date timestamp = receivedMessage.getDate();
         String content;

         Messageable serverCreatedMessage;

         // Extract content based on the type of the received message
         switch (receivedMessage) {
             case TextMessage textMessage:
                 content = textMessage.getContent();
                 serverCreatedMessage = new TextMessage(senderUser, timestamp, content);
                 break;
             case ImageMessage imageMessage:
                 content = imageMessage.getImageUrl();
                 serverCreatedMessage = new ImageMessage(senderUser, timestamp, content);
                 break;
             case VoiceMessage voiceMessage:
                 content = voiceMessage.getAudioUrl();
                 serverCreatedMessage = new VoiceMessage(senderUser, timestamp, content);
                 break;
             case FileMessage fileMessage:
                 content = fileMessage.getFileName();
                 serverCreatedMessage = new FileMessage(senderUser, timestamp, content);
                 break;
             case LocationMessage locationMessage:
                 content = locationMessage.getLocation();
                 serverCreatedMessage = new LocationMessage(senderUser, timestamp, content);
                 break;
             case ContactMessage contactMessage:
                 content = contactMessage.getContact();
                 serverCreatedMessage = new ContactMessage(senderUser, timestamp, content);
                 break;
             default:
                 System.out.println("Server: Unknown message type received: " + receivedMessage.getClass().getName());
                 return;
         }
         if (serverCreatedMessage == null) {
             System.out.println("Server: serverCreatedMessage = null");
             return;
         }

         chat.addMessage(serverCreatedMessage);// Add the message to the chat

         System.out.println("Server: broadcastMessage");
         for(ChatClientHandlerTCP client : clients)
                 client.sendMessageToClient(serverCreatedMessage);
     }

     public void broadcastChatUpdate(Chat updatedChat)
     {
         chat = updatedChat;

         if(clients == null)
             return;
         System.out.println("Server: broadcastChatUpdate");
         for (ChatClientHandlerTCP client : clients)
             client.sendChatUpdateToClient(chat);
     }
     public void broadcastJoinGreetings(ChatClientHandlerTCP sender)
     {
         if(clients == null)
             return;

         String notifyClients = sender.getUser().getName() + " has joined to chat";
         String greetSender = "Welcome to chat: " + chat.getChatName();

         System.out.println("Server: broadcastJoinLabel");
         for(ChatClientHandlerTCP client : clients) {
             if (client.equals(sender))
                 client.sendJoinGreetingsToClient(greetSender);
              else
                 client.sendJoinGreetingsToClient(notifyClients);

         }

     }

     public void setUserForClient(ChatClientHandlerTCP reciever ,User user)
     {
         System.out.println("Server: sendUserToClient");
         reciever.setUserToClient(user);
     }

    public void removeClient(ChatClientHandlerTCP clientHandler) {
        clients.remove(clientHandler);
    }

     public void closeServerSocket()
     {
         try {
             if (serverSocket != null) {
                 serverSocket.close();
                 System.out.println("Server stopped.");
             }
         }catch (IOException e)
         {
             e.printStackTrace();

         }
     }
     public void editResponse(ChatClientHandlerTCP requester, EditRequest editRequest)
     {
         System.out.println("Server: EditRequest's message ID is: " + editRequest.getMessage().getMessageId());
         if(editRequest == null) {
             requester.giveResponseToClient(new EditResponse(false, "Edit request is null"));
             return;
         }
         else if(!requester.getUser().equals(editRequest.getRequester())) {
             requester.giveResponseToClient(new EditResponse(false, "Edit request's user is not a user who can be a requester"));
             return;
         }

         Messageable message = chat.findMessageById(editRequest.getMessage().getMessageId());
         System.out.println("Server: created message on server(for editing) ID is: " + message.getMessageId());

         switch (message) {
             case TextMessage textMessage:
                 textMessage.setContent(editRequest.getContent());
                 broadcastChatUpdate(chat);
                 break;
             case ContactMessage contactMessage:
                 contactMessage.setContact(editRequest.getContent());
                 broadcastChatUpdate(chat);
                 break;
             case ImageMessage imageMessage:
                 imageMessage.setImageUrl(editRequest.getContent());
                 broadcastChatUpdate(chat);
                 break;
             case VoiceMessage voiceMessage:
                 voiceMessage.setAudioUrl(editRequest.getContent());
                 broadcastChatUpdate(chat);
                 break;
             case LocationMessage locationMessage:
                 locationMessage.setLocation(editRequest.getContent());
                 broadcastChatUpdate(chat);
                 break;
             case FileMessage fileMessage:
                 fileMessage.setFileName(editRequest.getContent());
                 broadcastChatUpdate(chat);
                 break;
             default:
                  requester.giveResponseToClient(new EditResponse(false, "Unsupported message type for edit."));
         }
         requester.giveResponseToClient(new EditResponse(true,"message: " + message.getMessageId() + " edited!"));
     }
     public void deleteResponse(ChatClientHandlerTCP requester, DeleteRequest deleteRequest)
     {

     }

    public static void main(String[] args) {
        ChatServerTCP server = new ChatServerTCP(PORT);
        server.startServer();

    }


}
