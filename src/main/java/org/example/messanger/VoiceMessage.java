package org.example.messanger;

import java.util.Date;

//1.2.1 Inheritance
public class VoiceMessage extends BaseMessage{
    private String audioUrl;

    public VoiceMessage(User user, Date date, String audioUrl) {
        //1.2.6 and 1.2.7 Use call of superclass method using super(Call of superclass constructor.)
        super(user, date);
        this.audioUrl = audioUrl;
    }
    // Copy constructor
    public VoiceMessage(User user, Date date, int messageId, String audioUrl) {
        super(user, date, messageId);
        this.audioUrl = audioUrl;
    }
    @Override
    public VoiceMessage clone() {
        return new VoiceMessage(user, new Date(date.getTime()), messageId, audioUrl);
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
        return user.getName() + " at (" + date.toString() + ")," + " sent a voice message: " + audioUrl;
    }
}
