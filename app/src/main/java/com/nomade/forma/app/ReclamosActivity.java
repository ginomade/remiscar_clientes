package com.nomade.forma.app;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nomade.forma.app.events.ReclamosEvent;
import com.nomade.forma.app.utils.ServiceUtils;
import com.nomade.forma.app.utils.SharedPrefsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

//pantalla de datos de viajes
public class ReclamosActivity extends AppCompatActivity {

    WebView mWebView;
    public String imei;
    public String telefono;
    public String prefijo;
    public Button Ret, send;
    String telCompleto = "";
    EditText mensaje;
    private static final String URL = "http://carlitosbahia.dynns.com/movil/reclamosMovil.php";
    String TAG_SUCCESS = "result";
    LinearLayout ll_mensaje, ll_confirmacion;
    String tNombre, tApellido, tUsuario;
    Context mContext;
    SharedPrefsUtil sharedPrefs;

    //JSONParser jsonParser = new JSONParser();

    // String imei, resp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamos);

        mContext = ReclamosActivity.this;
        sharedPrefs = SharedPrefsUtil.getInstance(mContext);
        //imei = settings.getString("imei", "");
        imei = sharedPrefs.getString("imei", "");
        telCompleto = sharedPrefs.getString("telefono", "");
        Log.e("Remiscar:", "Reclamos  - celu " + telCompleto);
        Log.e("Remiscar:", "Reclamos  - imei " + imei);
        tNombre = sharedPrefs.getString("nombre", "");
        tApellido = sharedPrefs.getString("apellido", "");
        tUsuario = tNombre + " " + tApellido;

        ll_mensaje = (LinearLayout) findViewById(R.id.mensaje);
        ll_confirmacion = (LinearLayout) findViewById(R.id.confirmacion);

        mensaje = (EditText) findViewById(R.id.editReclamo);

        mostrarForm();

        send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                try {
                    String texto = URLEncoder.encode(mensaje.getText().toString(), "utf-8");
                    if (texto.equals("")) {
                        Toast.makeText(ReclamosActivity.this, "Escriba el mensaje.", Toast.LENGTH_SHORT).show();
                        send.setText("Escribir y enviar!!!");
                    } else {
                        ServiceUtils.getReclamos(mContext, imei, telCompleto, texto,
                                tUsuario);


                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }

        });

        Ret = (Button) findViewById(R.id.buttonRet);
        Ret.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.e("Remiscar:", "Reclamos  - enviando...");
                finish();
            }

        });
    }

    @Subscribe()
    public void processReclamo(ReclamosEvent data) {

        try {
            if (data.getDataString().contains("ok")) {
                Toast.makeText(ReclamosActivity.this, "Reclamo enviado.", Toast.LENGTH_SHORT).show();
                mostrarConf();
            } else {
                Toast.makeText(ReclamosActivity.this, "Error al enviar el mensaje.", Toast.LENGTH_SHORT).show();

            }
            try {
                Thread.sleep(5000);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        finish();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
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

