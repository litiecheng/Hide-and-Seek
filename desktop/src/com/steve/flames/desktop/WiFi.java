package com.steve.flames.desktop;

import com.steve.flames.iWiFi;

import java.util.ArrayList;

/**
 * Created by Flames on 10/4/16.
 */
public class WiFi implements iWiFi {
    @Override
    public void enableBluetooth() {

    }

    @Override
    public void enableDiscoveribility() {

    }

    @Override
    public void discoverDevices() {

    }

    @Override
    public void stopDiscovering() {

    }

    @Override
    public boolean startServer() {
        return false;
    }

    @Override
    public void connectToServer() {

    }

    @Override
    public String getTest() {
        return null;
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
    public boolean canConnect() {
        return false;
    }

    @Override
    public void switchToNextDevice() {

    }

    @Override
    public void switchToPrevDevice() {

    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public String getDevice() {
        return null;
    }

    @Override
    public void setConnectedDevice(String device) {

    }

    @Override
    public ArrayList<String> getDevices() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isFirst() {
        return false;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean isDiscovering() {
        return false;
    }

    @Override
    public boolean isEnabled() {
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
