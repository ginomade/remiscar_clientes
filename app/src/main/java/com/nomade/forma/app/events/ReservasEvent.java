package com.nomade.forma.app.events;

/**
 * Created by Gino on 2/5/2018.
 */

public class ReservasEvent extends BaseEvent {
    private String dataString;

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }
}
