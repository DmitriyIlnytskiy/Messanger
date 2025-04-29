package org.example.messanger;

import java.io.Serializable;

public interface Requestable extends Serializable {
    User getRequester();
    Messageable getMessage();
}
