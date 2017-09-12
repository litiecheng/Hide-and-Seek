package com.steve.flames;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


/**
 * Created by Flames on 7/9/2017.
 */

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private AndroidLauncher activity;

    private WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            //if(!activity.getWfm().getAvailableDevices().equals(wifiP2pDeviceList.getDeviceList())) {
                activity.getWfm().getAvailableDevices().clear();
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList())
                    activity.getWfm().getAvailableDevices().add(new Device(device.deviceName, device.deviceAddress));
            //}
        }
    };

    private WifiP2pManager.ConnectionInfoListener infoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if(wifiP2pInfo.isGroupOwner) {
                activity.getWfm().setGroupOwner(true);
                activity.getWfm().initServer();
                activity.getWfm().setConnected(true);
            }
            else {
                activity.getWfm().setGroupOwner(false);
                activity.getWfm().initClient(wifiP2pInfo.groupOwnerAddress.toString());
            }
        }
    };

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, AndroidLauncher activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setWifiP2pEnabled(true);
            } else {
                activity.setWifiP2pEnabled(false);
                activity.resetData();
            }
            Log.d(AndroidLauncher.TAG, "P2P state changed - " + state);

        } /*else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                System.out.println("DISCOVERY STOPPED");
            }
            else {
                System.out.println("DISCOVERY STARTED");
            }
        } */else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, myPeerListListener);
            }
            Log.d(AndroidLauncher.TAG, "P2P peers changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                for(Device device: activity.getWfm().getAvailableDevices()) {
                    if(device.getAddress().equals(activity.getWfm().getCurrentConnectionAddress())) {
                        if(!activity.getWfm().getConnectedDevices().contains(device)) {
                            activity.getWfm().getConnectedDevices().add(device);
                            activity.getWfm().getAvailableDevices().remove(device);
                        }
                    }
                }
                activity.getWfm().setConnected(true);
                activity.getWfm().dismissDialog();
                manager.requestConnectionInfo(activity.channel, infoListener);
            } else {
                // It's a disconnect
                if(activity.getWfm().getConnectedDevices().size()==0)
                    activity.getWfm().setConnected(false);
                activity.resetData();
            }
            Log.d(AndroidLauncher.TAG, "Connection changed: "+ networkInfo.toString());

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            activity.getWfm().setCurrentDevice(new Device(device.deviceName, device.deviceAddress));
        }
    }

}


