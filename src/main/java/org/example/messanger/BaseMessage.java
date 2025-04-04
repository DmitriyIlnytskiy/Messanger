package org.example.messanger;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.util.Date;

//1.2.1 Abstract class
abstract class BaseMessage implements Serializable {
    //1.2.5 Use of private, protected, public
    protected Date date;
    protected User user;

    private int messageId;
    private static int messageCount = 0; // Constant field for counting messages

    public BaseMessage(User user, Date date) {
        this.user = user;
        this.date = date;
        messageId = ++messageCount;

    }

    public User getUser(){return user;}
    public int getMessageCount()
    {
        return messageCount;
    }
    public int getMessageId()
    {
        return messageId;
    }

    public abstract String showData();

    public abstract String render();

    // 1.2.3 Method Overloading
    public String render(String additionalInfo) {
        return additionalInfo + render();
   }

}
