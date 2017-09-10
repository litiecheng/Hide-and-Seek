package com.steve.flames;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.steve.flames.network.ChatServer;

import java.io.IOException;
import java.net.Socket;


public class AndroidLauncher extends AndroidApplication {

	private final IntentFilter mIntentFilter = new IntentFilter();
	private boolean isWifiP2pEnabled = false;

	WifiP2pManager manager;
	WifiP2pManager.Channel channel;
	private BroadcastReceiver receiver;

	static final String TAG = "AndroidLauncherActivity";

	private WiFiDirectManager wfm;
	private HaSGame game;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		//mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);

		wfm = new WiFiDirectManager(this);
		game = new HaSGame(wfm);
		initialize(game, config);
	}

	@Override
	protected void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, mIntentFilter);
        //wfm.discoverDevices(); //kako
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
		wfm.dismissDialog();
        //wfm.stopDiscovering();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		wfm.disconnect();
		for(Socket socket: ChatServer.dataSocketsHMap.values()) {
			Log.e("EEEEE", "EKANA RESET TA DATA SOCKETS STO ONDESTROY()");
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Remove all peers and clear all fields. This is called on
	 * BroadcastReceiver receiving a state change event.
	 */
	public void resetData() {
		wfm.resetData();
		//wfm.discoverDevices();
	}



	public boolean isWifiP2pEnabled() {
		return isWifiP2pEnabled;
	}

	public void setWifiP2pEnabled(boolean wifiP2pEnabled) {
		isWifiP2pEnabled = wifiP2pEnabled;
	}

	public WiFiDirectManager getWfm() {
		return wfm;
	}

	public HaSGame getGame() {
		return game;
	}
}
