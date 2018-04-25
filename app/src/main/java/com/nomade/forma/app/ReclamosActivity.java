package com.nomade.forma.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

//pantalla de datos de viajes
public class ReclamosActivity extends AppCompatActivity {

    WebView mWebView;
    public String imei;
    public String telefono;
    public String prefijo;
    public Button Ret, send;
    EditText mensaje;
    private static final String URL = "http://carlitosbahia.dynns.com/movil/reclamosMovil.php";
    String TAG_SUCCESS = "result";
    LinearLayout ll_mensaje, ll_confirmacion;

    //JSONParser jsonParser = new JSONParser();

    // String imei, resp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamos);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ReclamosActivity.this);//getSharedPreferences("RemisData", 0);
        //imei = settings.getString("imei", "");
        imei = getPhoneImei();
        telefono = settings.getString("celular", "");
        prefijo = settings.getString("car", "");
        Log.e("Remiscar:", "Reclamos  - celu " + telefono);
        Log.e("Remiscar:", "Reclamos  - imei " + imei);
        final String telCompleto = prefijo + telefono;

        ll_mensaje = (LinearLayout) findViewById(R.id.mensaje);
        ll_confirmacion = (LinearLayout) findViewById(R.id.confirmacion);

        mensaje = (EditText) findViewById(R.id.editReclamo);
        //mensaje= EditText.getText().toString();

        mostrarForm();

        send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //asReclamos(imei,  telCompleto, mensaje.getText().toString());
                try {
                    String texto = URLEncoder.encode(mensaje.getText().toString(), "utf-8");
                    if (texto.equals("")) {
                        Toast.makeText(ReclamosActivity.this, "Escriba el mensaje.", Toast.LENGTH_SHORT).show();
                        send.setText("Escribir y enviar!!!");
                    } else {

                        asReclamos(imei, telCompleto, texto);
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }

        });


        //asReclamos(imei, telefono);

        //
        Ret = (Button) findViewById(R.id.buttonRet);
        Ret.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.e("Remiscar:", "Reclamos  - enviando...");
                finish();
            }

        });
    }


    private void asReclamos(String imei, String celu, String mensaje) {
        Ion.with(ReclamosActivity.this)
                .load("http://carlitosbahia.dynns.com/movil/reclamosMovil.php?&IMEI=" + imei + "&Celular=" + celu + "&Descripcion=" + mensaje)

                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.e("Remiscar:", "Reclamos  - " + result);
                        try {
                            if (result.contains("ok")) {
                                Toast.makeText(ReclamosActivity.this, "Reclamo enviado.", Toast.LENGTH_SHORT).show();
                                mostrarConf();
                            } else {
                                Toast.makeText(ReclamosActivity.this, "Error al enviar el mensaje.", Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    private String getPhoneImei() {
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager.getDeviceId();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        super.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();

        finish();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

    private void mostrarForm() {
        ll_confirmacion.setVisibility(View.GONE);
        ll_mensaje.setVisibility(View.VISIBLE);
    }

    private void mostrarConf() {
        ll_confirmacion.setVisibility(View.VISIBLE);
        ll_mensaje.setVisibility(View.GONE);
    }

}

