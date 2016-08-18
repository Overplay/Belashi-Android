package tv.ougrglass.belashiandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
<<<<<<< HEAD
import android.webkit.JavascriptInterface;
=======
>>>>>>> 7e7b271df751d1bf9a2a32302fb37b2cb9ebd085
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

<<<<<<< HEAD
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * BelashiAndroid Created by logansaso on 8/12/16.
 */
public class ControllerActivity extends AppCompatActivity {

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
=======
/**
 * BelashiAndroid Created by logansaso on 8/12/16.
 */
public class ControllerActivity extends AppCompatActivity{

    WebView mView ;
    String mUrl ;

    Intent mControllerIntent ;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller); //Set the content view to the main activity

        mControllerIntent = getIntent();
>>>>>>> 7e7b271df751d1bf9a2a32302fb37b2cb9ebd085

        mView = (WebView) findViewById(R.id.controllerWebView);
        WebSettings viewSettings = mView.getSettings();

<<<<<<< HEAD

        viewSettings.setJavaScriptEnabled(true);

        mView.setWebViewClient(new WebViewClient());
        mView.loadUrl("http://" + mIPAddress.substring(1) + ":9090/www/control/index.html");

=======
        mUrl = mControllerIntent.getStringExtra("url");

        viewSettings.setJavaScriptEnabled(true);

        mView.setWebViewClient(new WebViewClient(){

            //I know it is deprecated, but the other version doesn't trigger properly
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                Intent newUrlIntent = new Intent(ControllerActivity.this, ControllerActivity.class);
                newUrlIntent.putExtra("url", url);
                startActivity(newUrlIntent); //Pass along the new url to the new intent

                return true; //Should cancel URL loading, no?
            }

        });

        mView.loadUrl(mUrl);
>>>>>>> 7e7b271df751d1bf9a2a32302fb37b2cb9ebd085


    }
}
