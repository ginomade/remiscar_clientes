package com.nomade.forma.app;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


public class WebActivity extends ActionBarActivity {

    public WebView webView;
    public String imei;
    public String telefono;
    public String prefijo;
    public Button Ret;
    public String webcontent;//contenido del webview en html
    public int iBip;
    public String MsjNum = "";
    public String MsjNumNuevo = "";
    public int flgCiclo; // flag para chequear movil asignado en el segundo llamado a origen
    public int flgOrigen;//flag para actualizacion de origen.php
    String telCompleto;
    Handler myHandler;
    int flg_mens=0; // flag para mensajes
    //Button buttonMisViajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        // identificador del equipo segun tipo
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            imei = getMacAdd();
        }else{
            //obtengo imei
            imei = getPhoneImei();//"359781041848146"
        }


        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WebActivity.this);
        webcontent = prefs.getString("webresult","Error.");
        telefono = prefs.getString("celular","");
        prefijo = prefs.getString("car","");
        telCompleto = prefijo + telefono;

        webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(View.VISIBLE);
        String webData = "<?xml version='1.0' encoding='utf-8'?><html><body>"+webcontent+" </body></html>";
        webView.loadData(webData, "text/html; charset=UTF-8", null);


        //Actualizacion de contenido de la webview.
        final Handler myHandler = new Handler();
        flgCiclo = 0;
        flgOrigen = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!WebActivity.this.isFinishing() && flgOrigen==0) {
                    try {
                        Thread.sleep(5000);
                        myHandler.post(mMyRunnable);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();

        Ret = (Button)findViewById(R.id.buttonRet);
        Ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //vuelvo a pantalla inicial
                try {
                    Intent intent = new Intent(WebActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(WebActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        });

// recarga de origen.php al cancelar o reclamar
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean shouldOverride = false;
                if (url.contains("cancelar.php") || url.contains("reclamar.php")) {
                    myHandler.postDelayed(mMyRunnable, 3000);
                }
                else if(url.contains("registro.php")){
                    flgOrigen = 1;
                }


                return shouldOverride;
            }
        });


    }

    public String isTablet(){
        TelephonyManager manager = (TelephonyManager)WebActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE){
            return "Tablet";
        }else{
            return "Mobile";
        }
    }

    public String getMacAdd(){
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    //Obtener numero de imei
    private String getPhoneImei(){
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager.getDeviceId();
    }


    //Here's a runnable/handler combo
    private Runnable mMyRunnable = new Runnable()
    {
        @Override
        public void run()
        {

            boolean bBloq = webcontent.contains("bloqueado");
            if(bBloq){
                Intent intent = new Intent(WebActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }else{

                //nuevo mensaje
                Ion.with(WebActivity.this)
                        .load("http://carlitosbahia.dynns.com/movil/Mmensajes.php")
                        .setBodyParameter("IMEI", imei)
                        .setBodyParameter("Ubicacion", "")
                        .setBodyParameter("geopos", "")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.e("Remiscar ", "MMENS - " + result);
                                try {
                                    int success = result.get("result").getAsInt();
                                    if (success == 0) {
                                        Log.e("Remiscar ", "sin mensajes.");
                                        //    flg_mens=0;
                                        Ret.setText("Inicio");
                                        Ret.setBackgroundColor(Color.parseColor("#4863a0"));
                                        Ret.setTextColor(Color.parseColor("#d5d9ea"));
                                    } else if (success == 1) {
                                        Log.e("Remiscar ", "hay mensajes.");
                                        //   if(flg_mens==0){
                                        Toast.makeText(getApplicationContext(), "Hay nuevos mensajes para usted.", Toast.LENGTH_SHORT).show();
                                        final MediaPlayer mp = MediaPlayer.create(WebActivity.this, R.raw.c2answer);
                                        mp.start();
                                        Ret.setText("Nuevo mensaje");
                                        Ret.setBackgroundColor(Color.parseColor("#ff0000"));
                                        Ret.setTextColor(Color.WHITE);
                                        //   }else{

                                        //   }
                                        //Thread.sleep(3000);
                                        //myHandler.post(mMyRunnable);
                                        //cargarOrigen();

                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });
                //new asyncOper().execute(imei,"origen.php",telCompleto);
                cargarOrigen();

            }
            if (flgCiclo ==0){flgCiclo = 1;}
            else{flgCiclo = 2;}

        }
    };


    // tarea asincrona de carga de origen.php
    public void cargarOrigen(){
        try {
            Ion.with(WebActivity.this)
                    .load("http://carlitosbahia.dynns.com/movil/origen.php")
                    .setBodyParameter("IMEI", imei)
                    .setBodyParameter("Celular", telCompleto)
                    .setBodyParameter("geopos", "")
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            Log.e("Remiscar ", "ORIGEN - " + result);
                            try {
                                webView.loadData(result ,"text/html; charset=UTF-8", null);
                                webcontent = result;
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                        }
                    });
        }catch(Exception e){
            Log.e("Remiscar ", "Web Origen ERR - "+e);
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(WebActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            myHandler.removeCallbacks(mMyRunnable);
        }catch(NullPointerException e){
            Log.e("Remiscar ", "ERR - "+e);
        }
    }
}


