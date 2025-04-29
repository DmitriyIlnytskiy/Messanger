package org.example.messanger;

import java.util.Date;

abstract class BaseMessage implements Messageable {
    protected Date date;
    protected User user;

    private int messageId;
    private static int messageCount = 0;

    public BaseMessage(User user, Date date) {
        this.user = user;
        this.date = date;
        messageId = ++messageCount;
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
