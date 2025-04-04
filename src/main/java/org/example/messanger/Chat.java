package org.example.messanger;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat implements Serializable {
    private String chatName;
    private List<User> users;
    private Map<Integer, BaseMessage> messages;
    private static int count = 0;
    private int chatId;

    public Chat(String chatName)
    {
        chatId = ++count;
        this.chatName = chatName;
        users    = new ArrayList<>();
        messages = new HashMap<>();
    }
    public Chat(String chatName, List<User> users, Map<Integer, BaseMessage> messages)
    {
        chatId = ++count;
        this.chatName = chatName;
        this.users    = users;
        this.messages = messages;
    }
    public int getChatId() {return chatId;}
    public void addMessage(BaseMessage message)
    {
        messages.put(message.getMessageId(), message);
    }
    public void deleteMessage(int id) {
        messages.remove(id);
    }

    public void addUser(User user)
    {
        users.add(user);
    }

   public ArrayList<FileMessage> getAllFileMessages()
   {
       ArrayList<FileMessage> temp = new ArrayList<>();
       for(BaseMessage message : messages.values())
       {
           if(message instanceof FileMessage)
               temp.add((FileMessage) message);
       }
       return temp;
   }
   public ArrayList<ImageMessage> getAllImageMessages()
   {
       ArrayList<ImageMessage> temp = new ArrayList<>();
       for(BaseMessage message : messages.values())
       {
           if(message instanceof ImageMessage)
               temp.add((ImageMessage) message);
       }
       return temp;
   }
   public void saveToFile(String fileName)
   {
       //writing the state of the object Chat(pointer this) to stream to file(fieName) (Chat is Serializable)
       try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName)))
       {
           os.writeObject(this);
           System.out.println("File has been written");
       }
       catch (Exception e)
       {
           System.out.println(e.getMessage());
       }
   }
   public static Chat loadFromFile(String fileName)
   {
       //loading the state of the object Chat from the stream to file(fileName) (Chat is Serializable)
       try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName)))
       {
           System.out.println("File has been loaded");
           //if(this.getClass() == ois.getClass())
            return (Chat)ois.readObject();
       }
       catch (Exception e) {
           System.out.println(e.getMessage());
       }
        return null;
   }

   public void showMessages()
   {
       if(messages != null)
       {
           System.out.println("Messages: \n");
           for (BaseMessage m : messages.values())
               System.out.println(messages.hashCode() + ". " + m.render());
       }
       else
           System.out.println("No messages are in chat");
   }
   public void showUsers()
   {
       if(users != null)
       {
           System.out.println("Users: \n");
           for (User m : users)
               System.out.println("Id: " + m.getId() + " Name: " + m.getName());
       }
       else
           System.out.println("No users are in chat");
   }
   public List<BaseMessage> getMessages()
   {
       return new ArrayList<>(messages.values());
   }
   public String getChatName(){return chatName;}
}
