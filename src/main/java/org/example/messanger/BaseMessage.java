package org.example.messanger;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;

//1.2.1 Abstract class
abstract class BaseMessage implements Serializable {
    //1.2.5 Use of private, protected, public
    protected String date;
    protected String author;

    private int messageId;
    private static int messageCount = 0; // Constant field for counting messages

    public BaseMessage(String author, String date) {
        this.author = author;
        this.date = date;
        messageId = ++messageCount;

    }

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
