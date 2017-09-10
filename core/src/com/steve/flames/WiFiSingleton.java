package com.steve.flames;

/**
 * Created by Flames on 10/4/16.
 */
public class WiFiSingleton {

    private static volatile WiFiSingleton instance = null;
    public iWiFiDirect wifiManager;

    /* METHODS */
    public static WiFiSingleton getInstance() {
        if (instance == null) {
            synchronized (WiFiSingleton.class) {
                if (instance == null) {
                    instance = new WiFiSingleton();
                }
            }
        }
        return instance;
    }

    private WiFiSingleton() {
    }
}
