package fr.inria.rsommerard.wifidirectserviceexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "WiFiDirectService";

    private static final String SERVICE_NAME = "_wifidirectservice";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
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

        Map<String, String> record = new HashMap<>();
        record.put("port", "42");

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, record);

        mManager.addLocalService(mChannel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.addLocalService::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "mManager.addLocalService::onFailure");
            }
        });

        mManager.setDnsSdResponseListeners(mChannel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.d(TAG, "mManager.setDnsSdResponseListeners::onDnsSdServiceAvailable");
                Log.d(TAG, "instanceName: " + instanceName);
                Log.d(TAG, "registrationType: " + registrationType);
                Log.d(TAG, "srcDevice: " + srcDevice);
            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.d(TAG, "mManager.setDnsSdResponseListeners::onDnsSdTxtRecordAvailable");
                Log.d(TAG, "fullDomainName: " + fullDomainName);
                Log.d(TAG, "txtRecordMap: " + txtRecordMap);
                Log.d(TAG, "srcDevice: " + srcDevice);
            }
        });

        WifiP2pDnsSdServiceRequest wifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, wifiP2pDnsSdServiceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.addServiceRequest::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "mManager.addServiceRequest::onFailure");
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.discoverServices::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "mManager.discoverServices::onFailure");
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

        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.clearLocalServices::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "mManager.clearLocalServices::onFailure");
            }
        });

        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.clearServiceRequests::onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "mManager.clearServiceRequests::onFailure");
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
