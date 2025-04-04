package org.example.messanger;

import java.util.Date;

//1.2.1 Inheritance
public class FileMessage extends BaseMessage{
    private String fileName;

    public FileMessage(User user, Date date, String fileName) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(user, date);
        this.fileName = fileName;
    }
    public void setFileName(String fileName) {this.fileName=fileName;}
    public String getFileName(){return fileName;}
    @Override
    public String showData() {
        return getFileName();
    }
    //1.2.4 Method overriding
    @Override
    public String render() {
        return user.getName() + " at (" + date.toString() + ")," +  " shared a file: " + fileName;
    }
}
