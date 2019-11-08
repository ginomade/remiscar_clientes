package com.nomade.forma.app.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nomade.forma.app.events.BloqueadoEvent;
import com.nomade.forma.app.events.MainViewEvent;
import com.nomade.forma.app.events.MensajesEvent;
import com.nomade.forma.app.events.MpPreferenceEvent;
import com.nomade.forma.app.events.ReclamosEvent;
import com.nomade.forma.app.events.ReservasEvent;
import com.nomade.forma.app.events.UbicacionEvent;
import com.nomade.forma.app.events.ViajesEvent;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    public static String url_privacy = "http://www.remiscar.com.ar/condiciones/";
    public static String url_payment_pref = base_url + "checkout.php";
    public static String url_payment_confirmation = base_url + "CobroMercadoPago.php";

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
                            Log.w("Remiscar* ", "error en asLocation.");
                        }

                    }
                });
    }

    public static String getVersionName(Context context){
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            Log.d("Remiscar", "Version Name : "+version + "\n Version Code : "+versionCode);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d("Remiscar", "PackageManager Catch : "+e.toString());
        }
        return version;
    }

    public static void validarImei(Context context) {
        Ion.with(context)
                .load(url_celu_bloqueado)
                .setBodyParameter("IMEI", SharedPrefsUtil.getInstance(context).getString("imei", ""))
                .setBodyParameter("version", getVersionName(context))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null) {
                            BloqueadoEvent event = new BloqueadoEvent();
                            event.setObject(result);
                            EventBus.getDefault().post(event);
                        } else {
                            Log.w("Remiscar* ", "error en validar bloqueados.");
                        }

                    }
                });
    }

    public static void getMensajes(Context context) {
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
                            //Log.e("Remiscar ", "mensajes - " + result);
                            if (result != null) {
                                MensajesEvent event = new MensajesEvent();
                                event.setObject(result);
                                EventBus.getDefault().post(event);
                            } else {
                                Log.w("Remiscar* ", "error en respuesta de mensajes.");
                            }

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //envio del pedido a origenreservas.php
    public static void sendReservas(Context context, String imei, String celu, String geo,
                                    String obs, String nombreUsuario,
                                    String dni, String email,
                                    boolean pagoConTarjeta) {
        Log.w("remiscar", "sendReservas - " + imei + "-" + celu + "-"
                + geo + "-" + obs + "-" + nombreUsuario
        + "- tarjeta - " + pagoConTarjeta);
        //"-54.4802,-68.1721"
        Ion.with(context)
                .load(url_origenreservas)
                .setBodyParameter("submit", "submit")
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .setBodyParameter("Geoposicion", geo)
                .setBodyParameter("Observaciones", obs)
                .setBodyParameter("Pasajero", nombreUsuario)
                .setBodyParameter("DNI", dni)
                .setBodyParameter("EMAIL", email)
                .setBodyParameter("Tarjeta", String.valueOf(pagoConTarjeta))
                .asString(Charset.forName("iso-8859-1"))
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        //Log.e("Remiscar ", "Login  - " + result);

                        if (result != null) {
                            ReservasEvent event = new ReservasEvent();
                            event.setDataString(result);
                            EventBus.getDefault().post(event);
                        } else {
                            Log.w("Remiscar* ", "error en respuesta de mensajes.");
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
                                Log.w("Remiscar* ", "error en respuesta de viajes.");
                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    public static void getMPPreferenceId(Context context, String imei, String celu, String reserva) {
        Log.w("remiscar", "getMPPreferenceId - " + imei + "-" + celu);
        Ion.with(context)
                .load(url_payment_pref)
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .setBodyParameter("RESERVAS", reserva)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject data) {
                        try {
                            if (data != null) {
                                String result = data.get("result").getAsString();
                                String id = data.get("preference_id").getAsString();
                                MpPreferenceEvent event = new MpPreferenceEvent();
                                event.setObject(data);
                                EventBus.getDefault().post(event);
                            } else {
                                Log.w("Remiscar* ", "error en getMPPreferenceId.");
                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    public static void getReclamos(Context context, String imei, String celu, String mensaje, String nombreUsuario) {
        String finalUrl = url_reclamosMovil + "?IMEI=" + imei + "&Celular=" + celu
                + "&Descripcion=" + mensaje + "&Pasajero=" + nombreUsuario;
        Log.d("remiscar", "getReclamos - " + finalUrl);
        Ion.with(context)
                .load(url_reclamosMovil)
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .setBodyParameter("Descripcion", mensaje)
                .setBodyParameter("Pasajero", nombreUsuario)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.d("Remiscar:", "Reclamos  - " + result);
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

    //contenido del webview en mainactivity.
    public static void getMainData(String imei, String telCompleto) {
        String finalUrl = ServiceUtils.url_viajes
                + "?IMEI=" + imei
                + "&Celular=" + telCompleto;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String data = "";
                    String reserva = "";
                    String empresa = "";
                    Document doc = Jsoup.connect(finalUrl).get();
                    Elements elements = doc.getAllElements();
                    for (Element element : elements) {
                        if (!element.select("tbody").isEmpty()) {
                            data = element.outerHtml() + "<br/>";
                            break;
                        }
                    }
                    Elements inputs = doc.select("input[name=Empresa]");
                    if (!inputs.isEmpty()) {
                        empresa = inputs.get(0).val();
                    }
                    Elements inputsReserva = doc.select("input[name=Reserva]");
                    if (!inputsReserva.isEmpty()) {
                        reserva = inputsReserva.get(0).val();
                    }
                    MainViewEvent event = new MainViewEvent();
                    event.setContent(data);
                    event.setReserva(reserva);
                    event.setEmpresa(empresa);
                    EventBus.getDefault().post(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static void getMainData(String url) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String data = "";
                    Document doc = Jsoup.connect(url).get();
                    Elements elements = doc.getAllElements();
                    for (Element element : elements) {
                        if (!element.select("tbody").isEmpty()) {
                            data = element.outerHtml() + "<br/>";
                            break;
                        }
                    }

                    MainViewEvent event = new MainViewEvent();
                    event.setContent(data);
                    EventBus.getDefault().post(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static void sendPaymentConfirmation(Context context, String imei, String reserva,
                                               String status,
                                               String statusDetail,
                                               String paymentId,
                                               String importeCobrado) {
        Log.d("remiscar", "sendPaymentConfirmation - " + imei + "-" + paymentId);
        Ion.with(context)
                .load(url_payment_confirmation + "?ImporteCobrado=" + importeCobrado)
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("idcobro", paymentId)
                .setBodyParameter("RESERVA", reserva)
                .setBodyParameter("Status", status)
                .setBodyParameter("StatusDetail", statusDetail)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject data) {
                        try {
                            if (data != null) {
                            } else {
                                Log.w("Remiscar* ", "error en sendPaymentConfirmation.");
                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }
}
