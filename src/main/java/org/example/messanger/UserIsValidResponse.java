package org.example.messanger;

public class UserIsValidResponse implements Responsable{
    private final User user;
    private final boolean success;
    private final String information;
    public UserIsValidResponse(boolean success, String information, User user) {
        this.success = success;
        this.information = information;
        this.user = user;
    }
    public User getUser() {
        return this.user;
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public String getInformation() {
        return this.information;
    }

}
