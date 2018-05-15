package com.nomade.forma.app;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.gson.JsonObject;
import com.nomade.forma.app.events.BloqueadoEvent;
import com.nomade.forma.app.events.MensajesEvent;
import com.nomade.forma.app.events.ReservasEvent;
import com.nomade.forma.app.events.UbicacionEvent;
import com.nomade.forma.app.utils.GooglePlayServicesHelper;
import com.nomade.forma.app.utils.ServiceUtils;
import com.nomade.forma.app.utils.SharedPrefsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements LocationListener {

    public EditText editTextMens;
    public String telefono = "";
    public String prefijo = "";
    public String imei;
    public Button Enviar;
    public Button buttonMensajes;
    public Double lat;
    public Double lon;
    public String url;
    static String direccion = "";
    public String mensaje;
    public TextView textBloqueado;
    public String estadoWifi = "0";
    public String coordenadas;
    String telCompleto = "";
    Handler mHandler;
    int flg_mens = 0; // flag para mensajes

    private Boolean pedidoEnviado = false;

    Context mContext;
    SharedPrefsUtil sharedPrefs;
    private GooglePlayServicesHelper locationHelper;
    WebView vViajesView;
    WebViewClient yourWebClient;
    RelativeLayout vHomeButton;
    RelativeLayout vWorkButton;
    RelativeLayout vOtrosButton;

    String tNombre, tApellido, tDireccionCasa,
            tDireccionTrabajo, tDireccionAlt, tUsuario;

    Button vButtonDatos;

    String[] mPermission = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    static final String HOME = "CASA";
    static final String WORK = "TRABAJO";
    static final String OTHER = "ALTERNATIVO";


    private static final int MY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vViajesView = (WebView) findViewById(R.id.wv_mensajes);
        setupWebView();
        mHandler = new android.os.Handler();

        mContext = MainActivity.this;
        sharedPrefs = SharedPrefsUtil.getInstance(mContext);
        locationHelper = new GooglePlayServicesHelper(this, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }


        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        initialConfiguration();
        initDatosUsuario();

        //validar celu bloqueado
        ServiceUtils.validarImei(MainActivity.this);

        //fecha y hora en pantalla

        editTextMens = (EditText) findViewById(R.id.editTextMens);
        textBloqueado = (TextView) findViewById(R.id.textBloqueado);

        setBotonesEnvio();

        vButtonDatos = (Button) findViewById(R.id.buttonDatos);
        vButtonDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new
                        Intent(MainActivity.this, DatosActivity.class);
                startActivity(intent);
            }
        });

        //Envio de consulta de mensajes
        buttonMensajes = (Button) findViewById(R.id.buttonMensajes);
        buttonMensajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //manejar los mensajes al usuario con este boton
                Intent intent = new
                        Intent(MainActivity.this, NovedadesActivity.class);
                startActivity(intent);
            }
        });

        if (!isOnline()) {
            Toast.makeText(MainActivity.this, "No hay conexion de datos. Verifique su conexion.", Toast.LENGTH_SHORT).show();

        } else {

        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!MainActivity.this.isFinishing()) {
                    try {
                        Thread.sleep(30000);

                        mHandler.post(mMyRunnable);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();

        yourWebClient = new WebViewClient() {
            // you tell the webclient you want to catch when a url is about to load
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            // here you execute an action when the URL you want is about to load
            @Override
            public void onLoadResource(WebView view, String url) {

            }
        };
    }

    private void initDatosUsuario() {
        tNombre = sharedPrefs.getString("nombre", "");
        tApellido = sharedPrefs.getString("apellido", "");
        tDireccionCasa = sharedPrefs.getString("direccion_casa", "");
        tDireccionTrabajo = sharedPrefs.getString("direccion_trabajo", "");
        tDireccionAlt = sharedPrefs.getString("direccion_alt", "");
        telCompleto = sharedPrefs.getString("telefono", "");
        tUsuario = tNombre + " " + tApellido;
    }

    private void setBotonesEnvio() {
        //Envio de pedido de viaje
        vHomeButton = (RelativeLayout) findViewById(R.id.buttonHome);
        vWorkButton = (RelativeLayout) findViewById(R.id.buttonWork);
        vOtrosButton = (RelativeLayout) findViewById(R.id.buttonOtro);

        vHomeButton.setEnabled(!tDireccionCasa.equals(""));
        vWorkButton.setEnabled(!tDireccionTrabajo.equals(""));
        vOtrosButton.setEnabled(!tDireccionAlt.equals(""));

        vHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarPedidoConPreseleccion(HOME);
            }
        });
        vWorkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarPedidoConPreseleccion(WORK);
            }
        });
        vOtrosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarPedidoConPreseleccion(OTHER);
            }
        });

        Enviar = (Button) findViewById(R.id.buttonEnviar);
        Enviar.setText("Solicitar Movil");
        Enviar.setEnabled(true);
        Enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //envio de login
                try {
                    mensaje = editTextMens.getText().toString();
                    Enviar.setText("GRABANDO PEDIDO");
                    Enviar.setBackgroundColor(Color.parseColor("#0020c2"));
                    Enviar.setTextColor(Color.parseColor("#ffffff"));
                    if (pedidoEnviado) {
                        Toast.makeText(MainActivity.this, "Ya existe un pedido en curso.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (mensaje.equals("")) {
                            Toast.makeText(MainActivity.this, "Indique el origen del viaje.", Toast.LENGTH_SHORT).show();
                            Enviar.setText("Indique el origen!!!");
                        } else {

                            if (telCompleto.toString().equals("")) {
                                Toast.makeText(MainActivity.this, "Indique el número de celular.", Toast.LENGTH_SHORT).show();
                            } else {

                                ServiceUtils.sendReservas(mContext, imei, telCompleto, coordenadas, mensaje, tUsuario);

                            }
                        }
                    }
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enviarPedidoConPreseleccion(String origen) {
        if (pedidoEnviado) {
            Toast.makeText(MainActivity.this, "Ya existe un pedido en curso.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Enviando pedido a " + origen, Toast.LENGTH_SHORT).show();
            String telCompleto = prefijo + telefono;
            ServiceUtils.sendReservas(mContext, imei, telCompleto, coordenadas, origen, tUsuario);
        }
    }

    private void initialConfiguration() {
        try {
            // identificador del equipo segun tipo
            Configuration config = getResources().getConfiguration();
            if (config.smallestScreenWidthDp >= 600) {
                imei = getMacAdd();
                sharedPrefs.saveString("imei", imei);
                Log.v("Remiscar", "entro por tab");
            } else {

                //Si hay conexion de WIFI pongo el flag a 1.
                if (isWifiOnline().equals("wifi")) {
                    estadoWifi = "1";
                }

                telefono = getPhoneNumber();
                //obtengo imei
                imei = getPhoneImei();//"359781041848146"
                sharedPrefs.saveString("imei", imei);
                Log.i("Remiscar", "start imei "+ imei);
            }

        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, "Error de ejecucion." + ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e("error Forma", ex.toString());
            finish();
        }
    }

    private void irAReclamos() {
        try {
            String telCompleto = prefijo + telefono;
            Intent intent = new
                    Intent(MainActivity.this, ReclamosActivity.class);
            startActivity(intent);
            //asReclamos(imei, telCompleto);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupWebView() {
        WebSettings webSettings = vViajesView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable Javascript.
        vViajesView.setWebViewClient(yourWebClient);

        webSettings.setAllowFileAccessFromFileURLs(true);
        String finalUrl = ServiceUtils.url_viajes + "?IMEI=" + imei + "&Celular=" + telCompleto;
        Log.w("Remiscar", "viajes: "+ finalUrl);
        vViajesView.loadUrl(finalUrl);
    }


    private void checkPermissions() {
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission[0])
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[1])
                            != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[2])
                            != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[3])
                            != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[4])
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        mPermission, MY_PERMISSIONS_REQUEST);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
                && grantResults[3] == PackageManager.PERMISSION_GRANTED
                && grantResults[4] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted.


        } else {

            // permission denied.
            Toast.makeText(mContext, "Faltan permisos necesarios para funcionar.", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private Runnable mMyRunnable = new Runnable() {
        @Override
        public void run() {

            ServiceUtils.getMensajes(mContext);

        }
    };

    @Subscribe()
    public void processMensajes(MensajesEvent data) {
        try {
            JsonObject result = data.getObject();
            int success = result.get("result").getAsInt();
            if (success == 0) {
                Log.e("Remiscar ", "sin mensajes.");
                flg_mens = 0;
                buttonMensajes.setText("Ver Mis Mensajes");
                buttonMensajes.setBackgroundColor(Color.parseColor("#4863a0"));
                buttonMensajes.setTextColor(Color.parseColor("#d5d9ea"));
            } else if (success == 1) {
                Log.e("Remiscar ", "Hay mensajes.");
                pedidoEnviado = false;
                if (flg_mens == 0) {
                    Toast.makeText(getApplicationContext(), "Hay nuevos mensajes para usted.", Toast.LENGTH_SHORT).show();
                    final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.c2answer);
                    mp.start();
                    buttonMensajes.setText("Nuevo mensaje");
                    buttonMensajes.setBackgroundColor(Color.parseColor("#ff0000"));
                    buttonMensajes.setTextColor(Color.parseColor("#ffffff"));
                    flg_mens = 1;
                } else {

                }
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public String getMacAdd() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //accion para lanzar pantalla de contacto desde el menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Menu Settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_reclamos) {
            irAReclamos();
        }
        return super.onOptionsItemSelected(item);
    }

    //Obtener numero de movil
    private String getPhoneNumber() {
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

        }
        assert mTelephonyManager != null;
        return mTelephonyManager.getLine1Number();
    }

    //Obtener numero de imei
    private String getPhoneImei() {
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

        }
        return mTelephonyManager.getDeviceId();
    }


    @Override
    public void onLocationChanged(Location location) {

        String str = location.getLatitude() + "," + location.getLongitude();
        lat = (Double) location.getLatitude();
        lon = (Double) location.getLongitude();
        coordenadas = str;

    }

    private void getSingleLocation() {
        Location singleLocation = locationHelper.getLastLocation();
        lat = (Double) singleLocation.getLatitude();
        lon = (Double) singleLocation.getLongitude();
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    //verifico estado de wifi
    public String isWifiOnline() {
        try {
            ConnectivityManager conMan = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            //Recogemos el estado del 3G
            //como vemos se recoge con el parámetro 0

            NetworkInfo.State internet_movil = conMan.getNetworkInfo(0).getState();
            //NetworkInfo.State internet_movil =NetworkInfo.State.DISCONNECTED;
            //Recogemos el estado del wifi
            //En este caso se recoge con el parámetro 1
            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
            //Miramos si el WIFI está conectado o conectandose...
            if (wifi == NetworkInfo.State.CONNECTED
                    || wifi == NetworkInfo.State.CONNECTING) {
                ///////////////
                //El movil está conectado por WIFI
                return "wifi";
                //Si no esta por WIFI comprobamos si está conectado o conectandose internet 3G
            } else if ((internet_movil == NetworkInfo.State.CONNECTED
                    || internet_movil == NetworkInfo.State.CONNECTING)) {
                ///////////////
                //El movil está conectado por 3G
                return "3g";
            } else {
                return "";
            }
        } catch (NullPointerException ex) {
            Toast.makeText(getBaseContext(), "Network error." + ex, Toast.LENGTH_LONG).show();
            return "";
        }
    }

    @Subscribe()
    public void processUbicacion(UbicacionEvent data) {

        try {
            JsonObject result = data.getObject();
            Log.d("Remiscar ", "LOC --" + result.getAsString());

            JsonObject location = result.getAsJsonArray("results").get(0).getAsJsonObject();
            String location_string = location.get("formatted_address").getAsString();
            direccion = location_string;

        } catch (Exception ee) {

            Toast.makeText(getBaseContext(), "No se puede recuperar ubicacion.", Toast.LENGTH_LONG).show();
            Log.d("Remiscar ", "No se puede recuperar ubicacion.");
        }

    }

    @Subscribe()
    public void processBloqueado(BloqueadoEvent data) {

        try {
            JsonObject result = data.getObject();
            int success = result.get("result").getAsInt();
            if (success == 0) {
                Log.e("Remiscar ", "celular validado.");

            } else if (success == 1) {
                Log.e("Remiscar ", "celular bloqueado.");
                setBloqueado();
            }

        } catch (Exception ee) {

            Log.d("Remiscar ", "No se puede validar celular.");
        }

    }

    private void setBloqueado(){
        textBloqueado.setVisibility(View.VISIBLE);
        vHomeButton.setEnabled(false);
        vWorkButton.setEnabled(false);
        vOtrosButton.setEnabled(false);
        vViajesView.setVisibility(View.GONE);
        buttonMensajes.setEnabled(false);
        Enviar.setEnabled(false);
        vButtonDatos.setEnabled(false);
    }

    @Subscribe()
    public void processReservas(ReservasEvent data) {

        try {
            if (data.getDataString().equals("ok")) {
                Toast.makeText(getBaseContext(), "Pedido enviado.", Toast.LENGTH_LONG).show();
                pedidoEnviado = true;
                Enviar.setEnabled(false);
                Enviar.setText("Enviando");
            } else {
                sharedPrefs.saveString("webresult", data.getDataString());

                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        return;
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        locationHelper.onPause();
        mHandler.removeCallbacks(mMyRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        locationHelper.onResume(MainActivity.this);
        initDatosUsuario();
        setBotonesEnvio();
    }

}