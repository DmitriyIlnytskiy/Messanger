package org.example.messanger;

import java.util.Date;

public class FileMessage extends BaseMessage{
    private String fileName;

    public FileMessage(User user, Date date, String fileName) {
        super(user, date);
        this.fileName = fileName;
    }
    // Copy constructor
    public FileMessage(FileMessage other) {
        super(new User(other.user), new Date(other.date.getTime()));  // Deep copy the user
        this.fileName = other.fileName;
    }
    @Override
    public FileMessage clone() {
        return new FileMessage(this);
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
