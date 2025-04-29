package org.example.messanger;

import java.io.Serializable;

public interface Responsable extends Serializable {
    boolean isSuccess();
    String getInformation();
}
