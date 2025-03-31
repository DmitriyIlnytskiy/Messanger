package org.example.messanger;

//1.2.1 Inheritance
public class ContactMessage extends BaseMessage{
    private String contact;

    public ContactMessage(String author, String date, String contact) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(author, date);
        this.contact = contact;

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
        return author + " at (" + date + ")," + " shared a contact:" + contact;
    }

}
