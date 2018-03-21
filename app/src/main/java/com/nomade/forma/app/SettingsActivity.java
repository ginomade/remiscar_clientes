package com.nomade.forma.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class SettingsActivity extends ActionBarActivity {

    public Button EnviarSMS;
    public Button Llamar;
    public String phoneNumber;
    public String smsNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        smsNumber = "02901489090";
        //Envio de SMS
        EnviarSMS = (Button) findViewById(R.id.buttonSMS);
        EnviarSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
                        + smsNumber)));
            }
        });

        //Llamado
        phoneNumber = "02901422222";
        Llamar = (Button) findViewById(R.id.buttonLlamar);
        Llamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"
                        + phoneNumber)));
            }
        });

    }

}
