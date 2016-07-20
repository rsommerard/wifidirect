package ut.disseminate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.NetworkInfo;
import fr.inria.rsommerard.widi.net.wifi.WpsInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pConfig;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDeviceList;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;


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

import java.util.ArrayList;

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

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        ArrayList<WifiP2pDevice> devices = new ArrayList<WifiP2pDevice>(peers.getDeviceList());
                        if (WiDi.TAG.equals("WiDiTwo"))
                            return;

                        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                        wifiP2pConfig.deviceAddress = devices.get(0).deviceAddress;
                        wifiP2pConfig.wps.setup = WpsInfo.PBC;
                        wifiP2pConfig.groupOwnerIntent = 0;

                        mManager.connect(mChannel, wifiP2pConfig, null);
                    }
                });
            }
        }
    }
}

