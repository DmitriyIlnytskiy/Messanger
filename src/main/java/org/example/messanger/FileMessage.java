package org.example.messanger;

import java.util.Date;

public class FileMessage extends BaseMessage{
    private String fileName;

    public FileMessage(User user, Date date, String fileName) {
        super(user, date);
        this.fileName = fileName;
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
