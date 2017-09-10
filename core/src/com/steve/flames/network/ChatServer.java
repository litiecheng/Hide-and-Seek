package com.steve.flames.network;

/**
 * Created by Flames on 8/9/2017.
 */

import com.steve.flames.Device;
import com.steve.flames.iWiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class ChatServer implements Runnable {

    private static final int PORT = 8888;
    private iWiFiDirect wfm;
    public static HashMap<Device, Socket> dataSocketsHMap = new HashMap<Device, Socket>();
    private Socket dataSocket;

    private ArrayList<PrintWriter> outList = new ArrayList<PrintWriter>();
    private BufferedReader in;
    //an thelw na steilw mono se enan mnm tote kanw k ena out sketo
    //private PrintWriter out;

    public ChatServer(iWiFiDirect wfm) {
        this.wfm = wfm;
        dataSocketsHMap = new HashMap<Device, Socket>();

        //start a new thread dedicated to receiving new connection requests
        Thread thread = new Thread(this);
        thread.start(); //calls the run() method on the new thread
    }

    @Override
    public void run() {
        ServerSocket connectionSocket = null;
        try {
            connectionSocket = new ServerSocket();
            connectionSocket.setReuseAddress(true);
            connectionSocket.bind(new InetSocketAddress(PORT));
            //as long as the program is running the server is waiting for new connection requests
            while(true) {
                System.out.println("WAITING FOR CONNECTION");
                final Socket dataSocket = connectionSocket.accept(); //accept new connection
                this.dataSocket = dataSocket;
                System.out.println("CONNECTION ESTABLISHED");

                OutputStream os = dataSocket.getOutputStream(); //get the outgoing stream
                outList.add(new PrintWriter(os,true));

                InputStream is = dataSocket.getInputStream(); //get the incoming stream
                in = new BufferedReader(new InputStreamReader(is));


                new Thread()
                {
                    public void run() {
                        String[] splitter; //this is used for splitting the incoming message to sections

                        System.out.println("PRIN TO PRWTO READ");
                        String clientNameAddress = wfm.receiveMessage(); //read the client name first (part of the communication protocol)
                        splitter = clientNameAddress.split("!~");
                        System.out.println("META TO PRWTO READ: "+ clientNameAddress);
                        wfm.getConnectedDevices().add(new Device(splitter[0], splitter[1]));
                        for(Device device: wfm.getAvailableDevices()) { //remove from available list
                            if(device.getAddress().equals(splitter[1]))
                                wfm.getAvailableDevices().remove(device);
                        }
                        //store the name of the client and the corresponding dataSocket in the dataSocketsHMap
                        ChatServer.dataSocketsHMap.put(wfm.getConnectedDevices().get(wfm.getConnectedDevices().size()-1), dataSocket);

                        sendConnClientsToAll(); //send the connected clients to the new client

                        System.out.println("TELEIWSE TO CHATSERVER");
                    }
                }.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(connectionSocket != null) {
                try {
                    connectionSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void sendConnClientsToAll() {
        wfm.sendMessageToAll("!START_CLIENT_INIT");
        System.out.println("ESTEILA !START_CLIENT_INIT");
        for(Device device: wfm.getConnectedDevices())
            wfm.sendMessageToAll(device.getName()+"!~"+device.getAddress()); //send message to client with PrintWriter
        wfm.sendMessageToAll("!END_CLIENT_INIT");
    }

    public void sendMessageToAll(String msg) {
        for(PrintWriter out: outList) {
            out.println(msg);
        }
    }

    public String receiveMessage() {
        try {
            if(in != null)
                return in.readLine();
        }catch(IOException e){
            int i=0;
            int j=0;
            if(e.getMessage().contains("Connection timed out")) {
                for(Socket socket: dataSocketsHMap.values()) {
                    if (socket == dataSocket) {
                        for(Device device: dataSocketsHMap.keySet()) {
                            if(i==j)
                                wfm.toast(device.getName() + " disconnected");
                            j++;
                        }
                    }
                    i++;
                }
                //textArea.append("Server is closed. You are now offline\n");
            }
            e.printStackTrace();
        }
        return null;
    }
}

