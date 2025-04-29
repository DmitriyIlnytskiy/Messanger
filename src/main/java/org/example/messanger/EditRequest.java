package org.example.messanger;

public class EditRequest extends ClientRequest {
    private final String content;


    public EditRequest(User requester, Messageable message, String content)
    {
        super(requester,message);
        this.content = content;
    }

    public String getContent(){return content;}

}
