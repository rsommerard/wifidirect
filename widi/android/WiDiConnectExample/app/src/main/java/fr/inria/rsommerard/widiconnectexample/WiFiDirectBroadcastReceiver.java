package fr.inria.rsommerard.widiconnectexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// WiDi
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pDeviceList;
import fr.inria.rsommerard.widi.net.NetworkInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDeviceList;
//

import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    public WiFiDirectBroadcastReceiver(final WifiP2pManager manager, final WifiP2pManager.Channel channel) {
        super();
        mManager = manager;
        mChannel = channel;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        Log.d(MainActivity.TAG, action);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(MainActivity.TAG, "Wifi P2P is enabled");
            } else {
                Log.d(MainActivity.TAG, "Wifi P2P is not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.d(MainActivity.TAG, peers.toString());
                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo == null)
                return;
            Log.d(MainActivity.TAG, networkInfo.toString());

            WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            if (wifiP2pInfo == null)
                return;
            Log.d(MainActivity.TAG, wifiP2pInfo.toString());

            if (networkInfo.isConnected()) {
                Log.d(MainActivity.TAG, "Devices connected");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
