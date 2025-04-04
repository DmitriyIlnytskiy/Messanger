package org.example.messanger;

import java.util.Date;

//1.2.1 Inheritance
public class TextMessage extends BaseMessage{
    //1.2.5 Use of private, protected, public
    private String content;

    public TextMessage(User user, Date date, String content) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(user, date);
        this.content = content;
    }
    public void setContent(String content) {this.content=content;}
    public String getContent(){return content;}

    @Override
    public String showData() {
        return getContent();
    }

    //1.2.6 Use call of superclass method using super.
    public int getMessageCount()
    {
        return super.getMessageCount();
    }

    //1.2.4 Method overriding
    @Override
    public String render() {
        return user.getName() + " at (" + date + "): " + content;
    }
    // 1.2.3 Method Overloading
    @Override
    public String render(String additionalInfo) {
        return "additionalInfo: " + additionalInfo + "\n" + user.getName() + " (" + date.toString() + "): \n" + content;
    }

}
