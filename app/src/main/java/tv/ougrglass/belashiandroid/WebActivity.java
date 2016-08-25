package tv.ougrglass.belashiandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * BelashiAndroid Created by logansaso on 8/12/16.
 */
public class WebActivity extends AppCompatActivity {




    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        Intent controllerIntent;
        WebView view ;
        String url;
        controllerIntent = getIntent(); //get the intent passed
        setContentView(R.layout.activity_controller); //set the content view

        url = controllerIntent.getStringExtra("url"); //Get the url from the intent

        view = (WebView) findViewById(R.id.controllerWebView); //get the active webview
        WebSettings viewSettings = view.getSettings(); //get the settings


        viewSettings.setJavaScriptEnabled(true); //set the javascript enabled

        view.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Intent newUrlIntent = new Intent(WebActivity.this, WebActivity.class); //create the intent
                newUrlIntent.putExtra("url", url); //set the url passed to the new instance
                startActivity(newUrlIntent); //start a new WebActivity for the new url

                return super.shouldOverrideUrlLoading(view, url);
            }

        });
        view.loadUrl(url); //load the url originially passed

    }
}
