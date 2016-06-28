package fr.inria.rsommerard.widibasicexample;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

// WiDi
//import android.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
//

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "WiDiBasicExample";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // WiDi
        // mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mManager = new WifiP2pManager();
        //

        mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "mManager.initialize::onChannelDisconnected");
            }
        });
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.discoverPeers::onSuccess");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "mManager.discoverPeers::onFailure");
            }
        });
    }

    @Override
    protected void onDestroy() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.stopPeerDiscovery::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "mManager.stopPeerDiscovery::onFailure");
            }
        });

        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
}
