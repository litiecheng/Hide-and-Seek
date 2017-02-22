package com.steve.flames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Flames on 10/4/16.
 */
public class WiFiActivity extends Activity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WiFiManager wm = new WiFiManager(this, new Handler());
        wm.getAdapter();
        WiFiSingleton.getInstance().wifiManager = wm;
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(WiFiActivity.this, AndroidLauncher.class);
        WiFiActivity.this.startActivity(intent);
    }
}
