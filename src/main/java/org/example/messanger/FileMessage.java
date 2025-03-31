package org.example.messanger;

//1.2.1 Inheritance
public class FileMessage extends BaseMessage{
    private String fileName;

    public FileMessage(String author, String date, String fileName) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(author, date);
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
        return author + " at (" + date + ")," +  " shared a file: " + fileName;
    }
}
