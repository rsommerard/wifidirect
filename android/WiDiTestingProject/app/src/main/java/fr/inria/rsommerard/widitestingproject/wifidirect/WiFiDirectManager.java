package fr.inria.rsommerard.widitestingproject.wifidirect;

// WiDi
//import android.net.wifi.p2p.WifiP2pManager;

import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
//

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import fr.inria.rsommerard.widitestingproject.WiFiDirect;
import fr.inria.rsommerard.widitestingproject.data.DataManager;
import fr.inria.rsommerard.widitestingproject.wifidirect.connection.ConnectionManager;
import fr.inria.rsommerard.widitestingproject.wifidirect.device.DeviceManager;
import fr.inria.rsommerard.widitestingproject.wifidirect.exception.WiFiException;

public class WiFiDirectManager {

    private final ServiceDiscoveryManager mServiceDiscoveryManager;
    //private final WifiManager mWiFiManager;
    private final ConnectionManager mConnectionManager;
    private final DeviceManager mDeviceManager;
    private final DataManager mDataManager;

    private int mNetId;

    public WiFiDirectManager(final Context context) throws IOException, WiFiException {
        // WiDi
        // WifiP2pManager wiFiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager wiFiP2pManager = new WifiP2pManager();
        //

        WifiP2pManager.ChannelListener initializeChannelListener = new CustomChannelListener();
        WifiP2pManager.Channel wiFiP2pChannel = wiFiP2pManager.initialize(context, context.getMainLooper(), initializeChannelListener);


        WiFiDirect.cleanAllGroupsRegistered(wiFiP2pManager, wiFiP2pChannel);

        mDeviceManager = new DeviceManager(context);

        mDataManager = new DataManager(context);
        WiFiDirect.populateDataTable(mDataManager, 5);

        disconnectWiFi();

        mServiceDiscoveryManager = new ServiceDiscoveryManager(wiFiP2pManager, wiFiP2pChannel, mDeviceManager);
        mServiceDiscoveryManager.startDiscovery();

        mConnectionManager = new ConnectionManager(context, wiFiP2pManager, wiFiP2pChannel, mServiceDiscoveryManager, mDataManager);
    }

    public void process() {
        if (mDeviceManager.hasDevices() && mDataManager.hasData()) {
            mConnectionManager.connect(mDeviceManager.getDevice());
        } else {
            Log.d(WiFiDirect.TAG, "No device or data to send available");
        }
    }

    // To be able to decide if we want to send or receive data. For instance, we can disable WiFi-Direct
    // data sharing when user is in a POI.
    public void stop(final Context context) {
        mConnectionManager.stop(context);
        mServiceDiscoveryManager.stop();

        mDeviceManager.deleteAll();

        reconnectWiFi();
    }

    private void disconnectWiFi() {
        /*mNetId = mWiFiManager.getConnectionInfo().getNetworkId();

        if (mNetId != -1)
            mWiFiManager.disableNetwork(mNetId);*/
    }

    private void reconnectWiFi() {
        /*if (mNetId != -1)
            mWiFiManager.enableNetwork(mNetId, true);*/
    }

    public void printData() {
        String str = WiFiDirect.dataListToString(mDataManager.getAllData());

        Log.i(WiFiDirect.TAG, str);
    }

    private class CustomChannelListener implements WifiP2pManager.ChannelListener {
        @Override
        public void onChannelDisconnected() {
            Log.i(WiFiDirect.TAG, "Channel disconnected");
        }
    }
}
