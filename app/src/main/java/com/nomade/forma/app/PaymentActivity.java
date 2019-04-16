package com.nomade.forma.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.nomade.forma.app.events.MpPreferenceEvent;
import com.nomade.forma.app.utils.ServiceUtils;
import com.nomade.forma.app.utils.SharedPrefsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PaymentActivity extends AppCompatActivity {

    private static final String PUBLIC_KEY = "TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a";
    private static int REQUEST_CODE = 11;
    Context context;
    SharedPrefsUtil sharedPrefs;
    String telCompleto, reserva;
    String imei;
    Button vPayButton;
    ProgressBar vProgress;

    String checkoutPreferenceId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        context = PaymentActivity.this;
        sharedPrefs = SharedPrefsUtil.getInstance(context);
        vPayButton = (Button) findViewById(R.id.payButton);
        vPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
        vPayButton.setEnabled(false);

        vProgress = (ProgressBar) findViewById(R.id.progressBar2);

        imei = sharedPrefs.getString("imei", "");
        telCompleto = sharedPrefs.getString("telefono", "");
        reserva = sharedPrefs.getString("reserva", "");

        getPreferenceId();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    private void getPreferenceId() {
        if(!reserva.equals("")){
            ServiceUtils.getMPPreferenceId(context, imei, telCompleto, reserva);
        }else{
            Log.w("remiscar", "error en valor de reserva.");
        }
    }

    public void submit() {

        startMercadoPagoCheckout(checkoutPreferenceId);

    }

    @Subscribe()
    public void processPreferenceEvent(MpPreferenceEvent data) {
        JsonObject result = data.getObject();
        int success = result.get("result").getAsInt();
        vProgress.setVisibility(View.GONE);
        if (success == 0) {
            checkoutPreferenceId = result.get("preference_id").getAsString();
            vPayButton.setEnabled(true);
            reserva = "";
        }
    }

    /*
        curl -X POST \
                'https://api.mercadopago.com/checkout/preferences?access_token=TEST-8003123851032516-101716-bc39e29b28b102576ae57cde2a51a815-99103401' \
                -H 'Content-Type: application/json' \
                -d '{
                "items": [
        {
            "title": "remiscar",
                "description": "1 viaje",
                "quantity": 1,
                "currency_id": "ARS",
                "unit_price": 120.0
        }
               ],
                       "payer": {
            "email": "payer@email.com"
        },
                "payment_methods": {
            "excluded_payment_types":[
            {"id":"atm"}
                ],
            "installments": 1
        }
    }'
    */

    private void startMercadoPagoCheckout(String checkoutPreferenceId) {
        final AdvancedConfiguration advancedConfiguration =
                new AdvancedConfiguration.Builder()
                        .setBankDealsEnabled(false)
                        .build();
        new MercadoPagoCheckout.Builder(PUBLIC_KEY, checkoutPreferenceId)
                .setAdvancedConfiguration(advancedConfiguration).build()
                .startPayment(PaymentActivity.this, REQUEST_CODE);
        ((TextView) findViewById(R.id.mp_results)).setText("Pago iniciado");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
                final Payment payment = (Payment) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT);
                setMessage("Resultado del pago: " + payment.getPaymentStatus());

                ServiceUtils.sendPaymentConfirmation(PaymentActivity.this, imei, reserva,
                        payment.getPaymentStatus(), payment.getPaymentStatusDetail(), payment.getId().toString(),
                        String.valueOf(payment.getTransactionDetails().getNetReceivedAmount()));
                vPayButton.setEnabled(false);
                //Done!
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null && data.getExtras() != null
                        && data.getExtras().containsKey(MercadoPagoCheckout.EXTRA_ERROR)) {
                    final MercadoPagoError mercadoPagoError =
                            (MercadoPagoError) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_ERROR);
                    final Payment payment = (Payment) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT);

                    String error = "";
                    if (payment != null) {
                        error = payment.getPaymentStatus();
                    } else {
                        error = mercadoPagoError.getMessage();
                    }
                    setMessage("Error: " + mercadoPagoError.getMessage());
                    ServiceUtils.sendPaymentConfirmation(PaymentActivity.this, imei, reserva,
                            error, "", "", "");
                    vPayButton.setEnabled(false);
                    //Resolve error in checkout
                } else {
                    //Resolve canceled checkout
                    ServiceUtils.sendPaymentConfirmation(PaymentActivity.this, imei, reserva,
                            "CANCELED", "", "", "");
                    vPayButton.setEnabled(false);
                }
            }
        }
    }

    private void setMessage(String mess) {
        TextView vMessage = (TextView) findViewById(R.id.mp_results);
        vMessage.setVisibility(View.VISIBLE);
        vMessage.setText(mess);
    }
}
