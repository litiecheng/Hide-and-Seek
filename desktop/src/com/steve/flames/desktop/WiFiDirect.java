package com.steve.flames.desktop;

import com.steve.flames.iWiFiDirect;

import java.util.ArrayList;

/**
 * Created by Flames on 10/4/16.
 */
public class WiFiDirect implements iWiFiDirect {


    @Override
    public void enableP2P() {

    }

    @Override
    public void discoverDevices() {

    }

    @Override
    public void stopDiscovering() {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public ArrayList<String> getConnectedDevicesNames() {
        return null;
    }

    @Override
    public ArrayList<String> getPairedDevices() {
        return null;
    }

    @Override
    public String getMyDeviceName() {
        return null;
    }
}
