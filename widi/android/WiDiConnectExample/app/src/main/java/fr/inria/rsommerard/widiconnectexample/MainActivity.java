package fr.inria.rsommerard.widiconnectexample;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

// WiDi

//import android.net.wifi.p2p.WifiP2pDevice;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pConfig;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;

//import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import fr.inria.rsommerard.widi.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

//import android.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;

//import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

//import android.net.wifi.WpsInfo;
import fr.inria.rsommerard.widi.net.wifi.WpsInfo;

//import android.net.wifi.p2p.WifiP2pConfig;

//


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "WiDiConnectExample";

    private static final String SERVICE_NAME = "_widiservice";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private WifiP2pDevice mLastSrcDevice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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

        Button discoverBtn = (Button) findViewById(R.id.discover);
        assert discoverBtn != null;
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        Button connectBtn = (Button) findViewById(R.id.connect);
        assert connectBtn != null;
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastSrcDevice == null) {
                    Toast.makeText(MainActivity.this, "No device available", Toast.LENGTH_SHORT).show();
                    return;
                }

                WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                wifiP2pConfig.deviceAddress = mLastSrcDevice.deviceAddress;
                wifiP2pConfig.wps.setup = WpsInfo.PBC;
                wifiP2pConfig.groupOwnerIntent = 0;

                mManager.connect(mChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "mManager.connect::onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "mManager.connect::onFailure");
                    }
                });
            }
        });

        Button disconnectBtn = (Button) findViewById(R.id.disconnect);
        assert disconnectBtn != null;
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(MainActivity.TAG, "disconnection");
                mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(MainActivity.TAG, "mManager.cancelConnect::onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(MainActivity.TAG, "mManager.cancelConnect::onFailure");
                    }
                });

                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(MainActivity.TAG, "mManager.removeGroup::onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(MainActivity.TAG, "mManager.removeGroup::onFailure");
                    }
                });
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

                mLastSrcDevice = srcDevice;
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
