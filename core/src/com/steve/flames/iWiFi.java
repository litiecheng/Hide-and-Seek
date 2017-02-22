package com.steve.flames;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Flames on 10/4/16.
 */
public interface iWiFi {

    public void enableBluetooth();
    public void enableDiscoveribility();
    public void discoverDevices();
    public void stopDiscovering();
    public boolean startServer();
    public void connectToServer();
    public String getTest();
    public void sendMessage(String message);
    public String getMessage();
    public boolean isConnected();
    public boolean canConnect();
    public void switchToNextDevice();
    public void switchToPrevDevice();
    public int getState(); //fkom CHANGE
    public String getDevice();
    public void setConnectedDevice(String device);
    public ArrayList<String> getDevices(); //fkom CHANGE
    public void stop();
    public boolean isFirst();
    public boolean isLast();
    public boolean isDiscovering();
    public boolean isEnabled();
    public ArrayList<String> getConnectedDevicesNames();
    public ArrayList<String> getPairedDevices();
    public String getMyDeviceName();
    //public void pairDevice(String device);
}