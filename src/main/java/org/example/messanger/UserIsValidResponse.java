package org.example.messanger;

public class UserIsValidResponse extends ServerResponse{
    private User user;
    public UserIsValidResponse(boolean success, String information, User user) {
        super(success, information);
        this.user = user;
    }
    public User getUser() {
        return user;
    }
}
