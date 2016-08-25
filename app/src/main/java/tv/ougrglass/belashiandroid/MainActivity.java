package tv.ougrglass.belashiandroid;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public TextView mWiFiTextView;
    Thread mUDPListenerThread;
    Thread mUDPBroadcastThread;
    DatagramSocket mSocket;
    private OGObjectAdapter adapter;
    private ArrayList<OGBoxObject> mOGList = new ArrayList<OGBoxObject>();
    private boolean mListening = false;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;


    @Override
    protected void onCreate(Bundle savedInstanceState) { //Create the view

        super.onCreate(savedInstanceState); //call onCreate android method
        setContentView(R.layout.activity_main); //Set the content view to the main activity

        mWiFiTextView = (TextView) findViewById(R.id.wifiView); //set the wifi text to the text view
        setWifiText(); //Actually set the content of the text view

        startListen(); //Start listening to the UDP client

        initializeRecyclerView(); //Start our main recycler view

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); //Add drawer layout
        mDrawerList = (ListView) findViewById(R.id.left_drawer); //Get the comment list view

        ImageView hamburgerImage = (ImageView) findViewById(R.id.hamburger_menu_view); //Get the hamburger image

        hamburgerImage.setOnClickListener(new View.OnClickListener() { //Create an onclick listener for the hamburger image
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(findViewById(R.id.left_drawer)); //open the drawer when the image is clicked
            }
        });

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, getString(R.string.menu_option_left).split(";"))); //Set an adapter to monitor menu

        createSidebarListener(); //Create the sidebar for the listener


    }

    /**
     * Creates a map intent from the MainActivity Context and the MapActivity class
     * Starts the activity once it's created
     */
    private void openMapView(){
        Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(mapIntent);
    }

    /**
     * Creates the sidebar listener. Uses the mDrawerList variable to set an onItemClickListener
     */
    private void createSidebarListener(){
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                try {
                    TextView textView = (TextView) view; //Get the text view that was clicked

                    switch (textView.getText().toString().toLowerCase()) { //Get the text content from the view and pass it to a switch statement
                        case "map": //If the text is 'map'
                            openMapView(); //run the map view
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void initializeRecyclerView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.ogBoxView); //Set the recycler view variable

        //adapter = new MyCustomAdapter(this, Data.getData());
        //mRecyclerView.setAdapter(adapter);

        //mOGList = simulateOGBoxes(3); //Initizalize the OGList (simulation currently)

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                OGBoxObject ogBox = (OGBoxObject) view.getTag(); //get ogbox from the view

                Intent controllerIntent = new Intent(MainActivity.this, WebActivity.class); //create webview controller intent
                controllerIntent.putExtra("url", "http://" +
                        ogBox.getIPAddress().toString() +
                        ":9090/www/control/index.html"); //Put the url we want to open
                startActivity(controllerIntent); //start the activity
            }
        };

        adapter = new OGObjectAdapter(this, mOGList, onClickListener); //Create the adapter object with a list

        recyclerView.setAdapter(adapter); // Set the recycler view adapter to the adapter object

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); //Set the layout manager to GridLayout

    }


    /**
     * Use this to simulate og boxes
     * @param number the number of ogboxes to simulate
     * @return a list of of og boxes for simulation
     */
    public List<OGBoxObject> simulateOGBoxes(int number) {

        List<OGBoxObject> returnList = new ArrayList<>(); //make a list

        for (int i = 0; i < number; i++) { //loop

            OGBoxObject obj = null;
            try {
                obj = new OGBoxObject("OGName" + Integer.toString(i),
                        "OGLocation" + Integer.toString(i)
                        , i + ":" + i + ":" + i + ":" + i
                        , InetAddress.getByName("192.168.1." + i)); //get a numerical ip
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //Make an OGBoxObject for every iteration in the loop

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
        stopListen(); //STAWP EVERAYTHANG
        super.onPause(); //Call android's pause method
    }

    /**
     *
     * @return a new datagram socket (so we can have this in one place) or null if it's already connected
     */
    public DatagramSocket getSocketPort(){
        try {
            return new DatagramSocket(9091);
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Listen for the packets
     */
    public void startListen() {
        //If we're already listening, don't do anything
        if (mListening)
            return;

        try {
            mSocket = getSocketPort(); //Set the socket to our listen port
            mSocket.setBroadcast(true); //Set that the socket can broadcast
        } catch (SocketException e) {
            e.printStackTrace(); //Print an error
        } catch (NullPointerException e){
            //This means that the socket was already a thing.
        }

        createUDPThreads(); //Create the udp threads
        mListening = true;
        mUDPListenerThread.start(); //And start it
        mUDPBroadcastThread.start(); //And start it
    }

    public void stopListen() {
        if (!mListening) //If we aren't listening don't stop listening again
            return;
        mListening = false; //We aren't listening anymore
        if(mSocket != null) //If the socket isn't null, disconncet
            mSocket.disconnect();
        //Kill the threads?

    }

    /**
     * Notify the recycler view adapter that data has changed.
     */
    public void updateAdapter() {
        MainActivity.this.runOnUiThread(new Runnable() { //Run a ui thread method

            @Override
            public void run() {
                adapter.notifyDataSetChanged(); //Tell the adapter stuff has changed
            }

        });
    }

    public void createUDPThreads() { //Create the udp threads

        mUDPBroadcastThread = new Thread(new Runnable() { //New thread runnable

            @Override
            public void run() { //run

                try {
                    while (mListening) { //While we should be finding boxes

                        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();

                        byte[] messageBytes =
                                ("{'name': '"+myDevice.getName()+"'," +
                                        " 'mac': '"+wifiInfo.getMacAddress()+"'," +
                                        " 'type': 'phone'}").getBytes(); //Get a message
                        DatagramPacket packet = new DatagramPacket(messageBytes,
                                messageBytes.length,
                                InetAddress.getByName("255.255.255.255"),
                                9091); //Create the packet
                        try {
                            mSocket.send(packet); //Send the packet
                        } catch(NullPointerException e){
                            mSocket = getSocketPort(); //if the socket is null, replace it
                        } catch (Exception e) {
                            e.printStackTrace(); //And if something else write a different one
                        }

                        /**
                         * Do a check every 5 seconds that our OGs aren't offline
                         * This is unused because our ogboxes don't broadcast forever. They respond
                         * to our broadcasts as we send them. Therefore, if there is a box that
                         * hasn't responded it simply is because we haven't sent out a ping.
                         */
                        //removeOldOGsFromList();

                        Thread.sleep(5000); //Wait 5 seconds before another one
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

                    while (mListening) { //If we're listening

                        byte[] receiveBuffer = new byte[512]; //Get a byte buffer

                        DatagramPacket receivedPacket =
                                new DatagramPacket(receiveBuffer, receiveBuffer.length); //Create the packet object from the buffer
                        mSocket.receive(receivedPacket); //Receiver the packet
                        processReceivedPackets(receivedPacket); //Process the received packets

                    }

                } catch (NullPointerException e) {
                    if(mSocket == null)
                        try {
                            mSocket = getSocketPort(); //If the socket is null... you get the drill
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                } catch (IOException e) {
                   e.printStackTrace();
                }
            }

        });

    }

    /**
     * This methods takes the packets and performs json decoding and byte interpretation
     * like making it a string from a byte array or trimming off the empty bytes.
     * @param receivedPacket Send the packet you want to process into an OGBox
     */
    private void processReceivedPackets(DatagramPacket receivedPacket) {

        try {

            byte[] data = receivedPacket.getData(); //Get the data from the packet.
            byte[] choppedData = new byte[receivedPacket.getLength()]; //Make a byte array from the packet length

            //Copy the data starting at 0 to the choppedData array starting from 0 and ending at the packet length
            System.arraycopy(data, 0, choppedData, 0, receivedPacket.getLength());

            String jsonString = new String(choppedData, "UTF-8"); //Decode the bytes to a string in UTF-8
            JSONObject jsonObject = new JSONObject(jsonString); //Parse it to a json object

            try{
                if(jsonObject.getString("type").equalsIgnoreCase("phone")) //If it is a phone, ignore
                {
                    return;
                }
            } catch (JSONException e){
                //At this point in time there is no 'type' section in the OGBox's response data
            }

            OGBoxObject ogBoxObject = new OGBoxObject(
                    jsonObject.getString("name"),
                    jsonObject.getString("location"),
                    jsonObject.getString("mac"),
                    receivedPacket.getAddress()); //Decode the name, location, and mac address

            addObjectToOGList(ogBoxObject); //add the OGBoxObject to our list of objects

        } catch (JSONException | UnsupportedEncodingException e) {
            //JSON Exception occured
            e.printStackTrace();
        }

    }


    /**
     * Add an ogobject to the list of the ogobjects
     * @param ogBoxObject send an OGBoxObject to add to the list
     */
    private void addObjectToOGList(OGBoxObject ogBoxObject) {

        boolean inList = false; //This is for duplicate checking
        for (OGBoxObject obj : mOGList) {
            if (obj.getName().equalsIgnoreCase(ogBoxObject.getName())) { //If the name already exists in the list
                inList = true; //they are in a list
                obj.setUpdateTime(Calendar.getInstance().getTime().getTime()); //set the update time (not useful anymore)
                break; //We don't need to keep checking if it was found
            }
        }

        if (!inList) { //if it isn't in the list
            mOGList.add(ogBoxObject); //add it to the list
            updateAdapter(); //update
        }

    }


    /**
     * Checks if any of the OGObjects are too old (30 seconds) and remove them from the list.
     */
    private void removeOldOGsFromList() {

        long now = Calendar.getInstance().getTime().getTime();

        for (OGBoxObject obj : mOGList) {

            if (now - obj.getUpdateTime() >= (30 * 1000)) {

                mOGList.remove(obj);
                updateAdapter();

            }
        }
    }
}