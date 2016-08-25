package tv.ougrglass.belashiandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



/**
 * Created by logansaso on 7/13/16.
 */
public class WIFIBroadcastReceiverListener extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

       new MainActivity().setWifiText();

    }

}
