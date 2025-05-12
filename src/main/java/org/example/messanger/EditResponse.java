package org.example.messanger;

public class EditResponse extends ServerResponse {
    public EditResponse(boolean success, String information, Messageable editedMessage) {
        super(success, information, editedMessage);
    }
}
