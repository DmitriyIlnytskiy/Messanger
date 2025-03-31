package org.example.messanger;

//1.2.1 Inheritance
public class LocationMessage extends BaseMessage{
    private String location;

    public LocationMessage(String author, String date, String location) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(author, date);
        this.location = location;
    }
    public void setLocation(String location) {this.location=location;}
    public String getLocation(){return location;}

    @Override
    public String showData() {
        return getLocation();
    }

    //1.2.4 Method overriding
    @Override
    public String render() {
        return author + " at (" + date + ")," + " shared a location: " + location;
    }
}
