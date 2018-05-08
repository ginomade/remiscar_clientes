package com.nomade.forma.app.events;

/**
 * Created by Gino on 2/5/2018.
 */

public class ReclamosEvent extends MensajesEvent {
    private String dataString;

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }
}
