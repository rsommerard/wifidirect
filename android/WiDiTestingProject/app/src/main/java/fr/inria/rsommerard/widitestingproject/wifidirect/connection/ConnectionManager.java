package fr.inria.rsommerard.widitestingproject.wifidirect.connection;

// WiDi
//import android.net.NetworkInfo;
//import android.net.wifi.WpsInfo;
//import android.net.wifi.p2p.WifiP2pConfig;
//import android.net.wifi.p2p.WifiP2pInfo;
//import android.net.wifi.p2p.WifiP2pManager;

import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.NetworkInfo;
import fr.inria.rsommerard.widi.net.wifi.WpsInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pConfig;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
//

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.inria.rsommerard.widitestingproject.WiFiDirect;
import fr.inria.rsommerard.widitestingproject.dao.Device;
import fr.inria.rsommerard.widitestingproject.data.DataManager;
import fr.inria.rsommerard.widitestingproject.wifidirect.CustomActionListener;
import fr.inria.rsommerard.widitestingproject.wifidirect.ServiceDiscoveryManager;

public class ConnectionManager {

    private static final int CONNECTION_TIMEOUT = 60000;

    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private final ConnectionBroadcastReceiver mConnectionBroadcastReceiver;
    private final Server mServer;
    private final DataManager mDataManager;
    private final ScheduledExecutorService mExecutor;
    private final ServiceDiscoveryManager mServiceDiscoveryManager;

    private NetworkInfo mNetworkInfo;

    public ConnectionManager(final Context context,
                             final WifiP2pManager manager,
                             final WifiP2pManager.Channel channel,
                             final ServiceDiscoveryManager serviceDiscoveryManager,
                             final DataManager dataManager) throws IOException {

        mWifiP2pManager = manager;
        mWifiP2pChannel = channel;

        mServiceDiscoveryManager = serviceDiscoveryManager;

        mDataManager = dataManager;

        mServer = new Server(mDataManager);
        mServer.start();

        mConnectionBroadcastReceiver = new ConnectionBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        context.registerReceiver(mConnectionBroadcastReceiver, intentFilter);

        mExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void stop(final Context context) {
        mExecutor.shutdown();

        disconnect();

        mServer.interrupt();

        try {
            context.unregisterReceiver(mConnectionBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            // Nothing: BroadcastReceiver is just not registered
        }
    }

    public void connect(final Device device) {
        Log.d(WiDi.TAG, "Connection connect");

        if (mNetworkInfo == null) {
            Log.e(WiDi.TAG, "Network info not yet available");
            return;
        }

        if (mNetworkInfo.isConnectedOrConnecting()) {
            Log.e(WiDi.TAG, "Device connected or connecting");
            return;
        }

        mServiceDiscoveryManager.stopDiscovery();

        mExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                Log.d(WiDi.TAG, "TIMEOUT");
                if (mNetworkInfo.isConnectedOrConnecting()) {
                    if (!mServiceDiscoveryManager.isServiceDiscoveryStarted()) {
                        mServiceDiscoveryManager.startDiscovery();
                    }
                    disconnect();
                }
            }
        }, CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = device.getAddress();
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        wifiP2pConfig.groupOwnerIntent = 0;

        mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(WiDi.TAG, "Connect succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WiDi.TAG, "Connect failed: " +
                        WiFiDirect.getActionListenerFailureName(reason));
                if (!mServiceDiscoveryManager.isServiceDiscoveryStarted()) {
                    mServiceDiscoveryManager.startDiscovery();
                }
                disconnect();
            }
        });
    }

    public void disconnect() {
        Log.d(WiDi.TAG, "Connection disconnect");

        WifiP2pManager.ActionListener cancelConnectActionListener = new CustomActionListener("Cancel connect succeeded", "Cancel connect failed: ");
        mWifiP2pManager.cancelConnect(mWifiP2pChannel, cancelConnectActionListener);

        WifiP2pManager.ActionListener removeGroupActionListener = new CustomActionListener("Remove group succeeded", "Remove group failed: ");
        mWifiP2pManager.removeGroup(mWifiP2pChannel, removeGroupActionListener);
    }

    private class ConnectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                mNetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

                Log.d(WiDi.TAG, wifiP2pInfo.toString());
                Log.d(WiDi.TAG, mNetworkInfo.toString());

                if (!mNetworkInfo.isConnected()) {
                    if (!mServiceDiscoveryManager.isServiceDiscoveryStarted()) {
                        mServiceDiscoveryManager.startDiscovery();
                    }
                    return;
                }

                mServiceDiscoveryManager.stopDiscovery();

                if (wifiP2pInfo.isGroupOwner) {
                    new Active(wifiP2pInfo.groupOwnerAddress, mDataManager).start();
                }
            }
        }
    }
}
