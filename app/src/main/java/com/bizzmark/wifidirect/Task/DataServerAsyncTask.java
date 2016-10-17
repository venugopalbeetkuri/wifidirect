package com.bizzmark.wifidirect.Task;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bizzmark.wifidirect.Activity.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class DataServerAsyncTask extends
        AsyncTask<Void, Void, String> {

    private TextView statusText;
    private MainActivity activity;

    static ServerSocket serverSocket = null;
    Socket client = null;

    /**
     * @param statusText
     */
    public DataServerAsyncTask(MainActivity activity, View statusText) {

        this.statusText = (TextView) statusText;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {

            if(null == serverSocket) {
                Log.i("xyz", "Server socket initialized.");
                serverSocket = new ServerSocket(8888);
            }


            if(null == client){
                client = serverSocket.accept();
            }
            // Log.i("xyz","");

             // Log.i("xyz","");

            Log.i("xyz","Client connected.");
            InputStream inputstream = client.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = inputstream.read()) != -1) {
                baos.write(i);
            }

            String str = baos.toString();



            return str;

        } catch (Throwable e) {

            try {
                serverSocket.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Log.e("xyz", e.toString());
            return null;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            client = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {

        Log.i("xyz", "data onpost");

        Toast.makeText(activity, "result"+result, Toast.LENGTH_SHORT).show();

        if (result != null) {
            statusText.setText("Data-String is " + result);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {

    }

}