package tv.ourglass.belashiandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import android.os.Bundle;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

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

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Criteria mLocationCriteria;
    private Location mMyLocation;
    private String mJsonString;
    private ArrayList mRestarauntList = new ArrayList<RestarauntObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationManager = (LocationManager) getSystemService(MapActivity.LOCATION_SERVICE);
        mLocationCriteria = new Criteria();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);

        Location location = mLocationManager.getLastKnownLocation(
                mLocationManager.getBestProvider(mLocationCriteria, false));


        LatLng mMyLocation = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng TEST_LOCATION_MARKER_VARIABLE = new LatLng(37.2973150,-121.8993740);

        mMap.addMarker(new MarkerOptions().position(TEST_LOCATION_MARKER_VARIABLE).title("My house in the middle of the street."));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TEST_LOCATION_MARKER_VARIABLE, 15));

        placeSurroundingOurglassMarkers(mMyLocation);

    }

    public void placeSurroundingOurglassMarkers(LatLng userLocation){

        /*PSEUDO CODE BEGIN
        * Get the lattitude/long of the current phone.
        * Send an API call to our webserver with the relevant information (location?)
        * Get a list of boxes and make sure none of them have duplicate data.
        * Add all of the locations to the map*/

        getJSON();

    }

    public void addMarkersToMap(String jsonString) throws JSONException {


        JSONArray restarauntList = new JSONArray(jsonString);

        for(int i = 0; i < restarauntList.length(); i++){

            try {

                String name = new JSONObject(restarauntList.get(i).toString()).getString("name");

                LatLng latlng = null;

                try {

                    JSONObject geolocation = new JSONObject(new JSONObject(restarauntList.get(i).toString())
                            .getString("geolocation"));

                    String latitude = geolocation.getString("latitude");
                    String longitude = geolocation.getString("longitude");
                    latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                } catch (JSONException e) {

                    JSONObject jsonAddress = new JSONObject(new JSONObject(restarauntList.get(i).toString())
                            .getString("address"));

                    String address =
                            jsonAddress.getString("street") + " " +
                            jsonAddress.getString("city") + " " +
                            jsonAddress.getString("state");

                    latlng = getLocationFromAddress(address);

                    System.out.println(latlng);

                }

                RestarauntObject restarauntObject = new RestarauntObject(name, latlng);
                mMap.addMarker(
                        new MarkerOptions().position(restarauntObject.getCoords())
                                .title(restarauntObject.getName()));


                mRestarauntList.add(restarauntObject);



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

    public LatLng getLocationFromAddress(String streetAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;

        try {
            address = coder.getFromLocationName(streetAddress, 20);
            if(address == null){
                return null;
            }

            Address location = address.get(0);
            return new LatLng(location.getLatitude(),location.getLongitude());
        } catch (Exception e) {
            //Nothin
        }
        return null;
    }

    public void getJSON() {

        Thread getJsonThread = new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection c = null;
                try {
                    URL u = new URL("http://104.131.145.36/api/v1/venue");
                    c = (HttpURLConnection) u.openConnection();
                    c.connect();

                    int status = c.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201:
                            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                                sb.append("\n");
                            }
                            br.close();
                            mJsonString = sb.toString();

                            MapActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        addMarkersToMap(mJsonString);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null){
                        try{
                            c.disconnect();
                        } catch (Exception e){
                            //Disconnect
                        }
                    }
                }
            }

        });

        getJsonThread.start();

    }

}