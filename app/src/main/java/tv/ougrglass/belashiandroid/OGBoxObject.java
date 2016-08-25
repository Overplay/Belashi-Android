package tv.ougrglass.belashiandroid;

import java.net.InetAddress;
import java.util.Calendar;

/**
 * Created by logansaso on 7/19/16.
 */
public class OGBoxObject {

    private String macAddress;
    private String name;
    private String location;
    private InetAddress ipAddress;
    private long updateTime;

    /**
     * OGBoxObject Constructor
     * @param name The name of the OGBoxObject
     * @param location The location the OGBoxObject provides
     * @param mac The MacAddress of the OGBoxObject
     * @param ipAddress The IPAddress of the OGBoxObject
     */
    public OGBoxObject(String name, String location, String mac, InetAddress ipAddress){

        if(mac != null){
            this.macAddress = mac; //If not null set mac address
        } else {
            this.macAddress = "00:00:00:00:00:00";
        }

        this.name = name;
        this.location = location;
        this.ipAddress = ipAddress;
        this.updateTime = Calendar.getInstance().getTime().getTime();

    }


    public String toString(){
        return String.format("Name: %s, Mac: %s, Location: %s, IP: %s, Time: %s",
                name, macAddress, location, ipAddress, updateTime);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public InetAddress getIPAddress() {
        return ipAddress;
    }

    public void setIPAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
