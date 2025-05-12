package org.example.messanger;

public class DeleteResponse extends ServerResponse{

    public DeleteResponse(boolean success, String information, Messageable deletedMessage) {
        super(success, information, deletedMessage);
    }
}
