package org.example.messanger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Chat implements Serializable {
    private String chatName;
    private List<User> users;
    private List<BaseMessage> messages;

    public Chat(String chatName)
    {
        this.chatName = chatName;
        users    = new ArrayList<>();
        messages = new ArrayList<>();
    }
    public Chat(String chatName, List<User> users, List<BaseMessage> messages)
    {
        this.chatName = chatName;
        this.users    = users;
        this.messages = messages;
    }
    public void addMessage(BaseMessage message)
    {
        messages.add(message);
    }
   public ArrayList<FileMessage> getAllFileMessages()
   {
       ArrayList<FileMessage> temp = new ArrayList<>();
       for(BaseMessage message : messages)
       {
           if(message instanceof FileMessage)
               temp.add((FileMessage) message);
       }
       return temp;
   }
   public ArrayList<ImageMessage> getAllImageMessages()
   {
       ArrayList<ImageMessage> temp = new ArrayList<>();
       for(BaseMessage message : messages)
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
       catch (Exception ex)
       {
           System.out.println(ex.getMessage());
       }
   }
   public static Chat loadFromFile(String fileName)
   {
       //loading the state of the object Chat from the stream to file(fileName) (Chat is Serializable)
       try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName)))
       {
           System.out.println("File has been loaded");
            return (Chat)ois.readObject();
       }
       catch (Exception ex) {
           System.out.println(ex.getMessage());
       }
        return null;
   }

   public void showMessages()
   {
       if(messages != null)
       {
           System.out.println("Messages: \n");
           for (BaseMessage m : messages)
               System.out.println(m.render());
       }
       else
           System.out.println("No messages are in chat");
   }
   public List<BaseMessage> getMessages()
   {
       List<BaseMessage> temp = new ArrayList<>();
       if(messages != null) {
           for (BaseMessage m : messages)
               temp.add(m);
       }
       return temp;
   }
}
