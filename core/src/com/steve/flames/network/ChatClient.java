package com.steve.flames.network;

/**
 * Created by Flames on 8/9/2017.
 */

import com.steve.flames.Device;
import com.steve.flames.HaSGame;
import com.steve.flames.screens.PlayScreen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ChatClient implements Runnable{
    private static final int PORT = 8888;
    private HaSGame game;
    private String hostIP;

    private PrintWriter out;
    private BufferedReader in;
    private boolean finish = false;

    private Socket dataSocket;

    public ChatClient(String ip, HaSGame game) {
        hostIP = ip.substring(1); //to remove '/'
        this.game = game;
        System.out.println("ZAZA BHKA CLINET");
        Thread thread = new Thread(this);
        thread.start(); //calls the run() method on the new thread
    }

    @Override
    public void run() {
        try {
            dataSocket = new Socket();
            dataSocket.bind(null);
            System.out.println("HOST IP: " +hostIP);
            dataSocket.connect((new InetSocketAddress(hostIP, PORT)), 500);

            OutputStream os = dataSocket.getOutputStream(); //get the outgoing stream
            out = new PrintWriter(os,true);
            InputStream is = dataSocket.getInputStream(); //get the incoming stream
            in = new BufferedReader(new InputStreamReader(is));

            //send current device's name to server
            sendMessageToServer(game.wfm.getCurrentDevice().getName() + "!~"+ game.wfm.getCurrentDevice().getAddress());
            receiveConnectedClients();
        } catch (IOException e) {
            System.out.println("MALLON DE TO STEILE TO ONOMA TOU");
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(String msg) {
        out.println(msg);
        System.out.println("SENT TO SERVER: " + msg);
    }

    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            if(e.getMessage().contains("Connection timed out"))
                game.wfm.disconnect();
            e.printStackTrace();
        }
        return null;
    }

    private void receiveConnectedClients() {
        String[] splitter; //this is used for splitting the incoming message to sections

        System.out.println("WAITING FOR MSG TO READ");
        String msg = game.wfm.receiveMessage();
        while(msg != null) {
            System.out.println("MESSAGE FROM SERVER " + msg);
            if(msg.equals("!START_CLIENT_INIT")) {
                        /*reads all the current connected clients (the server writes them in the stream. Part of the communication protocol)*/
                msg = game.wfm.receiveMessage();
                System.out.println("CONNECTED DEVICE NAME: " + msg);
                splitter = msg.split("!~");
                while(!msg.equals("!END_CLIENT_INIT")) {
                    game.wfm.getConnectedDevices().add(new Device(splitter[0], splitter[1]));
                    msg = game.wfm.receiveMessage();
                    splitter = msg.split("!~");
                    System.out.println("CLIENT TELEIWSA ME TO INIT");
                }
            }
            else if(msg.equals("!START")) {
                finish = true;
                System.out.println("EDW1");
            }
            msg = game.wfm.receiveMessage(); //read next message
            if(finish)
                msg=null;
            System.out.println("EDW2 "+msg);
        }
        System.out.println("CLIENT BGHKA APO LOOPA");
        //dataSocket.close();
    }

    public boolean isFinish() {
        return finish;
    }

    public Socket getDataSocket() {
        return dataSocket;
    }
}
