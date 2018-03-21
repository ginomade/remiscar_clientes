package com.nomade.forma.app;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.location.Address;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends Activity implements LocationListener {

    public EditText editTextMens; //= (EditText) findViewById(R.id.editTextMens);
    public EditText editTextStatus; //= (EditText) findViewById(R.id.editTextStatus);
    public EditText editTextFecha;
    public EditText editTextHora;
    public EditText editTexCelular;
    public EditText editTextCar;
    public EditText editTexUbicacion;
    public String telefono = "";
    public String prefijo = "";
    public String imei;
    public Button Enviar;
    public Button Viajes;
    public Button Reclamos;
    private LocationManager locationManager;
    public Double lat;
    public Double lon;
    public String url;
    static String direccion = "";
    public String mensaje;
    private ProgressDialog pDialog;
    public TextView textStatus;
    public String estadoWifi ="0";
    public String coordenadas;
    Handler mHandler;
    int flg_mens=0; // flag para mensajes

    private Boolean pedidoEnviado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextStatus = (EditText) findViewById(R.id.editTextStatus);
        editTexUbicacion = (EditText) findViewById(R.id.editTextUbicacion);
        textStatus = (TextView) findViewById(R.id.textStatus);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mHandler = new android.os.Handler();

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            // identificador del equipo segun tipo
            Configuration config = getResources().getConfiguration();
            if (config.smallestScreenWidthDp >= 600){
                imei = getMacAdd();

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,10, this);
                Log.v("start", "entro por tab");
            } else {

                //Si hay conexion de WIFI pongo el flag a 1.
                if (isWifiOnline().equals("wifi")){
                    estadoWifi = "1";
                }

                telefono = getPhoneNumber();
                //obtengo imei
                imei = getPhoneImei();//"359781041848146"

                /* CAL METHOD requestLocationUpdates */
                if (imei.equals("000000000000000")){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,10, this);
                }else{
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000,10, this);
                }
            }
            //obtengo numero del celular
            editTexCelular = (EditText) findViewById(R.id.editTextCelular);
            editTextCar = (EditText) findViewById(R.id.editTextCar);

            if (telefono.equals("") && prefs.getString("celular","").equals("")){
                editTexCelular.setFocusable(true);
                editTexCelular.setClickable(true);
                editTexCelular.setEnabled(true);
                editTextCar.setFocusable(true);
                editTextCar.setClickable(true);
                editTextCar.setEnabled(true);
                Toast.makeText(MainActivity.this, "Ingrese su numero de celular con el 0 y sin el 15.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                editTexCelular.setFocusable(false);
                editTexCelular.setClickable(false);
                editTextStatus.setEnabled(false);
                editTextCar.setFocusable(false);
                editTextCar.setClickable(false);
                editTextCar.setEnabled(false);
                editTexCelular.setText(prefs.getString("celular",telefono));
                editTextCar.setText(prefs.getString("car",prefijo));
                telefono = editTexCelular.getText().toString();
                prefijo = editTextCar.getText().toString();
            }



            editTextStatus.setText(imei);


        }
        catch(Exception ex){
            Toast.makeText(MainActivity.this, "Error de ejecucion." + ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e("error Forma",ex.toString());
            finish();
        }

        //fecha y hora en pantalla
        editTextFecha = (EditText) findViewById(R.id.editTextFecha);
        editTextHora = (EditText) findViewById(R.id.editTextHora);
        editTextMens = (EditText) findViewById(R.id.editTextMens);
        Calendar c = Calendar.getInstance();
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);
        int dia= c.get(Calendar.DATE);
        int mes = c.get(Calendar.MONTH)+1;
        int anio = c.get(Calendar.YEAR);
        editTextFecha.setText(dia +"/"+ mes +"/"+ anio);
        if(minuto < 10) {
            editTextHora.setText(hora+":0"+minuto);
        }else{editTextHora.setText(hora+":"+minuto);}



        //Envio de pedido de viaje
        Enviar = (Button)findViewById(R.id.buttonEnviar);
        Enviar.setText("Solicitar Movil");
        Enviar.setEnabled(true);
        Enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //guardo numero de celular
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("celular", editTexCelular.getText().toString());
                editor.putString("car", editTextCar.getText().toString());
                editor.commit();

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
                            if (editTexCelular.getText().toString().equals("")) {
                                Toast.makeText(MainActivity.this, "Indique el número de celular.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (telefono.equals("")) {
                                    telefono = editTexCelular.getText().toString();
                                    prefijo = editTextCar.getText().toString();
                                }
                                String telCompleto = prefijo + telefono;
                                //new asynclogin().execute(imei, telCompleto, coordenadas, mensaje); //"02901414900", "1916"
                                asLogin(imei, telCompleto, coordenadas, mensaje);
                            }
                        }
                    }
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Envio de consulta de viajes
        Viajes = (Button)findViewById(R.id.buttonMisViajes);
        Viajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //envio de login
                try {
                    if (telefono.equals("")){telefono = editTexCelular.getText().toString();
                        prefijo = editTextCar.getText().toString();}
                    String telCompleto = prefijo + telefono;
                    //new asyncViajes().execute(imei, telCompleto);
                    asViajes(imei, telCompleto);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Envio de consulta de sugerencia
        Reclamos = (Button)findViewById(R.id.buttonReclamos);
        Reclamos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //envio de reclamos
                try {
                    if (telefono.equals("")){telefono =
                            editTexCelular.getText().toString();
                        prefijo = editTextCar.getText().toString();}
                    String telCompleto = prefijo + telefono;
                    Intent intent = new
                            Intent(MainActivity.this, ReclamosActivity.class);
                    startActivity(intent);
                    //asReclamos(imei, telCompleto);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "No se puede enviar el pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        if (!isOnline()){
            Toast.makeText(MainActivity.this, "No hay conexion de datos. Verifique su conexion.", Toast.LENGTH_SHORT).show();
            textStatus.setText("Sin Conexión.");
        }else{
            textStatus.setText("Online.");
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!MainActivity.this.isFinishing()) {
                    try {
                        Thread.sleep(5000);

                        mHandler.post(mMyRunnable);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
    }

    private Runnable mMyRunnable = new Runnable()
    {
        @Override
        public void run()
        {

            try{

                Ion.with(MainActivity.this)
                        .load("http://carlitosbahia.dynns.com/movil/Mmensajes.php")
                        .setBodyParameter("IMEI", imei)
                        .setBodyParameter("Ubicacion", "")
                        .setBodyParameter("geopos", "")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.e("Remiscar ", "Main MMENS - "+result);
                                try {
                                    int success = result.get("result").getAsInt();
                                    if (success == 0) {
                                        Log.e("Remiscar ", "sin mensajes.");
                                        flg_mens=0;
                                        Viajes.setText("Ver Mis Viajes");
                                        Viajes.setBackgroundColor(Color.parseColor("#4863a0"));
                                        Viajes.setTextColor(Color.parseColor("#d5d9ea"));
                                    } else if (success == 1) {
                                        Log.e("Remiscar ", "Hay mensajes.");
                                        pedidoEnviado = false;
                                        if(flg_mens==0){
                                            Toast.makeText(getApplicationContext(), "Hay nuevos mensajes para usted.", Toast.LENGTH_SHORT).show();
                                            final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.c2answer);
                                            mp.start();
                                            Viajes.setText("Nuevo mensaje");
                                            Viajes.setBackgroundColor(Color.parseColor("#ff0000"));
                                            Viajes.setTextColor(Color.parseColor("#ffffff"));
                                            flg_mens=1;
                                        }else{

                                        }
                                    }

                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });

            }catch(Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    };




    public String getMacAdd(){
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
        }
        return super.onOptionsItemSelected(item);
    }

    //Obtener numero de movil
    private String getPhoneNumber(){
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager.getLine1Number();
    }

    //Obtener numero de imei
    private String getPhoneImei(){
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager.getDeviceId();
    }


    @Override
    public void onLocationChanged(Location location) {

        String str = location.getLatitude()+","+location.getLongitude();
        lat = (Double) location.getLatitude();
        lon = (Double) location.getLongitude();
        //Toast.makeText(getBaseContext(), str, Toast.LENGTH_LONG).show();

        /*if(estadoWifi.equals("1") && !MainActivity.this.isFinishing()){
            // en coordenadas va el dato a enviar.
            // Si hay wifi se envia direccion, sino las coordenadas
            getMyLocationAddress();
            coordenadas = direccion;
        }else{
            coordenadas = str;
        }*/
        coordenadas = str;
        editTexUbicacion.setText("Ubicacion detectada.");
        //editTexUbicacion.setText(direccion);
    }


    @Override
    public void onProviderDisabled(String provider) {

        /******** Called when User off Gps *********/
        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        /******** Called when User on Gps  *********/
        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    public void getMyLocationAddress() {

        try {
            url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lon+"&sensor=false&location_type=RANGE_INTERPOLATED&key=AIzaSyBH-28WMgXixowdrfcYpt1d5_Om-OL1LjY";
            //url = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lon+"&sensor=false";
            // call AsynTask to perform network operation on separate thread
            editTexUbicacion.setText("Buscando...");
            new HttpAsyncUbicacion().execute(url);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Could not get address..!", Toast.LENGTH_LONG).show();
        }
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public boolean isConnected(){
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
        }catch(NullPointerException ex){
            Toast.makeText(getBaseContext(), "Network error." + ex, Toast.LENGTH_LONG).show();
            return "";
        }
    }

    private class HttpAsyncUbicacion extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject location = jsonObject.getJSONArray("results").getJSONObject(0);
                String location_string = location.getString("formatted_address");
                direccion = location_string;

            }catch (JSONException e){
                Toast.makeText(getBaseContext(), "No se puede recuperar ubicación.", Toast.LENGTH_LONG).show();
            }
        }
    }



    //INICIO - Tarea asincrona para envio del pedido a origenreservas.php
    private void asLogin(String imei, String celu, String geo, String obs){
        Ion.with(MainActivity.this)
                .load("http://carlitosbahia.dynns.com/movil/origenreservas.php")
                .setBodyParameter("submit", "submit")
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .setBodyParameter("Geoposicion", geo)
                .setBodyParameter("Observaciones", obs)
                .asString(Charset.forName("iso-8859-1"))
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.e("Remiscar ", "Login  - "+result);
                        try {
                            if (result.equals("ok")){
                                Toast.makeText(getBaseContext(), "Pedido enviado.", Toast.LENGTH_LONG).show();
                                pedidoEnviado = true;
                                Enviar.setEnabled(false);
                                Enviar.setText("Enviando");
                            }else{
                                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("webresult", result);
                                editor.commit();

                                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                                startActivity(intent);
                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }


//FIN - Tarea asincrona para envio del pedido a origenreservas.php

    //INICIO - Tarea asincrona consulta de mis viajes a origen.php
    private void asViajes(String imei, String celu){
        Ion.with(MainActivity.this)
                .load("http://carlitosbahia.dynns.com/movil/origen.php")
                .setBodyParameter("IMEI", imei)
                .setBodyParameter("Celular", celu)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.e("Remiscar ", "Login  - " + result);
                        try {
                            if (result.equals("ok")) {

                            } else {

                                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("webresult", result);
                                editor.commit();

                                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                                startActivity(intent);
                            }

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }


//FIN - Tarea asincrona consulta de mis viajes a origen.php

    //INICIO - Tarea asincrona consulta de mis viajes a reclamos.php


//FIN - Tarea asincrona consulta de mis viajes a reclamos.php


    @Override
    public void onBackPressed() {
        finish();
        return;
    }

    @Override
    public void onPause(){
        super.onPause();
        mHandler.removeCallbacks(mMyRunnable);
    }

}