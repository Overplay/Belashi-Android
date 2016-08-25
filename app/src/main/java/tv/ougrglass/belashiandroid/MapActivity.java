package tv.ougrglass.belashiandroid;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * BelashiAndroid Created by logansaso on 8/18/16.
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private String TAG = "MapActivityDebug";

    private GoogleMap mMap;
    private String mJsonString;
    private ArrayList mRestaurantList = new ArrayList<RestaurantObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment); //Get the map fragment
        mapFragment.getMapAsync(this); //get the map async
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap; //get the map to the googlemap
        LocationManager locationManager =
                (LocationManager) getSystemService(MapActivity.LOCATION_SERVICE); //get a location manager
        Criteria locationCriteria = new Criteria(); //get location criteria

        mMap.setMyLocationEnabled(true); //set show my location enabled to true

        Location location = locationManager.getLastKnownLocation(
                locationManager.getBestProvider(locationCriteria, false)); //get my location object

        LatLng myLocation =
                new LatLng(location.getLatitude(), location.getLongitude()); //get my LatLong

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15)); //move camera to me

        placeSurroundingOurglassMarkers(myLocation); //place the surrounding markers for ourglass locations

    }

    /**
     * Place surrounding markers for ourglass enabled locations
     * @param userLocation Pass the user location
     */
    public void placeSurroundingOurglassMarkers(LatLng userLocation) {

        //This is written this way because in the future there can be a radius parameter to only load near ones


        /*PSEUDO CODE BEGIN
        * Get the lattitude/long of the current phone.
        * Send an API call to our webserver with the relevant information (location?)
        * Get a list of boxes and make sure none of them have duplicate data.
        * Add all of the locations to the map*/

        getJSON(); //Do all the work

    }

    /**
     * Download the json of objects from our server
     */
    public void getJSON() {

        Thread getJsonThread = new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection c = null;
                try {
                    URL u = new URL("http://104.131.145.36/venue/getMobileView"); //make url object
                    c = (HttpURLConnection) u.openConnection(); //open the httpurlconnection
                    c.connect(); //connect the HttpURLConnection

                    int status = c.getResponseCode(); //get the status code
                    switch (status) { //switch based on response code
                        case 200:
                            BufferedReader br =
                                    new BufferedReader(new InputStreamReader(c.getInputStream()));
                            StringBuilder sb = new StringBuilder(); //Make a StringBuilder
                            String line; //make the line
                            while ((line = br.readLine()) != null) { //as long as the line is not null
                                sb.append(line); //add it to the end
                                sb.append("\n"); //new line
                            }
                            br.close(); //close the bufferedreader
                            mJsonString = sb.toString(); //jsonString is the stringbuilder result

                            MapActivity.this.runOnUiThread(new Runnable() { //run on the ui thread
                                @Override
                                public void run() {
                                    try {
                                        addMarkersToMap(mJsonString); //add the markers to the map
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        try {
                            c.disconnect();
                        } catch (Exception e) {
                            //Disconnect
                        }
                    }
                }
            }

        });

        getJsonThread.start(); //start the thread

    }

    /**
     * Decode the json into LatLng and add the markers to our map
     * @param jsonString Pass the json of objects we downloaded in getJSON
     * @throws JSONException
     */
    public void addMarkersToMap(String jsonString) throws JSONException {

        JSONArray restarauntList = new JSONArray(jsonString);

        for (int i = 0; i < restarauntList.length(); i++) {

            try {

                String name = new JSONObject(restarauntList.get(i).toString()).getString("name");

                LatLng latlng = null;

                try {

                    JSONObject geolocation = new JSONObject(new JSONObject(restarauntList.get(i).toString())
                            .getString("geolocation"));

                    String latitude = geolocation.getString("latitude");
                    String longitude = geolocation.getString("longitude");
                    latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                } catch (Exception e) {

                    JSONObject jsonAddress = new JSONObject(new JSONObject(restarauntList.get(i).toString())
                            .getString("address"));

                    String address =
                            jsonAddress.getString("street") + " " +
                                    jsonAddress.getString("city") + " " +
                                    jsonAddress.getString("state");

                    latlng = getLocationFromAddress(address);

//                    if (latlng == null)
//                        return;



                }

                Log.d(TAG, "------------------------");
                Log.d(TAG, name);
                Log.d(TAG, latlng.toString());
                Log.d(TAG, "------------------------");

                RestaurantObject restaurantObject = new RestaurantObject(name, latlng);

                mMap.addMarker(
                        new MarkerOptions().position(restaurantObject.getCoords())
                                .title(restaurantObject.getName()));

                mRestaurantList.add(restaurantObject);

//                LatLng latlng = new LatLng(
//                        new JSONObject(new JSONObject(restarauntList.get(i))
//                        .getJSONObject("geolocation")).getString("latitude"),
//                        new JSONObject(new JSONObject(restarauntList.get(i))
//                        .getJSONObject("geolocation")).getString("longitude"));

            } catch (Exception e) {
                e.printStackTrace();
            }

//            System.out.println(restarauntList.get(i));

        }

    }

    public LatLng getLocationFromAddress(String streetAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng returnMe = null;

        try {
            address = coder.getFromLocationName(streetAddress, 20);
            if (address.size() == 0) {
                return null;
            }

            Address location = address.get(0);

            Log.d(TAG, location.toString());
            Log.d(TAG, String.valueOf(location.getLatitude()));
            Log.d(TAG, String.valueOf(location.getLongitude()));

            returnMe = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.d(TAG + "Address", returnMe.toString());
        return returnMe;
    }


}