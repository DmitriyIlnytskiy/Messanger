package org.example.messanger;

import java.util.Date;

//1.2.1 Inheritance
public class ContactMessage extends BaseMessage{
    private String contact;

    public ContactMessage(User author, Date date, String contact) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(author, date);
        this.contact = contact;

    }
    // Copy constructor
    public ContactMessage(ContactMessage other) {
        super(new User(other.user), new Date(other.date.getTime()));  // Deep copy the user
        this.contact = other.contact;
    }
    @Override
    public ContactMessage clone() {
        return new ContactMessage(this);
    }

    public void setContact(String contact) {this.contact=contact;}
    public String getContact(){return contact;}

    @Override
    public String showData() {
        return getContact();
    }
    //1.2.4 Method overriding
    @Override
    public String render() {
        return user.getName() + " at (" + date.toString() + ")," + " shared a contact:" + contact;
    }

}
