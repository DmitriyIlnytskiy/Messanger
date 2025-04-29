package org.example.messanger;

public abstract class ClientRequest implements Requestable{
    private final Messageable message;
    private final User requester;
    public ClientRequest(User requester, Messageable message)
    {
        this.message = message;
        this.requester = requester;
    }
    @Override
    public Messageable getMessage(){return message;}

    @Override
    public User getRequester(){return requester;}
}
