package com.nomade.forma.app;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.gson.JsonObject;
import com.nomade.forma.app.events.BloqueadoEvent;
import com.nomade.forma.app.events.MainViewEvent;
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
    public Button buttonEnviarPedido;
    public Button buttonMensajes;
    public String url;
    static String direccion = "";
    public String mensaje;
    public TextView textBloqueado;
    public String estadoWifi = "0";
    public String coordenadas = "";
    public String reservas = "";
    String telCompleto = "";
    int flg_mens = 0; // flag para mensajes

    String webContent = "";

    private boolean pedidoEnviado = false;
    private boolean enviandoPedido = false;

    //tiempo de refresco de webview en milisegundos
    private static int REFRESH_TIME = 5000;

    private GooglePlayServicesHelper locationHelper;

    Context mContext;
    SharedPrefsUtil sharedPrefs;
    WebView vViajesView;

    RelativeLayout vHomeButton;
    RelativeLayout vWorkButton;
    RelativeLayout vOtrosButton;
    FrameLayout vLocIndicator;

    String tNombre, tApellido, tDireccionCasa,
            tDireccionTrabajo, tDireccionAlt, tUsuario;

    Button vButtonDatos, vBtnPagos;

    String[] mPermission = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private static final int MY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        vViajesView = (WebView) findViewById(R.id.wv_mensajes);
        vLocIndicator = (FrameLayout) findViewById(R.id.fl_location_indicator);
        sharedPrefs = SharedPrefsUtil.getInstance(mContext);

        reservas = "";
        guardarReserva(reservas);
        locationHelper = new GooglePlayServicesHelper(this, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        } else {
            initialConfiguration();
        }

        setLocationOff();
        setupWebView();
        getMainData();
        initWebview();

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

    private void guardarReserva(String reserva) {
        sharedPrefs.saveString("reserva", reserva);
        reservas = reserva;
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
                enviarPedidoConPreseleccion(tDireccionCasa);
            }
        });
        vWorkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarPedidoConPreseleccion(tDireccionTrabajo);
            }
        });
        vOtrosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarPedidoConPreseleccion(tDireccionAlt);
            }
        });

        buttonEnviarPedido = (Button) findViewById(R.id.buttonEnviar);
        enviandoPedido = true;
        setBotonPedidoEstadoInicial();
        buttonEnviarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //envio de login
                try {
                    mensaje = editTextMens.getText().toString();
                    buttonEnviarPedido.setText("GRABANDO PEDIDO");
                    hideKeyboard(MainActivity.this);
                    if (pedidoEnviado) {
                        Toast.makeText(MainActivity.this, "Ya existe un pedido en curso.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (mensaje.equals("")) {
                            Toast.makeText(MainActivity.this, "Indique el origen del viaje.", Toast.LENGTH_SHORT).show();
                            buttonEnviarPedido.setText("Indique el origen!!!");
                        } else {

                            if (telCompleto.toString().equals("")) {
                                Toast.makeText(MainActivity.this, "Indique el número de celular.", Toast.LENGTH_SHORT).show();
                            } else {

                                ServiceUtils.sendReservas(mContext, imei, telCompleto, coordenadas, mensaje, tUsuario);
                                enviandoPedido = true;
                            }
                        }
                    }
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vBtnPagos = (Button) findViewById(R.id.buttonPagos);
        vBtnPagos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
        enableButtonPagos(false);
    }

    private void setBotonPedidoEstadoInicial() {
        if (enviandoPedido) {
            buttonEnviarPedido.setText("Solicitar Movil");
            buttonEnviarPedido.setEnabled(false);
            enviandoPedido = false;
            editTextMens.setText("");
            hideKeyboard(MainActivity.this);
        }
    }

    private void setLocationOn() {
        vLocIndicator.setBackgroundColor(Color.GREEN);
    }

    private void setLocationOff() {
        vLocIndicator.setBackgroundColor(Color.GRAY);
    }

    private void enviarPedidoConPreseleccion(String origen) {
        if (pedidoEnviado) {
            Toast.makeText(MainActivity.this, "Ya existe un pedido en curso.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Enviando pedido a " + origen, Toast.LENGTH_SHORT).show();
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

                //obtengo imei
                imei = getPhoneImei();//"359781041848146"
                sharedPrefs.saveString("imei", imei);
                Log.i("Remiscar", "start imei " + imei);
            }

            initDatosUsuario();


            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            //validar celu bloqueado
            if (imei.equals("")) {
                Toast.makeText(mContext, "Error de sistema. No se puede obtener imei.", Toast.LENGTH_LONG);
                Log.e("remiscar", "error obteniendo imei.");
                finish();
            } else {
                ServiceUtils.validarImei(MainActivity.this);
            }

            editTextMens = (EditText) findViewById(R.id.editTextMens);
            editTextMens.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    buttonEnviarPedido.setEnabled(s.length() > 0);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //nothing

                }
            });
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
                    initWebview();
                    resetBotonMensajes();
                }
            });

            if (!isOnline()) {
                Toast.makeText(MainActivity.this, "No hay conexion de datos. Verifique su conexion.", Toast.LENGTH_SHORT).show();

            } else {

            }


        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, "Error de ejecucion." + ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e("remiscar", ex.toString());
            finish();
        }
    }

    final Handler handler = new Handler();
    final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            ServiceUtils.getMensajes(mContext);
            getMainData();
            initWebview();
            setBotonPedidoEstadoInicial();

            handler.postDelayed(runnableCode, REFRESH_TIME);
            getSingleLocation();
        }
    };

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
        final String finalUrl = ServiceUtils.url_viajes + "?IMEI=" + imei + "&Celular=" + telCompleto;
        WebSettings webSettings = vViajesView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable Javascript.



        vViajesView.setWebViewClient(new WebViewClient() {
            // you tell the webclient you want to catch when a url is about to load
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                getMainData(url);
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getMainData(request.getUrl().toString());
                }
                return false;
            }


        });

    }

    private void getMainData(){
        ServiceUtils.getMainData(imei, telCompleto);
    }

    private void getMainData(String url){
        ServiceUtils.getMainData(url);
    }

    @Subscribe()
    public void processWebview(MainViewEvent data) {
        webContent = data.getContent();

        enableButtonPagos(!data.getEmpresa().equals(""));
        guardarReserva(data.getReserva());
    }

    private void loadWebView(){
        vViajesView.loadData(webContent, "text/html", "UTF-8");
    }

    private void initWebview(){
        vViajesView.loadUrl(ServiceUtils.url_viajes
                + "?IMEI=" + imei
                + "&Celular=" + telCompleto);
    }


    private void checkPermissions() {
        try {
            if (ContextCompat.checkSelfPermission(this, mPermission[0])
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[1])
                            != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[2])
                            != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[3])
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        mPermission, MY_PERMISSIONS_REQUEST);

            } else {
                initialConfiguration();
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
                && grantResults[3] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted.

            initialConfiguration();
        } else {

            // permission denied.
            Toast.makeText(mContext, "Faltan permisos necesarios para funcionar.", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Subscribe()
    public void processMensajes(MensajesEvent data) {
        try {
            JsonObject result = data.getObject();
            int success = result.get("result").getAsInt();
            if (success == 0) {
                Log.e("Remiscar ", "sin mensajes.");
                flg_mens = 0;
                resetBotonMensajes();
            } else if (success == 1) {
                Log.e("Remiscar ", "Hay mensajes.");
                pedidoEnviado = false;
                setBotonPedidoEstadoInicial();
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

    private void resetBotonMensajes() {
        if (buttonMensajes != null) {
            buttonMensajes.setText("Ver Mensajes");
            buttonMensajes.setBackgroundColor(Color.parseColor("#4863a0"));
            buttonMensajes.setTextColor(Color.parseColor("#d5d9ea"));
        }
    }

    private void enableButtonPagos(boolean enable) {
        if (vBtnPagos != null) {
            vBtnPagos.setEnabled(enable);
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
        } else if (id == R.id.action_privacy) {
            Intent intent = new
                    Intent(MainActivity.this, PrivacyActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //Obtener numero de imei
    private String getPhoneImei() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager mTelephonyManager;
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            return mTelephonyManager.getDeviceId();
        }
        return "";
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

    private void setBloqueado() {
        textBloqueado.setVisibility(View.VISIBLE);
        vHomeButton.setEnabled(false);
        vWorkButton.setEnabled(false);
        vOtrosButton.setEnabled(false);
        vViajesView.setVisibility(View.GONE);
        buttonMensajes.setEnabled(false);
        buttonEnviarPedido.setEnabled(false);
        vButtonDatos.setEnabled(false);
    }

    @Subscribe()
    public void processReservas(ReservasEvent data) {

        try {
            if (data.getDataString().equals("ok")) {
                Toast.makeText(getBaseContext(), "Pedido enviado.", Toast.LENGTH_LONG).show();
                pedidoEnviado = true;
                buttonEnviarPedido.setEnabled(false);
                buttonEnviarPedido.setText("Enviando");
            } else {
                sharedPrefs.saveString("webresult", data.getDataString());

                String webData = "<?xml version='1.0' encoding='utf-8'?><html><body>" + data.getDataString() + " </body></html>";
                vViajesView.loadData(webData, "text/html; charset=UTF-8", null);

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
        handler.removeCallbacks(runnableCode);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        initialConfiguration();

        checkLocationServices();

        handler.post(runnableCode);

        if (locationHelper != null) {
            locationHelper.onResume(this);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            String str = location.getLatitude() + "," + location.getLongitude();
            //54°48'02.1"S 68°17'21.9"W -54.4802,-68.1721
            coordenadas = str;
            Log.d("Remiscar ", " - set location -" + str);
            setLocationOn();
        }
    }

    private void getSingleLocation() {
        if (locationHelper != null) {
            Location singleLocation = locationHelper.getLastLocation();
            if (singleLocation != null) {
                String str = singleLocation.getLatitude() + "," + singleLocation.getLongitude();
                coordenadas = str;
            }
            setLocationOn();
        }
    }

    private void checkLocationServices() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setMessage("Activar localización");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        }
    }
}