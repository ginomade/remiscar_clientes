package com.nomade.forma.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nomade.forma.app.MainActivity;
import com.nomade.forma.app.R;
import com.nomade.forma.app.ReclamosActivity;
import com.nomade.forma.app.WebActivity;
import com.nomade.forma.app.events.BloqueadoEvent;
import com.nomade.forma.app.events.MensajesEvent;
import com.nomade.forma.app.events.ReclamosEvent;
import com.nomade.forma.app.events.ReservasEvent;
import com.nomade.forma.app.events.UbicacionEvent;
import com.nomade.forma.app.events.ViajesEvent;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.Charset;

/**
 * @author gino.ghiotto
 */
public class ServiceUtils {

    public static String base_url = "http://carlitosbahia.dynns.com/movil/";
    private static String url_mensajes = base_url + "Mmensajes.php";
    private static String url_origenreservas = base_url + "origenreservas.php";
    public static String url_viajes = base_url + "origen.php";
    private static String url_reclamosMovil = base_url + "reclamosMovil.php";
    private static String url_celu_bloqueado = base_url + "Mcelubloqueado.php";

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

    public static void validarImei(Context context) {
        Ion.with(context)
                .load(url_celu_bloqueado)
                .setBodyParameter("IMEI", SharedPrefsUtil.getInstance(context).getString("imei", ""))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null) {
                            BloqueadoEvent event = new BloqueadoEvent();
                            event.setObject(result);
                            EventBus.getDefault().post(event);
                        } else {
                            Log.d("Remiscar* ", "error en validar bloqueados.");
                        }

                    }
                });
    }

    public static void getMensajes(Context context){
        try {

            Ion.with(context)
                    .load(url_mensajes)
                    .setBodyParameter("IMEI", SharedPrefsUtil.getInstance(context).getString("imei", ""))
                    .setBodyParameter("Ubicacion", "")
                    .setBodyParameter("geopos", "")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            Log.e("Remiscar ", "mensajes - " + result);
                            if (result != null) {
                                MensajesEvent event = new MensajesEvent();
                                event.setObject(result);
                                EventBus.getDefault().post(event);
                            } else {
                                Log.d("Remiscar* ", "error en respuesta de mensajes.");
                            }

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //envio del pedido a origenreservas.php
    public static void sendReservas(Context context, String imei, String celu, String geo,
                                    String obs, String nombreUsuario) {
        Log.w("remiscar", "sendReservas - " + imei + "-" + celu + "-" + geo + "-" + obs + "-" + nombreUsuario);
        Ion.with(context)
                .load(url_origenreservas)
                .setBodyParameter("submit", "submit")
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .setBodyParameter("Geoposicion", geo)
                .setBodyParameter("Observaciones", obs)
                .setBodyParameter("Pasajero", nombreUsuario)
                .asString(Charset.forName("iso-8859-1"))
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.e("Remiscar ", "Login  - " + result);

                        if (result != null) {
                            ReservasEvent event = new ReservasEvent();
                            event.setDataString(result);
                            EventBus.getDefault().post(event);
                        } else {
                            Log.d("Remiscar* ", "error en respuesta de mensajes.");
                        }
                    }
                });
    }

    public static void getViajes(Context context, String imei, String celu) {
        Log.w("remiscar", "sendReservas - " + imei + "-" + celu);
        Ion.with(context)
                .load(url_viajes)
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.e("Remiscar ", "viajes  - " + result);
                        try {
                            if (result.equals("ok")) {

                            } else {

                                /*sharedPrefs.saveString("webresult", result);
                                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                                startActivity(intent);*/
                            }

                            if (result != null) {
                                ViajesEvent event = new ViajesEvent();
                                event.setDataString(result);
                                EventBus.getDefault().post(event);
                            } else {
                                Log.d("Remiscar* ", "error en respuesta de viajes.");
                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    public static void getReclamos(Context context, String imei, String celu, String mensaje, String nombreUsuario) {
        Log.w("remiscar", "sendReservas - " + imei + "-" + celu + "-" + mensaje +nombreUsuario);
        Ion.with(context)
                .load(url_reclamosMovil + "?IMEI=" + imei + "&Celular=" + celu
                        + "&Descripcion=" + mensaje + "&Pasajero=" + nombreUsuario)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.e("Remiscar:", "Reclamos  - " + result);
                        try {

                            ReclamosEvent event = new ReclamosEvent();
                            event.setDataString(result);
                            EventBus.getDefault().post(event);


                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }
}
