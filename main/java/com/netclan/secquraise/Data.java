package com.netclan.secquraise;

public class Data {

    String connectivity, batteryCharging, batteryCharge, location, timestamp;

    public Data() {
    }

    public Data(String connectivity, String batteryCharging, String batteryCharge, String location, String timestamp) {
        this.connectivity = connectivity;
        this.batteryCharging = batteryCharging;
        this.batteryCharge = batteryCharge;
        this.location = location;
        this.timestamp = timestamp;
    }

    public String getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(String connectivity) {
        this.connectivity = connectivity;
    }

    public String getBatteryCharging() {
        return batteryCharging;
    }

    public void setBatteryCharging(String batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public String getBatteryCharge() {
        return batteryCharge;
    }

    public void setBatteryCharge(String batteryCharge) {
        this.batteryCharge = batteryCharge;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
