package tv.ourglass.belashiandroid;

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
public class ControllerActivity extends AppCompatActivity {

    WebView mView ;
    String mURL;

    Intent controllerIntent ;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        controllerIntent = getIntent();
        setContentView(R.layout.activity_controller);

        mURL = controllerIntent.getStringExtra("url");

        mView = (WebView) findViewById(R.id.controllerWebView);
        WebSettings viewSettings = mView.getSettings();


        viewSettings.setJavaScriptEnabled(true);

        mView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Intent newUrlIntent = new Intent(ControllerActivity.this, ControllerActivity.class);
                newUrlIntent.putExtra("url", url);
                startActivity(newUrlIntent);

                return super.shouldOverrideUrlLoading(view, url);
            }

        });
        mView.loadUrl(mURL);

    }
}
