package fr.inria.rsommerard.widitestingproject.wifidirect;

// WiDi
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
//import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
//

import android.util.Log;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.inria.rsommerard.widitestingproject.WiFiDirect;
import fr.inria.rsommerard.widitestingproject.dao.Device;
import fr.inria.rsommerard.widitestingproject.wifidirect.device.DeviceManager;

public class ServiceDiscoveryManager {

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_tcp";

    private static final int SERVICE_DISCOVERY_INTERVAL =  11000;

    private final DeviceManager mDeviceManager;
    private WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;
    private CustomActionListener mAddLocalServiceActionListener;
    private CustomDnsSdServiceResponseListener mDnsSdServiceResponseListener;
    private CustomDnsSdTxtRecordListener mDnsSdTxtRecordListener;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;
    private CustomActionListener mAddServiceRequestActionListener;
    private CustomActionListener mClearLocalServicesActionListener;
    private CustomActionListener mClearServiceRequestsActionListener;
    private CustomActionListener mDiscoverServicesActionListener;
    private CustomActionListener mStopPeerDiscoveryActionListener;
    private ScheduledExecutorService mExecutor;

    private final WifiP2pManager mWiFiP2pManager;
    private final WifiP2pManager.Channel mWiFiP2pChannel;

    private int mDiscoveryFailedCounter;

    public ServiceDiscoveryManager(final WifiP2pManager manager,
                                   final WifiP2pManager.Channel channel,
                                   final DeviceManager deviceManager) {

        mWiFiP2pManager = manager;
        mWiFiP2pChannel = channel;

        mDeviceManager = deviceManager;

        initialize();
    }

    public void initialize() {
        Log.d(WiDi.TAG, "initialize()");

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, null);
        mAddLocalServiceActionListener = new CustomActionListener(null, "Add local service failed: ");
        mDnsSdServiceResponseListener = new CustomDnsSdServiceResponseListener();
        mDnsSdTxtRecordListener = new CustomDnsSdTxtRecordListener();

        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mAddServiceRequestActionListener = new CustomActionListener(null, "Add service request failed: ");

        mClearLocalServicesActionListener = new CustomActionListener(null, "Clear local service failed: ");
        mClearServiceRequestsActionListener = new CustomActionListener(null, "Clear service requests failed: ");

        mDiscoverServicesActionListener = new DiscoverServicesActionListener(null, "Discovery failed: ");

        mStopPeerDiscoveryActionListener = new CustomActionListener(null, "Stop peer discovery failed: ");

        mDiscoveryFailedCounter = 0;
    }

    public void startDiscovery() {
        Log.d(WiDi.TAG, "Start service discovery");

        if (mExecutor != null) {
            Log.i(WiDi.TAG, "Service discovery already started");
            return;
        }

        mExecutor = Executors.newSingleThreadScheduledExecutor();

        // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html
        mWiFiP2pManager.clearLocalServices(mWiFiP2pChannel, mClearLocalServicesActionListener);
        mWiFiP2pManager.clearServiceRequests(mWiFiP2pChannel, mClearServiceRequestsActionListener);

        mWiFiP2pManager.stopPeerDiscovery(mWiFiP2pChannel, mStopPeerDiscoveryActionListener);

        mWiFiP2pManager.addLocalService(mWiFiP2pChannel, mWifiP2pDnsSdServiceInfo, mAddLocalServiceActionListener);
        mWiFiP2pManager.setDnsSdResponseListeners(mWiFiP2pChannel, mDnsSdServiceResponseListener, mDnsSdTxtRecordListener);
        mWiFiP2pManager.addServiceRequest(mWiFiP2pChannel, mWifiP2pDnsSdServiceRequest, mAddServiceRequestActionListener);

        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                discover();
            }
        }, 0, SERVICE_DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public boolean isServiceDiscoveryStarted() {
        return mExecutor != null;
    }

    private void discover() {
        mWiFiP2pManager.discoverServices(mWiFiP2pChannel, mDiscoverServicesActionListener);
    }

    public void stopDiscovery() {
        Log.d(WiDi.TAG, "Stop service discovery");
        if (mExecutor != null)
            mExecutor.shutdown();

        mExecutor = null;

        // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html
        mWiFiP2pManager.clearLocalServices(mWiFiP2pChannel, mClearLocalServicesActionListener);
        mWiFiP2pManager.clearServiceRequests(mWiFiP2pChannel, mClearServiceRequestsActionListener);

        mDiscoveryFailedCounter = 0;
    }

    private void restart() {
        stop();
        initialize();
        startDiscovery();
    }

    public void stop() {
        stopDiscovery();
        mWiFiP2pManager.stopPeerDiscovery(mWiFiP2pChannel, mStopPeerDiscoveryActionListener);
    }

    private boolean isValidDnsSdTxtRecord(final String fullDomainName, final WifiP2pDevice srcDevice) {
        if (fullDomainName == null ||
                !fullDomainName.contains(SERVICE_NAME + "." + SERVICE_TYPE)) {
            return false;
        }

        if (srcDevice.deviceAddress == null ||
                srcDevice.deviceAddress.isEmpty()) {
            return false;
        }

        if (srcDevice.deviceName == null ||
                srcDevice.deviceName.isEmpty()) {
            return false;
        }

        return true;
    }

    private class DiscoverServicesActionListener extends CustomActionListener {

        public DiscoverServicesActionListener(String onSuccessMessage, String onFailureMessage) {
            super(onSuccessMessage, onFailureMessage);
        }

        @Override
        public void onFailure(int reason) {
            super.onFailure(reason);
            mDiscoveryFailedCounter++;
            if (mDiscoveryFailedCounter >= 3) {
                restart();
            }
        }
    }

    private class CustomDnsSdServiceResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(final String instanceName, final String registrationType, final WifiP2pDevice srcDevice) {
            // Nothing
            //Log.i(WiDi.TAG, "DnsSdService available");
        }
    }

    private class CustomDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(final String fullDomainName, final Map<String, String> txtRecordMap, final WifiP2pDevice srcDevice) {
            // Log.i(WiDi.TAG, "DnsSdTxtRecord available");
            // Log.i(WiDi.TAG, srcDevice.toString());
            Log.i(WiDi.TAG, srcDevice.deviceName + " available");

            if (isValidDnsSdTxtRecord(fullDomainName, srcDevice)) {
                Device device = new Device();
                device.setName(srcDevice.deviceName);
                device.setAddress(srcDevice.deviceAddress);
                device.setTimestamp(Long.toString(System.currentTimeMillis()));

                if (mDeviceManager.containsDevice(device)) {
                    mDeviceManager.updateDevice(device);
                } else {
                    mDeviceManager.addDevice(device);
                }
            }
        }
    }
}
