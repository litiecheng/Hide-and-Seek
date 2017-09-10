package com.steve.flames;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Flames on 10/4/16.
 */
public interface iWiFiDirect {

    void enableP2P();
    void discoverDevices();
    void createGroup();
    void connectTo(String address);
    void disconnect();
    void stopDiscovering();
    void sendMessageToServer(String message);
    void sendMessageToAll(String message);
    String receiveMessage();
    boolean isConnected();
    void setConnected(boolean b);
    ArrayList<Device> getAvailableDevices();
    ArrayList<Device> getConnectedDevices();
    void resetData();
    boolean hasClientInitFinish();
    boolean isGroupOwner();

    void initServer();
    void initClient(String s);

    Device getCurrentDevice();
    void setCurrentDevice(Device device);
    void toast(String string);
}