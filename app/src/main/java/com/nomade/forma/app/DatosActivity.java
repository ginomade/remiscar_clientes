package com.nomade.forma.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nomade.forma.app.utils.SharedPrefsUtil;

public class DatosActivity extends AppCompatActivity {

    EditText tNombre, tApellido, tDireccionCasa,
            tDireccionTrabajo, tDireccionAlt, tTelefono,
            tDni, tEmail;
    Button bSend;

    Context mContext;
    SharedPrefsUtil sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);

        mContext = DatosActivity.this;
        sharedPrefs = SharedPrefsUtil.getInstance(mContext);

        initViewElements();

        initDatos();

        //guardo datos en sharedPreferences de la aplicacion.
        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tNombre.getText().toString().equals("")
                        || tApellido.getText().toString().equals("")
                        || tTelefono.getText().toString().equals("")) {
                    Toast.makeText(mContext, "Falta Nombre, Apellido o Telefono", Toast.LENGTH_LONG).show();
                } else {
                    sharedPrefs.saveString("nombre", tNombre.getText().toString());
                    sharedPrefs.saveString("apellido", tApellido.getText().toString());
                    sharedPrefs.saveString("direccion_casa", tDireccionCasa.getText().toString());
                    sharedPrefs.saveString("direccion_trabajo", tDireccionTrabajo.getText().toString());
                    sharedPrefs.saveString("direccion_alt", tDireccionAlt.getText().toString());
                    sharedPrefs.saveString("telefono", tTelefono.getText().toString());
                    sharedPrefs.saveString("dni", tDni.getText().toString());
                    sharedPrefs.saveString("email", tEmail.getText().toString());
                    Toast.makeText(mContext, "Datos Guardados.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void initDatos() {
        tNombre.setText(sharedPrefs.getString("nombre", ""));
        tApellido.setText(sharedPrefs.getString("apellido", ""));
        tDireccionCasa.setText(sharedPrefs.getString("direccion_casa", ""));
        tDireccionTrabajo.setText(sharedPrefs.getString("direccion_trabajo", ""));
        tDireccionAlt.setText(sharedPrefs.getString("direccion_alt", ""));
        tTelefono.setText(sharedPrefs.getString("telefono", ""));
        tEmail.setText(sharedPrefs.getString("dni", ""));
        tDni.setText(sharedPrefs.getString("email", ""));

    }

    private void initViewElements() {
        tNombre = (EditText) findViewById(R.id.editName);
        tApellido = (EditText) findViewById(R.id.editApellido);
        tDireccionCasa = (EditText) findViewById(R.id.editDomCasa);
        tDireccionTrabajo = (EditText) findViewById(R.id.editDomTrabajo);
        tDireccionAlt = (EditText) findViewById(R.id.editDomAlt);
        tTelefono = (EditText) findViewById(R.id.editTelefono);
        tDni = (EditText) findViewById(R.id.editDNI);
        tEmail = (EditText) findViewById(R.id.editEmail);
        bSend = (Button) findViewById(R.id.buttonSend);

    }
}
