package com.nomade.forma.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class SettingsActivity extends AppCompatActivity {

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
