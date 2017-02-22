package com.steve.flames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WiFiManager implements iWiFi {

    public LinkedList<BluetoothDevice> devices;
    private BluetoothDevice connectedDevice = null;
    private Set<BluetoothDevice> connectedDevices;
    private ArrayList<String> connectedDevicesNames  = new ArrayList<>();
    private int deviceIndex = 0;
    private Activity currentActivity;
    private final Handler mHandler;
    private int mState;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public boolean isConnected = false;
    public boolean canConnect = true;
    private String temp[];

    public String message;
    public boolean messageTaken = true;

    public BluetoothAdapter bta = null;


    //private Set<BluetoothDevice> pairedDevices;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                //devicesNames.add(device.getName());
            }
        }
    };

    public boolean isConnected() {
        return isConnected;
    }

    public String getMessage() {
        if (!messageTaken) {
            messageTaken = true;
            //if (message != null)
                //message = message.substring(0, 2);
            if(message!=null) {
                temp = message.split(" ");
                message = temp[0];
            }
            return message;
        }

        return null;
    }

    public String getTest() {
        try {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getName().contains("HTC")
                        || devices.get(i).getName().contains("GT")) {
                    connectedDevice = devices.get(i);
                    return devices.get(i).getName();
                }
            }
        } catch (Exception exc) {System.out.println("EDWx");
            exc.printStackTrace();
        }
        return null;
    }

    public String getDevice() {
        try {
            connectedDevice = devices.get(deviceIndex);
            return connectedDevice.getName();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        return null;
    }

    public ArrayList<String> getDevices() { //fix logic, CHANGE!!!
        /*devicesNames.clear();


        // get paired devices
        //pairedDevices = getAdapter().getBondedDevices();

        //for(BluetoothDevice device : pairedDevices)
            //devicesNames.add(device.getName()); //+ "\n" + device.getAddress()
        for(BluetoothDevice device: devices) {
            if(!devicesNames.contains(device.getName()))
                devicesNames.add(device.getName());
        }
        //System.out.println(devicesNames);*/
        return null; //devicesNames
    }

    public boolean isDevicesEmpty() {
        return devices.isEmpty();
    }

    public boolean isLast() {
        try {
            //if (connectedDevice == null) {
            //    System.out.println("connectedDevice == null");
            //    return true;
            //}
            if (devices.isEmpty()) {
                //System.out.println("devices.isEmpty()");
                return true;
            }
            if (connectedDevice != null && connectedDevice.equals(devices.getLast())) {
                //System.out.println("connectedDevice.equals(devices.getLast())");
                return true;
            }
        } catch (Exception exc) {System.out.println("EDWxxx");
            exc.printStackTrace();
        }

        return false;
    }

    public boolean isAfterLast() {
        if(deviceIndex == devices.size() + 1) {
            return true;
        }
        return false;
    }

    public boolean isFirst() {
        try {
            if (connectedDevice == null)
                return true;
            if (devices.isEmpty())
                return true;
            if (connectedDevice.equals(devices.getFirst())) {
                return true;
            }
        } catch (Exception exc) {System.out.println("EDW1xxxx");
            exc.printStackTrace();
        }

        return false;
    }

    public void switchToNextDevice() {
        if (!isLast()) {
            connectedDevice = devices.get(++deviceIndex);
        }
    }

    public void switchToPrevDevice() {
        if (!isFirst()) {
            connectedDevice = devices.get(--deviceIndex);
        }
    }

    private void init() {
        devices = new LinkedList<>();
    }

    public WiFiManager(Activity activity, Handler handler) {
        init();

        currentActivity = activity;
        mState = STATE_NONE;
        mHandler = handler;

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        currentActivity.registerReceiver(mReceiver, filter);
    }

    private synchronized void setState(int state) {
        mState = state;

        mHandler.obtainMessage(WiFiActivity.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device) {
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(WiFiActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(WiFiActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
        connectedDevices.add(device);
        connectedDevicesNames.add(device.getName());
    }

    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
        isConnected = false;
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
        //Toast.makeText(getApplicationContext(), "Your toast message.", Toast.LENGTH_SHORT).show();
        System.out.println("Connection Failed!!!"); //CHANGE!!
        canConnect = false;
        Message msg = mHandler.obtainMessage(WiFiActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(WiFiActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(STATE_LISTEN);

        //CHANGE??
        BluetoothDevice dev = getAdapter().getRemoteDevice(getAdapter().getAddress());
        connectedDevices.remove(dev);
        connectedDevicesNames.remove(getAdapter().getName());
        Message msg = mHandler.obtainMessage(WiFiActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(WiFiActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void enableDiscoveribility() {
        Intent discoverableIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
        currentActivity.startActivity(discoverableIntent);
    }

    public BluetoothAdapter getAdapter() {
        if (bta == null) {
            bta = BluetoothAdapter.getDefaultAdapter();
        }

        return bta;
    }

    public void enableBluetooth() {
        if (!getAdapter().isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            currentActivity.startActivityForResult(enableIntent, 2);
        }
    }

    public void setConnectedDevice(String deviceName) {
        for(BluetoothDevice dev: devices) {
            if(dev.getName().equals(deviceName)) {
                connectedDevice = dev;
            }
        }
    }

    public boolean isDiscovering() {
        return getAdapter().isDiscovering();
    }

    public void discoverDevices() {
        deviceIndex = 0;
        if (getAdapter().isDiscovering()) {
            getAdapter().cancelDiscovery();
        }
        getAdapter().startDiscovery();
    }

    public void stopDiscovering() {
        getAdapter().cancelDiscovery();
    }

    public boolean startServer() {
        if (getAdapter().isEnabled()) {
            this.start();
            return true;
        }

        return false;
    }

    public boolean canConnect() {
        return this.canConnect;
    }

    public void connectToServer() {
        canConnect = true;
        try {
            BluetoothDevice device = getAdapter().getRemoteDevice(
                    connectedDevice.getAddress());
            this.connect(device);
        } catch (Exception exc) {System.out.println("EDW1y");
            exc.printStackTrace();
        }
    }

    public void pairDevice(BluetoothDevice device) { //String deviceName
        //BluetoothDevice
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {System.out.println("EDW1yy");
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    System.out.println("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    System.out.println("Unpaired");
                }

            }
        }
    };

    public void sendMessage(String message) {
        if (message.length() > 0) {
            if (message.length() == 1) {
                message = "0" + message;
            }
            byte[] send = message.getBytes();
            this.write(send);
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = getAdapter()
                        .listenUsingRfcommWithServiceRecord(
                                "HideAndSeek",
                                UUID.fromString("e157c150-ff41-11e5-a837-0800200c9a66")); //DDD59690-4FBA-11E2-BCFD-0800200C9A66
            } catch (IOException e) {System.out.println("EDW1t");
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    System.out.println("EDW1");
                    e.printStackTrace();
                    break;
                }
                if (socket != null) {
                    synchronized (WiFiManager.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        System.out.println("EDW2");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                System.out.println("EDW3");
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString("e157c150-ff41-11e5-a837-0800200c9a66"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            getAdapter().cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                System.out.println("EDW4");
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    System.out.println("EDW5");
                    closeException.printStackTrace();
                }

                return;
            }

            synchronized (WiFiManager.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {System.out.println("EDW6");
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            isConnected = true;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {System.out.println("EDW7");
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    setMessage(new String(buffer, "UTF-8"));
                    mHandler.obtainMessage(WiFiActivity.MESSAGE_READ, bytes, -1,
                            buffer).sendToTarget();
                } catch (IOException e) {System.out.println("EDW8");
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {System.out.println("EDW9");
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
                isConnected = false;
            } catch (IOException e) {System.out.println("EDW10");
                e.printStackTrace();
            }
        }
    }

    private void setMessage(String message) {
        if (messageTaken) {
            this.message = message;
            messageTaken = false;
        }
    }

    public boolean isEnabled() {
        return getAdapter().isEnabled();
    }

    public ArrayList<String> getConnectedDevicesNames() {
        return connectedDevicesNames;
    }

    public ArrayList<String> getPairedDevices() {
        Set<BluetoothDevice> s = bta.getBondedDevices();
        ArrayList<String> names = new ArrayList<>();
        for (BluetoothDevice dev: s) {
            names.add(dev.getName());
        }
        return names;
    }

    public String getMyDeviceName() {
        return getAdapter().getName();
    }
}