package org.example.messanger;

import java.util.Date;

public class FileMessage extends BaseMessage{
    private String fileName;

    public FileMessage(User user, Date date, String fileName) {
        super(user, date);
        this.fileName = fileName;
    }
    // Copy constructor
    public FileMessage(User user, Date date, int messageId, String fileName) {
        super(user, date, messageId);
        this.fileName = fileName;
    }
    @Override
    public FileMessage clone() {
        return new FileMessage(user, new Date(date.getTime()), messageId, fileName);
    }

    public void setFileName(String fileName) {this.fileName=fileName;}
    public String getFileName(){return fileName;}
    @Override
    public String showData() {
        return getFileName();
    }
    @Override
    public String render() {
        return user.getName() + " at (" + date.toString() + ")," +  " shared a file: " + fileName;
    }
}
