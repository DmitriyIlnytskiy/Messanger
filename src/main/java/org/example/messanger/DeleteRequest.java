package org.example.messanger;

public class DeleteRequest extends ClientRequest {

    public DeleteRequest(User requester, Messageable message)
    {
        super(requester,message);
    }

}
