package com.nomade.forma.app.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nomade.forma.app.events.UbicacionEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * @author gino.ghiotto
 */
public class ServiceUtils {

    public static String base_url = "http://carlitosbahia.dynns.com/movil/";
    private static String url_mensajes = base_url + "Mmensajes.php";

    public static void asUbicacion(Context context, String url_ubicacion) {
        Ion.with(context)
                .load(url_ubicacion)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null) {
                            UbicacionEvent event = new UbicacionEvent();
                            event.setObject(result);
                            EventBus.getDefault().post(event);
                        } else {
                            Log.d("Remiscar* ", "error en asLocation.");
                        }

                    }
                });
    }
}
