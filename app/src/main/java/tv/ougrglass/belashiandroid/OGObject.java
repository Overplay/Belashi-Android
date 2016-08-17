package tv.ougrglass.belashiandroid;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by logansaso on 7/19/16.
 */
public class OGObject {

    private String macAddress;
    private String name;
    private String location;
    private InetAddress ipAddress;
    private long updateTime;

    public OGObject(String name, String location, String mac, InetAddress ipAddress){

        this.macAddress = "00:00:00:00:00:00";

        if(mac != null){
            this.macAddress = mac;
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
