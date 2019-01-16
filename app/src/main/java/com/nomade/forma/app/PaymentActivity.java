package com.nomade.forma.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.internal.datasource.MercadoPagoPaymentProcessor;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private static int REQUEST_CODE = 11;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        context = PaymentActivity.this;
    }

    // Método ejecutado al hacer clic en el botón
    public void submit(View view) {

/*.setPublicKey("TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a")
                .setCheckoutPreferenceId("150216849-ceed1ee4-8ab9-4449-869f-f4a8565d386f")*/
        Site site = Sites.ARGENTINA;
        List<Item> items = new ArrayList<>();
        BigDecimal precio = new BigDecimal(150);
        Item unItem = new Item.Builder("viaje", 1, precio).build();
        items.add(unItem);
        CheckoutPreference preference = new CheckoutPreference.Builder(site, "pepe@gmail.com", items).build();
        PaymentProcessor paymentProcessor = new MercadoPagoPaymentProcessor();
        PaymentConfiguration paymentConf = new PaymentConfiguration.Builder(paymentProcessor).build();
        startMercadoPagoCheckout(preference, paymentConf);


    }

    private void startMercadoPagoCheckout(CheckoutPreference checkoutPreference, PaymentConfiguration paymentConf) {
        new MercadoPagoCheckout.Builder("TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a", checkoutPreference, paymentConf).build()
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
                    ((TextView) findViewById(R.id.mp_results)).setText("Error: " +  mercadoPagoError.getMessage());
                    //Resolve error in checkout
                } else {
                    //Resolve canceled checkout
                }
            }
        }
    }
}
