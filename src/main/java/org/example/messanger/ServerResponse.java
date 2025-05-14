package org.example.messanger;

public abstract class ServerResponse implements Responsable{
    protected final boolean success;
    private final String information;
    private Messageable message;

    public ServerResponse(boolean success, String information, Messageable message)
    {
        this.success = success;
        this.information = information;
        this.message = message;
    }
    @Override
    public boolean isSuccess() {
        return success;
    }
    @Override
    public String getInformation() {
        return information;
    }

    public Messageable getMessage(){return message;}

}
