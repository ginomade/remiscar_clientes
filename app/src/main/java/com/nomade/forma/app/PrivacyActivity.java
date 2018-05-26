package com.nomade.forma.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.nomade.forma.app.utils.ServiceUtils;

public class PrivacyActivity extends AppCompatActivity {

    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(ServiceUtils.url_privacy);
    }
}
