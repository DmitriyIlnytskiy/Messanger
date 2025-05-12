package org.example.messanger;

import java.util.Date;

//1.2.1 Inheritance
public class LocationMessage extends BaseMessage{
    private String location;

    public LocationMessage(User user, Date date, String location) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(user, date);
        this.location = location;
    }
    // Copy constructor
    public LocationMessage(User user, Date date, int messageId, String location) {
        super(user, date, messageId);
        this.location = location;
    }
    @Override
    public LocationMessage clone() {
        return new LocationMessage(user, new Date(date.getTime()), messageId, location);
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
        return user.getName() + " at (" + date.toString() + ")," + " shared a location: " + location;
    }
}
