package com.nomade.forma.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

// pantalla de datos de novedades para el movil
public class NovedadesActivity extends AppCompatActivity {

    WebView mWebView;
    private static final String URL = "http://carlitosbahia.dynns.com/movil/Mmensajes.php";

    public Button Ret;
    String params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novedades);

        SharedPreferences settings = getSharedPreferences("RemisData", 0);
        String imei = settings.getString("imei", "");

        params = "?IMEI=" + imei;

        mWebView = (WebView) findViewById(R.id.webView);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable Javascript.
        mWebView.setWebViewClient(yourWebClient);

        webSettings.setAllowFileAccessFromFileURLs(true);  // Enable HTML Imports to access file://.
        //webSettings.setAllowUniversalAccessFromFileURLs(true);

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.loadUrl(URL + params);

        Ret = (Button) findViewById(R.id.buttonRet);
        Ret.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }

        });

    }

    WebViewClient yourWebClient = new WebViewClient() {
        // you tell the webclient you want to catch when a url is about to load
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //mWebView.loadUrl(url);
            return true;
        }

        // here you execute an action when the URL you want is about to load
        @Override
        public void onLoadResource(WebView view, String url) {

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_novedades, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        super.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();

        finish();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }
}
