package org.example.messanger;

import java.time.LocalDateTime;
import java.util.Date;

abstract class BaseMessage implements Messageable, Cloneable {
    protected Date date;
    protected User user;

    protected int messageId;
    private static int messageCount = 0;

    private LocalDateTime timestamp;

    // I have problem with serialization(server sends the same object - client do not reserialize it and use previous object - so I need to clone for creating a new object)
    public BaseMessage(BaseMessage other) {
        this.user = other.getUser();
        this.date = other.getDate();
        this.messageId = other.getMessageId();
        this.timestamp = LocalDateTime.now();
    }
    public BaseMessage(User user, Date date) {
        this.user = user;
        this.date = date;
        messageId = ++messageCount;
        this.timestamp = LocalDateTime.now();
    }
    public BaseMessage(User user, Date date, int messageId) {
        this.user = user;
        this.date = date;
        this.messageId = messageId;
        this.timestamp = LocalDateTime.now();
    }
    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    // Deep cloning method
    @Override
    public BaseMessage clone() {
        try {
            // Clone the object using super.clone() which performs a shallow copy
            BaseMessage cloned = (BaseMessage) super.clone();

            // Deep copy the mutable fields (User and Date)
            cloned.user = new User(this.user);  // User has a copy constructor
            cloned.date = new Date(this.date.getTime());

            // Return the deep cloned object
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported for BaseMessage", e);
        }
    }

    public int getMessageCount() {return messageCount;}

    @Override
    public int getMessageId() {
        return messageId;}
    @Override
    public User getUser(){return user;}
    @Override
    public Date getDate(){return date;}
    @Override
    public abstract String showData();
    @Override
    public abstract String render();



}
