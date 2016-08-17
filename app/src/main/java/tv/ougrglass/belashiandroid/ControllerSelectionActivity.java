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
public class ControllerSelectionActivity extends AppCompatActivity {

    WebView mView ;
    String mIPAddress;

    Intent controllerIntent ;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        controllerIntent = getIntent();
        setContentView(R.layout.activity_controller);

        mIPAddress = controllerIntent.getStringExtra("ogIP");

        mView = (WebView) findViewById(R.id.controllerWebView);
        WebSettings viewSettings = mView.getSettings();


        viewSettings.setJavaScriptEnabled(true);

        mView.setWebViewClient(new WebViewClient(){

            //I know it is deprecated, but the other version doesnt trigger
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                Intent newUrlIntent = new Intent(ControllerSelectionActivity.this, ControllerActivity.class);
                newUrlIntent.putExtra("url", url);
                startActivity(newUrlIntent);

                return true; //Should cancel loading, no?
            }

        });

        mView.loadUrl("http://" + mIPAddress.substring(1) + ":9090/www/control/index.html");

    }
}