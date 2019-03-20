package com.nomade.forma.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.nomade.forma.app.events.MpPreferenceEvent;
import com.nomade.forma.app.utils.ServiceUtils;
import com.nomade.forma.app.utils.SharedPrefsUtil;

import org.greenrobot.eventbus.Subscribe;

public class PaymentActivity extends AppCompatActivity {

    private static final String PREFERENCE_ID = "99103401-f93caed8-88f0-4e98-a9f7-6c01edc72e10";
    private static final String PUBLIC_KEY = "TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a";
    private static int REQUEST_CODE = 11;
    Context context;
    SharedPrefsUtil sharedPrefs;
    String telCompleto;
    String imei;
    Button vPayButton;

    String checkoutPreferenceId = PREFERENCE_ID;

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

        imei = sharedPrefs.getString("imei", "");
        telCompleto = sharedPrefs.getString("telefono", "");

        getPreferenceId();
    }

    private void getPreferenceId() {
        ServiceUtils.getMPPreferenceId(context, imei, telCompleto);
    }

    public void submit() {

        startMercadoPagoCheckout(checkoutPreferenceId);

    }

    @Subscribe()
    public void processPreferenceEvent(MpPreferenceEvent data) {
        JsonObject result = data.getObject();
        int success = result.get("result").getAsInt();
        if (success == 0) {
            checkoutPreferenceId = result.get("preference_id").getAsString();
            vPayButton.setEnabled(true);
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
                ((TextView) findViewById(R.id.mp_results)).setText("Resultado del pago: " + payment.getPaymentStatus());
                //Done!
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null && data.getExtras() != null
                        && data.getExtras().containsKey(MercadoPagoCheckout.EXTRA_ERROR)) {
                    final MercadoPagoError mercadoPagoError =
                            (MercadoPagoError) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_ERROR);
                    ((TextView) findViewById(R.id.mp_results)).setText("Error: " + mercadoPagoError.getMessage());
                    //Resolve error in checkout
                } else {
                    //Resolve canceled checkout
                }
            }
        }
    }
}
