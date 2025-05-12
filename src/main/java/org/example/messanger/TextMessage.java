package org.example.messanger;

import java.util.Date;

public class TextMessage extends BaseMessage{
    private String content;

    public TextMessage(User user, Date date, String content) {
        super(user, date);
        this.content = content;
    }
    // Copy constructor
    public TextMessage(TextMessage other) {
        super(new User(other.user), new Date(other.date.getTime()));  // Deep copy the user
        this.content = other.content;
    }
    @Override
    public TextMessage clone() {
        return new TextMessage(this);
    }

    public void setContent(String content) {this.content=content;}
    public String getContent(){return content;}

    @Override
    public String showData() {
        return getContent();
    }

    public int getMessageCount()
    {
        return super.getMessageCount();
    }

    @Override
    public String render() {
        return user.getName() + " at (" + date + "): " + content;
    }

}
