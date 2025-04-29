package org.example.messanger;

public abstract class ServerResponse implements Responsable{
    private final boolean success;
    private final String information;

    public ServerResponse(boolean success, String information)
    {
        this.success = success;
        this.information = information;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
    @Override
    public String getInformation() {
        return information;
    }
}
