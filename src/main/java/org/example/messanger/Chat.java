package org.example.messanger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Chat implements Serializable {
    private String chatName;
    private List<User> users;
    private List<Messageable> messages;
    private static int count = 0;
    private int chatId;

    public Chat(String chatName)
    {
        chatId = ++count;
        this.chatName = chatName;
        users    = new ArrayList<>();
        messages = new ArrayList<>();
    }
    public Chat(String chatName, List<User> users, List<Messageable> messages)
    {
        chatId = ++count;
        this.chatName = chatName;
        this.users    = users;
        this.messages = messages;
    }
    public int getChatId() {return chatId;}
    public void addMessage(Messageable message)
    {
        messages.add(message);
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
       for(Messageable message : messages)
       {
           if(message instanceof FileMessage)
               temp.add((FileMessage) message);
       }
       return temp;
   }
   public ArrayList<ImageMessage> getAllImageMessages()
   {
       ArrayList<ImageMessage> temp = new ArrayList<>();
       for(Messageable message : messages)
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
           for (Messageable m : messages)
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
   public List<Messageable> getMessages()
   {
       return new ArrayList<>(messages);
   }
   public List<User> getUsers()
   {
       return new ArrayList<>(users);
   }

   public String getChatName(){return chatName;}

    public Messageable findMessageById(int messageId) {
        sortMessagesById();
        int low = 0;
        int high = messages.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int midId = messages.get(mid).getMessageId();

            if (midId == messageId) {
                return messages.get(mid);
            } else if (midId < messageId) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null; // Message not found
    }

    public void sortMessagesById() {
        List<Messageable> temp = new ArrayList<>(messages);
        mergeSort(temp, 0, temp.size() - 1);
        this.messages = temp; // Update the main list with the sorted one
    }

    private void mergeSort(List<Messageable> arr, int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;

            mergeSort(arr, l, m);
            mergeSort(arr, m + 1, r);

            merge(arr, l, m, r);
        }
    }

    private void merge(List<Messageable> arr, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        List<Messageable> L = new ArrayList<>(n1);
        List<Messageable> R = new ArrayList<>(n2);

        for (int i = 0; i < n1; ++i)
            L.add(arr.get(l + i));
        for (int j = 0; j < n2; ++j)
            R.add(arr.get(m + 1 + j));

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if (L.get(i).getMessageId() <= R.get(j).getMessageId()) {
                arr.set(k++, L.get(i++));
            } else {
                arr.set(k++, R.get(j++));
            }
        }

        while (i < n1)
            arr.set(k++, L.get(i++));
        while (j < n2)
            arr.set(k++, R.get(j++));
    }
}

