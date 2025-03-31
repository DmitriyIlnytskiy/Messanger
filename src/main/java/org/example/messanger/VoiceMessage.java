package org.example.messanger;

//1.2.1 Inheritance
public class VoiceMessage extends BaseMessage{
    private String audioUrl;

    public VoiceMessage(String author, String date, String audioUrl) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(author, date);
        this.audioUrl = audioUrl;
    }
    public void setAudioUrl(String audioUrl) {this.audioUrl=audioUrl;}
    public String getAudioUrl(){return audioUrl;}

    @Override
    public String showData() {
        return getAudioUrl();
    }

    //1.2.4 Method overriding
    @Override
    public String render() {
        return author + " at (" + date + ")," + " sent a voice message: " + audioUrl;
    }
}
