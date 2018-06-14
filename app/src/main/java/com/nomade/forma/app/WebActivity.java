package com.nomade.forma.app;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.nomade.forma.app.events.MensajesEvent;
import com.nomade.forma.app.events.ViajesEvent;
import com.nomade.forma.app.utils.ServiceUtils;
import com.nomade.forma.app.utils.SharedPrefsUtil;

import org.greenrobot.eventbus.Subscribe;


public class WebActivity extends AppCompatActivity {

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
    int flg_mens = 0; // flag para mensajes
    Context mContext;
    SharedPrefsUtil sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        // identificador del equipo segun tipo
        Configuration config = getResources().getConfiguration();

        mContext = WebActivity.this;
        sharedPrefs = SharedPrefsUtil.getInstance(mContext);

        imei = sharedPrefs.getString("imei", "");
        webcontent = sharedPrefs.getString("webresult", "Error.");
        telCompleto = sharedPrefs.getString("telefono", "");

        webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(View.VISIBLE);
        String webData = "<?xml version='1.0' encoding='utf-8'?><html><body>" + webcontent + " </body></html>";
        webView.loadData(webData, "text/html; charset=UTF-8", null);


        //Actualizacion de contenido de la webview.
        final Handler myHandler = new Handler();
        flgCiclo = 0;
        flgOrigen = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!WebActivity.this.isFinishing() && flgOrigen == 0) {
                    try {
                        Thread.sleep(5000);
                        myHandler.post(mMyRunnable);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();

        Ret = (Button) findViewById(R.id.buttonRet);
        Ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                } else if (url.contains("registro.php")) {
                    flgOrigen = 1;
                }


                return shouldOverride;
            }
        });


    }

    //Here's a runnable/handler combo
    private Runnable mMyRunnable = new Runnable() {
        @Override
        public void run() {

            boolean bBloq = webcontent.contains("bloqueado");
            if (bBloq) {
                Intent intent = new Intent(WebActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            } else {

                //nuevo mensaje
                ServiceUtils.getMensajes(mContext);
                ServiceUtils.getViajes(mContext, imei, telCompleto);
            }
            if (flgCiclo == 0) {
                flgCiclo = 1;
            } else {
                flgCiclo = 2;
            }

        }
    };

    @Subscribe()
    public void processMensajes(MensajesEvent data) {
        try {
            JsonObject result = data.getObject();
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

            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Subscribe()
    public void processViajes(ViajesEvent data) {

        try {
            webView.loadData(data.getDataString(), "text/html; charset=UTF-8", null);
            webcontent = data.getDataString();

        } catch (Exception e1) {
            e1.printStackTrace();
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
    public void onPause() {
        super.onPause();
        try {
            myHandler.removeCallbacks(mMyRunnable);
        } catch (NullPointerException e) {
            Log.e("Remiscar ", "ERR - " + e);
        }
    }
}


