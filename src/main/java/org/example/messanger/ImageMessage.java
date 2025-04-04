package org.example.messanger;

import java.util.Date;

//1.2.1 Inheritance
public class ImageMessage extends BaseMessage{
    private String imageUrl;

    public ImageMessage(User user, Date date, String imageUrl) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(user, date);
        this.imageUrl = imageUrl;
    }
    public void setImageUrl(String imageUrl) {this.imageUrl=imageUrl;}
    public String getImageUrl(){return imageUrl;}

    @Override
    public String showData() {
        return getImageUrl();
    }

    //1.2.4 Method overriding
    @Override
    public String render() {
        return user.getName() + " at (" + date.toString() + ")," + " shared an image: " + imageUrl;
    }
}
