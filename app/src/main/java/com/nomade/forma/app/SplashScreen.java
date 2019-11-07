package com.nomade.forma.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent mainIntent = new Intent().setClass(SplashScreen.this, MainActivity.class);
                startActivity(mainIntent);
                finish();//Destruimos esta activity para prevenir que el usuario retorne aqui presionando el boton Atras.
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_TIME_OUT);//Pasado los 6 segundos dispara la tarea
    }
}
