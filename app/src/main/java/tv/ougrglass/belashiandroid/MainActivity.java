package tv.ougrglass.belashiandroid;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

//import tv.ougrglass.belashiandroid.OGObject;

public class MainActivity extends AppCompatActivity {

    public TextView mWiFiTextView;
    private boolean TEMP_IN_SIMULATOR = false;
    Thread mUDPListenerThread;
    boolean shouldSocketListen;
    DatagramSocket mSocket;
    HashMap<String, OGObject> mOGBoxes = new HashMap<>();
    private OGObjectAdapter adapter ;
    private List<OGObject> mOGList ;


    public TextView getTextView() {
        return mWiFiTextView;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWiFiTextView = (TextView) findViewById(R.id.wifiView);
         setWifiText();

        createUDPThread();
        mUDPListenerThread.start();
        try {
            mSocket = new DatagramSocket(9091);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        initializeRecyclerView();


        addOGToTable();

    }

    private void initializeRecyclerView() {

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.ogBoxView);

        //adapter = new MyCustomAdapter(this, Data.getData());
        //mRecyclerView.setAdapter(adapter);

        mOGList = simulateOGBoxes(7);

        adapter = new OGObjectAdapter(this, mOGList);

        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

    }

    public List<OGObject> simulateOGBoxes(int number){

        List<OGObject> returnList = new ArrayList<>();

        for(int i = 0; i < number; i++){

            OGObject obj = new OGObject("OGName" + Integer.toString(i),
                    "OGLocation" + Integer.toString(i), i+ ":"+i+":"+i+":"+i, "192.168.1." + i);

            returnList.add(obj);

        }

        return returnList;

    }

    public void setWifiText() {


        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifiManager.getConnectionInfo();

        String ssid = info.getSSID().substring(1, info.getSSID().length()-1);

        String result;

        if(ssid.equalsIgnoreCase("unknown ssid")){
            result = "Wi-Fi Network Not Found";
        } else {
            result = String.format("Wi-Fi Network: %s", ssid);
        }

        mWiFiTextView.setText(result);

    }

    @Override
    public void onResume() {
        super.onResume();
        startListen();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void startListen() {
        createUDPThread();
        mUDPListenerThread.start();
    }

    public void endListen() {
        shouldSocketListen = false;
    }

    public void updateAdapter(){
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }

        });
    }

    private void processReceivedPackets(DatagramPacket receivedPacket) {

        try {

            byte[] data = receivedPacket.getData();
            byte[] choppedData = new byte[receivedPacket.getLength()];
            System.arraycopy(data, 0, choppedData, 0, receivedPacket.getLength());

            String jsonString = new String(choppedData, "UTF-8");
            JSONObject jsonObject = new JSONObject(jsonString);

            OGObject ogObject = new OGObject(
                    jsonObject.getString("name"),
                    jsonObject.getString("location"),
                    jsonObject.getString("mac"),
                    receivedPacket.getAddress().toString());

            boolean inList = false;
            for(OGObject obj : mOGList){
                if(obj.getName().equalsIgnoreCase(ogObject.getName())){
                    inList = true;
                    obj.setUpdateTime(Calendar.getInstance().getTime().getTime());
                    break;
                }
            }

            if(!inList){
                mOGList.add(ogObject);
                updateAdapter();
            }

//            long now = Calendar.getInstance().getTime().getTime();
//
//            for(OGObject obj : mOGList){
//
//                if(now - obj.getUpdateTime() >= (30 * 1000)){
//
//                    //remove box
//
//                    mOGList.remove(obj);
//                    updateAdapter();
//
//                }
//
//            }

            Log.d("Servers Array Print", mOGBoxes.keySet().toString());

        } catch (JSONException | UnsupportedEncodingException e) {
            //JSON Exception occured
            e.printStackTrace();
        }

    }

    public void createUDPThread() {

        shouldSocketListen = true;

        mUDPListenerThread = new Thread(new Runnable() {


            @Override
            public void run() {

                try {
                    mSocket = new DatagramSocket(9091);
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                try {

                    while (shouldSocketListen) {

                        byte[] receiveBuffer = new byte[256];

                        DatagramPacket receivedPacket =
                                new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        mSocket.receive(receivedPacket);
                        processReceivedPackets(receivedPacket);

                    }

                } catch (NullPointerException e) {
                    Log.e("NullPointer", e.toString());
                } catch (UnsupportedEncodingException e) {
                    Log.e("Encoding Exception", e.toString());
                } catch (IOException e) {
                    Log.e("IO Exception", e.toString());
                }

                mSocket.disconnect();

            }

        });

    }

    public void addOGToTable() {

    }

}