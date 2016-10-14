package com.bizzmark.wifidirect.Service;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Venu on 14-10-2016.
 */
public class ClientSocketFactory {


    private static final int SOCKET_TIMEOUT = 10000;

    static Socket socket = null;


    public static boolean writeToSocket(String host, int port, String message) {

        try {

            if (null == socket) {
                boolean success = openSocket(host, port);
                if(!success) {
                    return false;
                }
            }
            return write(message);

            //return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            closeSocket();
        }
        return false;

    }

    private static boolean write(String message) {
        try {

            /*returns an output stream to write data into this socket*/
            OutputStream stream = socket.getOutputStream();
            stream.write(message.getBytes());
            // Toast.makeText(getApplicationContext(), msg + " send to other device.", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Throwable ex) {
            // Toast.makeText(getApplicationContext(), ex.getMessage() ,Toast.LENGTH_SHORT).show();
            closeSocket();
        }
        return false;
    }

    private static boolean openSocket(String host, int port) {

        try {

            socket = new Socket();

            // Log.d("xyz", "Opening client socket - ");
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
            // Toast.makeText(getApplicationContext(), "Other device socket connected." ,Toast.LENGTH_SHORT).show();

            return true;
        } catch (Throwable th) {
            // Toast.makeText(getApplicationContext(), th.getMessage() ,Toast.LENGTH_SHORT).show();
            closeSocket();
        }

        return false;
    }

    private static void closeSocket() {

        try {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Toast.makeText(getApplicationContext(), e.getMessage() ,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Throwable th) {
            // Toast.makeText(getApplicationContext(), th.getMessage() ,Toast.LENGTH_SHORT).show();
        } finally {
            socket = null;
        }
    }


}
