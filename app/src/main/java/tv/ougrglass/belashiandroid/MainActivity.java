package tv.ougrglass.belashiandroid;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    protected void onCreate(Bundle savedInstanceState) { //Create the view
        super.onCreate(savedInstanceState); //call onCreate android method
        setContentView(R.layout.activity_main); //Set the content view to the main activity

        mWiFiTextView = (TextView) findViewById(R.id.wifiView); //set the wifi text to the text view
        setWifiText(); //Actually set the content of the text view

        startListen(); //Start listening to the UDP client
        try {
            mSocket = new DatagramSocket(9091); //Set the socket to our listen port
        } catch (SocketException e) {
            e.printStackTrace(); //Print an error
        }

        initializeRecyclerView(); //Start our main recycler view

    }

    private void initializeRecyclerView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.ogBoxView); //Set the recycler view variable

        //adapter = new MyCustomAdapter(this, Data.getData());
        //mRecyclerView.setAdapter(adapter);

        mOGList = simulateOGBoxes(3); //Initizalize the OGList (simulation currently)

        adapter = new OGObjectAdapter(this, mOGList); //Create the adapter object with a list

        recyclerView.setAdapter(adapter); // Set the recycler view adapter to the adapter object

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); //Set the layout manager to GridLayout

    }

    public List<OGObject> simulateOGBoxes(int number){

        List<OGObject> returnList = new ArrayList<>(); //make a list

        for(int i = 0; i < number; i++){ //loop

            OGObject obj = new OGObject("OGName" + Integer.toString(i),
                    "OGLocation" + Integer.toString(i), i+ ":"+i+":"+i+":"+i, "192.168.1." + i);
            //Make an OGObject for every iteration in the loop

            returnList.add(obj); //Add it to the list

        }

        return returnList; //Return the list

    }

    public void setWifiText() {

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); //Set the wifi manager

        WifiInfo info = wifiManager.getConnectionInfo(); // Get the info

        String ssid = info.getSSID().substring(1, info.getSSID().length()-1); //get the ssid

        String result; //Get read to build the result

        if(ssid.equalsIgnoreCase("unknown ssid")){ //If we don't know the ssid
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
        super.onPause();
    }

    public void startListen() {
        createUDPThread(); //Create the udp thread
        mUDPListenerThread.start(); //And start it
    }

    public void endListen() {
        shouldSocketListen = false;
    }

    public void updateAdapter(){
        MainActivity.this.runOnUiThread(new Runnable() { //Run a ui thread method

            @Override
            public void run() {
                adapter.notifyDataSetChanged(); //Tell the adapter stuff has changed
            }

        });
    }

    private void addObjectToOGList(OGObject ogObject){

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

    }

    private void removeOldOGsFromList(){

        long now = Calendar.getInstance().getTime().getTime();

        for(OGObject obj : mOGList){

            if(now - obj.getUpdateTime() >= (30 * 1000)){

                mOGList.remove(obj);
                updateAdapter();

            }
        }
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


            addObjectToOGList(ogObject);

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

}