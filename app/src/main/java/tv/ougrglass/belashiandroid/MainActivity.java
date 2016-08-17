package tv.ougrglass.belashiandroid;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

//import tv.ougrglass.belashiandroid.OGObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public TextView mWiFiTextView;
    Thread mUDPListenerThread;
    Thread mUDPBroadcastThread;
    DatagramSocket mSocket;
    HashMap<String, OGObject> mOGBoxes = new HashMap<>();
    private OGObjectAdapter adapter;
    private List<OGObject> mOGList = new ArrayList<OGObject>();
    private boolean mListening = false;

    public TextView getTextView() {
        return mWiFiTextView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { //Create the view
        super.onCreate(savedInstanceState); //call onCreate android method
        setContentView(R.layout.activity_main); //Set the content view to the main activity

        mWiFiTextView = (TextView) findViewById(R.id.wifiView); //set the wifi text to the text view
        setWifiText(); //Actually set the content of the text view

        startListen(); //Start listening to the UDP client

        initializeRecyclerView(); //Start our main recycler view

    }

    private void initializeRecyclerView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.ogBoxView); //Set the recycler view variable

        //adapter = new MyCustomAdapter(this, Data.getData());
        //mRecyclerView.setAdapter(adapter);

        //mOGList = simulateOGBoxes(3); //Initizalize the OGList (simulation currently)

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked");
                OGObject ogBox = (OGObject) view.getTag();

                Intent controllerIntent = new Intent(MainActivity.this, ControllerSelectionActivity.class);
                controllerIntent.putExtra("ogIP", ogBox.getIPAddress().toString());
                startActivity(controllerIntent);

            }
        };

        adapter = new OGObjectAdapter(this, mOGList, onClickListener); //Create the adapter object with a list

        recyclerView.setAdapter(adapter); // Set the recycler view adapter to the adapter object

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); //Set the layout manager to GridLayout

    }


    public List<OGObject> simulateOGBoxes(int number) {

        List<OGObject> returnList = new ArrayList<>(); //make a list

        for (int i = 0; i < number; i++) { //loop

            OGObject obj = null;
            try {
                obj = new OGObject("OGName" + Integer.toString(i),
                        "OGLocation" + Integer.toString(i), i + ":" + i + ":" + i + ":" + i, InetAddress.getByName("192.168.1." + i));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //Make an OGObject for every iteration in the loop

            returnList.add(obj); //Add it to the list

        }

        return returnList; //Return the list

    }

    public void setWifiText() {

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); //Set the wifi manager

        WifiInfo info = wifiManager.getConnectionInfo(); // Get the info

        String ssid = info.getSSID().substring(1, info.getSSID().length() - 1); //get the ssid

        String result; //Get read to build the result

        if (ssid.equalsIgnoreCase("unknown ssid")) { //If we don't know the ssid
            result = "Wi-Fi Network Not Found"; //Set it to something more pretty
        } else {
            result = String.format("Wi-Fi Network: %s", ssid); //Or set the text
        }

        mWiFiTextView.setText(result); //Set the wifi text

    }

    @Override
    public void onResume() {
        super.onResume(); //Android resume
        startListen(); //Resume the listening again
    }

    @Override
    public void onPause() {
        stopListen();
        super.onPause();
    }

    public void startListen() {
        if (mListening)
            return;

        try {
            mSocket = new DatagramSocket(9091); //Set the socket to our listen port
            mSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace(); //Print an error
        }

        createUDPThread(); //Create the udp thread
        mListening = true;
        mUDPListenerThread.start(); //And start it
        mUDPBroadcastThread.start();
    }

    public void stopListen() {
        if (!mListening)
            return;
        mListening = false;

        //Kill the threads?

    }

    public void updateAdapter() {
        MainActivity.this.runOnUiThread(new Runnable() { //Run a ui thread method

            @Override
            public void run() {
                adapter.notifyDataSetChanged(); //Tell the adapter stuff has changed
            }

        });
    }

    public void createUDPThread() {

        mUDPBroadcastThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    while (mListening) {
                        byte[] messageBytes =
                                ("{'name': 'Android Testing'," +
                                        " 'location': 'Probably near Logan'," +
                                        " 'mac': 'and cheese'," +
                                        " 'type': 'phone'}").getBytes();
                        DatagramPacket packet = new DatagramPacket(messageBytes,
                                messageBytes.length,
                                InetAddress.getByName("255.255.255.255"),
                                9091);
                        try {
                            mSocket.send(packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

        mUDPListenerThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    while (mListening) {

                        byte[] receiveBuffer = new byte[256];

                        DatagramPacket receivedPacket =
                                new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        mSocket.receive(receivedPacket);
                        processReceivedPackets(receivedPacket);

                    }

                } catch (NullPointerException e) {
                    Log.e("NullPointer", e.toString());
                    mSocket.disconnect();
                } catch (UnsupportedEncodingException e) {
                    Log.e("Encoding Exception", e.toString());
                    mSocket.disconnect();
                } catch (IOException e) {
                    Log.e("IO Exception", e.toString());
                    mSocket.disconnect();
                }

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

            try{
                if(jsonObject.getString("type").equalsIgnoreCase("phone"))
                {
                    return;
                }
            } catch (JSONException e){
                //Type is an OG box, continue;
            }

            OGObject ogObject = new OGObject(
                    jsonObject.getString("name"),
                    jsonObject.getString("location"),
                    jsonObject.getString("mac"),
                    receivedPacket.getAddress());

            addObjectToOGList(ogObject);

        } catch (JSONException | UnsupportedEncodingException e) {
            //JSON Exception occured
            e.printStackTrace();
        }

    }

    private void addObjectToOGList(OGObject ogObject) {

        boolean inList = false;
        for (OGObject obj : mOGList) {
            if (obj.getName().equalsIgnoreCase(ogObject.getName())) {
                inList = true;
                obj.setUpdateTime(Calendar.getInstance().getTime().getTime());
                break;
            }
        }

        if (!inList) {
            mOGList.add(ogObject);
            updateAdapter();
        }

    }

    private void removeOldOGsFromList() {

        long now = Calendar.getInstance().getTime().getTime();

        for (OGObject obj : mOGList) {

            if (now - obj.getUpdateTime() >= (30 * 1000)) {

                mOGList.remove(obj);
                updateAdapter();

            }
        }
    }
}