package com.steve.flames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.widget.Toast;

import com.steve.flames.network.ChatClient;
import com.steve.flames.network.ChatServer;
import com.steve.flames.screens.MenuScreen;


public class WiFiDirectManager implements iWiFiDirect {

    private AndroidLauncher activity;
    private ArrayList<Device> availableDevices = new ArrayList<>();
    private ArrayList<Device> connectedDevices = new ArrayList<>();

    private Device currentDevice;
    private String currentConnectionAddress;

    private AlertDialog alertDialog;
    private static int alertDialogSec = 10;
    private Timer timer;

    private boolean connected = false;
    private boolean groupOwner = false;
    private ChatServer chatServer;
    private ChatClient chatClient;


    WiFiDirectManager(AndroidLauncher activity) {
        this.activity = activity;
        alertDialogSec = 10;
    }

    @Override
    public void enableP2P() {

    }

    @Override
    public void discoverDevices() {
        if (!activity.isWifiP2pEnabled()) {
            toast("Please enable WiFiDirect");
        }
        else {
            activity.manager.discoverPeers(activity.channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    toast("Discovery Initiated");
                }

                @Override
                public void onFailure(int reasonCode) {
                    toast("Discovery Failed : " + reasonCode);
                }
            });
        }
    }

    @Override
    public void connectTo(final String address) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        dismissDialog();

        activity.manager.connect(activity.channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //success logic
                currentConnectionAddress = address;
                createDialog("Please wait", "Connecting to " + address, false);
                //Toast.makeText(activity, "Connecting to "+ address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                toast("Connect failed. Please try again (code: "+reason+")");
                dismissDialog();
            }
        });
    }


    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void createGroup() {
        activity.manager.createGroup(activity.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //success logic
                toast("Group created");
            }

            @Override
            public void onFailure(int reason) {
                toast("Group creation failed(code: "+reason+")");
                activity.getGame().dispose();
                activity.getGame().setScreen(new MenuScreen(activity.getGame()));
            }
        });
    }

    @Override
    public void disconnect(){
        activity.manager.removeGroup(activity.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                toast("Group reset");
                connected = false;
            }

            @Override
            public void onFailure(int reasonCode) {
                //Toast.makeText(activity, "Reset failed. Reason :" + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void stopDiscovering() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { //api 16
            // Do something for lollipop and above versions
            activity.manager.stopPeerDiscovery(activity.channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    System.out.println("Stop Discovery success");
                }

                @Override
                public void onFailure(int i) {
                    System.out.println("Stop Discovery failed");
                }
            });
        }
    }

    @Override
    public void sendMessageToServer(String msg) {
        chatClient.sendMessageToServer(msg);

    }

    @Override
    public void sendMessageToAll(String msg) {
        if(chatServer != null)
            chatServer.sendMessageToAll(msg);
    }

    @Override
    public String receiveMessage() {
        if(connected) {
            if (chatServer != null)
                return chatServer.receiveMessage();
            else {
                if(chatClient != null)
                    return chatClient.receiveMessage();
            }
        }
        return null;
    }

    public ArrayList<Device> getAvailableDevices() {
        return availableDevices;
    }

    @Override
    public ArrayList<Device> getConnectedDevices() {
        return connectedDevices;
    }

    @Override
    public Device getCurrentDevice() {
        return currentDevice;
    }

    @Override
    public void setCurrentDevice(Device device) {
        currentDevice = device;
    }

    @Override
    public void toast(final String string) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDialog(final String title, final String msg, final boolean cancelable) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity.getContext());
                alertDialogBuilder.setTitle(title);
                alertDialogBuilder.setMessage(msg).setCancelable(cancelable);
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                //cancel after 10 secs
                timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        alertDialogSec --;
                        if(alertDialogSec <= 0) {
                            dismissDialog();
                            activity.manager.cancelConnect(activity.channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    toast("Aborted connection");
                                }

                                @Override
                                public void onFailure(int i) {
                                    toast("Failed to abort connection");
                                }
                            });
                        }
                    }
                }, 0, 1000);
            }
        });
    }

    /**
     * reset data
     */
    @Override
    public void resetData() {
        availableDevices.clear();
        connectedDevices.clear();
        if(chatClient != null) {
            try {
                chatClient.getDataSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initServer() {
        if(chatServer == null)
            chatServer = new ChatServer(this); //initialize socket server
    }

    @Override
    public void initClient(String s) {
        //if(chatClient == null)
            chatClient = new ChatClient(s, activity.getGame());
    }

    @Override
    public boolean hasClientInitFinish() {
        if(chatClient != null)
            return chatClient.isFinish();
        return false;
    }

    @Override
    public boolean isGroupOwner() {
        return groupOwner;
    }

    void dismissDialog() {
        if(alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            timer.cancel();
            alertDialogSec = 10;
        }
    }

    String getCurrentConnectionAddress() {
        return currentConnectionAddress;
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setGroupOwner(boolean groupOwner) {
        this.groupOwner = groupOwner;
    }
}