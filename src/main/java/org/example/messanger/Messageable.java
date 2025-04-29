package org.example.messanger;

import java.io.Serializable;
import java.util.Date;

public interface Messageable extends Serializable {

    User getUser();

    int getMessageId();

    String render();

    String showData();

    Date getDate();
}
