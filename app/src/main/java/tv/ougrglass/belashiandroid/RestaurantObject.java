package tv.ougrglass.belashiandroid;

import com.google.android.gms.maps.model.LatLng;

/**
 * BelashiAndroid Created by logansaso on 8/23/16.
 */
public class RestaurantObject {

    private String name;
    private LatLng coords;

    public RestaurantObject(String name, double latitude, double longitude){

        this.name = name;
        this.coords = new LatLng(latitude, longitude);

    }

    public RestaurantObject(String name, LatLng coords){

        this.name = name;
        this.coords = coords;

    }

    public RestaurantObject(String name, String latitude, String longitude){

        this.name = name;
        this.coords = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

    }

    public String getName(){
        return this.name;
    }

    public LatLng getCoords(){
        return this.coords;
    }

}
