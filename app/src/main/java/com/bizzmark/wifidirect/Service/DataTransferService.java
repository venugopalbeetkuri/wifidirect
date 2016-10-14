// Copyright 2011 Google Inc. All Rights Reserved.

package com.bizzmark.wifidirect.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class DataTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_DATA = "com.example.android.wifidirect.SEND_DATA";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "group_owner_address";
    public static final String EXTRAS_GROUP_OWNER_PORT = "group_owner__port";

    Socket socket = null;

    public DataTransferService(String name) {
        super(name);
    }

    public DataTransferService() {
        super("DataTransferService");
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Toast.makeText(getApplicationContext(), " Inside handle intent...", Toast.LENGTH_SHORT).show();

        if(null == socket) {

            Toast.makeText(getApplicationContext(), " Socket null creating...", Toast.LENGTH_SHORT).show();
            boolean success = openSocket(intent);
        }

        String msg = intent.getExtras().getString("msg");

        try {

            /*returns an output stream to write data into this socket*/
            OutputStream stream = socket.getOutputStream();
            stream.write(msg.getBytes());
            Toast.makeText(getApplicationContext(), msg + " send to other device.", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {

            Toast.makeText(getApplicationContext(), ex.getMessage() ,Toast.LENGTH_SHORT).show();
        } finally {

            closeSocket();
        }
    }

    private boolean openSocket(Intent intent) {
        try {

            if (intent.getAction().equals(ACTION_SEND_DATA)) {

                String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);

                socket = new Socket();

                int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

                try {

                    // Log.d("xyz", "Opening client socket - ");
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    Toast.makeText(getApplicationContext(), "Other device socket connected." ,Toast.LENGTH_SHORT).show();
                } catch (Throwable e) {

                    Toast.makeText(getApplicationContext(), e.getMessage() ,Toast.LENGTH_SHORT).show();
                    Log.e("xyz", e.getMessage());
                } finally {

                    // closeSocket();
                }
            }

        } catch(Throwable th) {
            Toast.makeText(getApplicationContext(), th.getMessage() ,Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void closeSocket() {

        try {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage() ,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Throwable th) {
            Toast.makeText(getApplicationContext(), th.getMessage() ,Toast.LENGTH_SHORT).show();
        } finally {
            socket = null;
        }
    }


}
