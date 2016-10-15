package com.bizzmark.wifidirect.Activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizzmark.wifidirect.Adapter.MyAdapter;
import com.bizzmark.wifidirect.BroadcastReceiver.WifiDirectBroadcastReceiver;

import com.bizzmark.wifidirect.R;
import com.bizzmark.wifidirect.Service.ClientSocketFactory;
import com.bizzmark.wifidirect.Service.DataTransferService;
import com.bizzmark.wifidirect.Task.DataServerAsyncTask;
import com.bizzmark.wifidirect.Utils.Utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button discover;
    private EditText edt;

    // private Button server;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List peers = new ArrayList();
    private List<HashMap<String, String>> peersshow = new ArrayList();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private WifiP2pInfo info;

    private DataServerAsyncTask mDataTask;

    private Utils utils;
    String message = "hello";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initIntentFilter();
        initReceiver();
        initEvents();
    }

    private void initView() {

        discover = (Button) findViewById(R.id.bt_discover);
        edt = (EditText)findViewById(R.id.sharemsg);

        // server = (Button) findViewById(R.id.bt_server);

        mRecyclerView = (RecyclerView) findViewById(
                R.id.recyclerview);
        mAdapter = new MyAdapter(peersshow);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
    }

    private void initIntentFilter() {

        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initReceiver() {

        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {

            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {

                Log.i("xyz", "onPeersAvailable.");
                Toast.makeText(getBaseContext(),"onPeersAvailable.", Toast.LENGTH_SHORT).show();

                peers.clear();
                peersshow.clear();

                Collection<WifiP2pDevice> aList = peersList.getDeviceList();
                peers.addAll(aList);

                for (int i = 0; i < aList.size(); i++) {

                    WifiP2pDevice a = (WifiP2pDevice) peers.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", a.deviceName);
                    map.put("address", a.deviceAddress);


                    Log.i("xyz", "name: " + a.deviceName + " address: " + a.deviceAddress);
                    Toast.makeText(getBaseContext(),"name: " + a.deviceName + " address: " + a.deviceAddress, Toast.LENGTH_SHORT).show();
                    peersshow.add(map);
                }

                mAdapter = new MyAdapter(peersshow);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {

                    @Override
                    public void OnItemClick(View view, int position) {


                        CreateConnect(peersshow.get(position).get("address"), peersshow.get(position).get("name"));

                    }

                    @Override
                    public void OnItemLongClick(View view, int position) {

                    }
                });
            }
        };

        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo minfo) {

                Toast.makeText(getBaseContext(),"Connection info available.", Toast.LENGTH_SHORT).show();
                info = minfo;

                Boolean groupFormed = info.groupFormed;
                Boolean groupOwner = info.isGroupOwner;

                Toast.makeText(getApplicationContext(), "Group formed: " + groupFormed.toString() + " Groupowner: " + groupOwner.toString(), Toast.LENGTH_SHORT).show();

                // mManager.requestGroupInfo(mChannel, WifiP2pManager.GroupInfoListener);

                TextView view = (TextView) findViewById(R.id.tv_main);

                if (groupOwner) {
                    Log.i("xyz", "Group owner.");
                    Toast.makeText(getBaseContext(),"Group owner.", Toast.LENGTH_SHORT).show();
                    mDataTask = new DataServerAsyncTask(MainActivity.this, view);
                    mDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        };

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.i("xyz", "onResume");
        Toast.makeText(getBaseContext(),"onResume: ", Toast.LENGTH_SHORT).show();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause() {

        super.onPause();
        Log.i("xyz", "onPause");
        Toast.makeText(getBaseContext(),"onPause: ", Toast.LENGTH_SHORT).show();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void ResetReceiver() {

        unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, mFilter);
    }

   // static boolean serv = false;

    private void initEvents() {

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //unregisterReceiver(mReceiver);
                //registerReceiver(mReceiver, mFilter);

                // get text value and assign to message string

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(getBaseContext(), "Discovery peer success", Toast.LENGTH_SHORT).show();
                        Log.i("xyz", "Discovery peer success");

                    }

                    @Override
                    public void onFailure(int reason) {

                        // popup[ for wifi




                        Toast.makeText(getBaseContext(), "Discovery peer failure. Reason: " + reason, Toast.LENGTH_SHORT).show();
                        Log.i("xyz", "Discovery peer failure. Reason: " + reason);
                    }
                });
            }
        });

        /*server.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                serv = true;
            }
        });*/

        mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {

            @Override
            public void OnItemClick(View view, int position) {

                CreateConnect(peersshow.get(position).get("address"), peersshow.get(position).get("name"));
            }

            @Override
            public void OnItemLongClick(View view, int position) {
            }
        });
    }

    static int count = 1;
    WifiP2pConfig config = null;


    /*A demo base on API which you can connect android device by wifidirect,
    and you can send data by socket,what is the most important is that you can set
    which device is the client or server.*/

    private void CreateConnect(String address, final String name) {

        Log.i("xyz", address);
        Toast.makeText(getBaseContext(),"address: " + address, Toast.LENGTH_SHORT).show();
        //if (null == config) {
            initCreateConnect(address);
        //}
        String msg = edt.getText().toString();
        sendData(msg);

    }

    /**
     * Initialize WifiP2P Configuration.
     * @param address
     */
    private void initCreateConnect(String address){
        try {

            config = new WifiP2pConfig();

            config.deviceAddress = address;

            //config.deviceName;

             config.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                    Log.i("xyz", "P2P Connection success.");
                    Toast.makeText(getBaseContext(),"P2P Connection success.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {

                    Log.i("xyz", "P2P Connection failure: Reason: " + reason);
                    Toast.makeText(getBaseContext(),"P2P Connection failure: Reason: " + reason, Toast.LENGTH_SHORT).show();
                    config = null;
                }
            });
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Send data to other device.
     */
    private void sendData(String msg) {

        try {

            Toast.makeText(getApplicationContext(), "Calling senddata.", Toast.LENGTH_SHORT).show();
            Log.i("xyz", "Calling senddata.");


            /*Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        ClientSocketFactory.writeToSocket(info.groupOwnerAddress.getHostAddress(), 8888, "Hi");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();*/



            if (null == info) {
                Toast.makeText(getApplicationContext(), "Send data info obj null.", Toast.LENGTH_SHORT).show();
                Log.i("xyz", "Send data info obj null.");
            }

            Intent serviceIntent = new Intent(MainActivity.this, DataTransferService.class);

            serviceIntent.setAction(DataTransferService.ACTION_SEND_DATA);

            String hostAddress = info.groupOwnerAddress.getHostAddress();

            //hostAddress = "192.168.5.61";
            serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_ADDRESS, hostAddress);
            Log.i("xyz", "owner ip is " + hostAddress);
            Toast.makeText(getApplicationContext(), "owner ip is " + hostAddress, Toast.LENGTH_SHORT).show();

            serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_PORT, 8888);
           // msg = msg + count++;

            serviceIntent.putExtra("msg", msg);

            MainActivity.this.startService(serviceIntent);

        } catch(Throwable th) {
            Toast.makeText(getBaseContext(), th.getMessage(), Toast.LENGTH_SHORT).show();
        }

    } // Send data.

}
