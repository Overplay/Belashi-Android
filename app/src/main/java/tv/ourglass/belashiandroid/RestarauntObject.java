package tv.ourglass.belashiandroid;

import com.google.android.gms.maps.model.LatLng;

/**
 * BelashiAndroid Created by logansaso on 8/23/16.
 */
public class RestarauntObject {

    private String name;
    private LatLng coords;

    public RestarauntObject(String name, double latitude, double longitude){

        this.name = name;
        this.coords = new LatLng(latitude, longitude);

    }

    public RestarauntObject(String name, LatLng coords){

        this.name = name;
        this.coords = coords;

    }

    public RestarauntObject(String name, String latitude, String longitude){

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
