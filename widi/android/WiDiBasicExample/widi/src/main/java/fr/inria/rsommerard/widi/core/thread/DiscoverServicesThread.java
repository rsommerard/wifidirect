package fr.inria.rsommerard.widi.core.thread;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import fr.inria.rsommerard.widi.core.DnsSdServiceResponse;
import fr.inria.rsommerard.widi.core.DnsSdTxtRecord;
import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import fr.inria.rsommerard.widi.net.wifi.p2p.nsd.WifiP2pServiceInfo;

public class DiscoverServicesThread extends Thread implements Runnable {

    private final WifiP2pManager.ActionListener mActionListener;
    private final WifiP2pManager.DnsSdServiceResponseListener mDnsSdServiceResponseListener;
    private final WifiP2pManager.DnsSdTxtRecordListener mDnsSdTxtRecordListener;
    private final WifiP2pDnsSdServiceInfo mwifiP2pDnsSdServiceInfo;

    public DiscoverServicesThread(
            final WifiP2pServiceInfo wifiP2pServiceInfo,
            final WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener,
            final WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener,
            final WifiP2pManager.ActionListener actionListener) {

        Log.d(WiDi.TAG, "DiscoverServicesThread");

        mwifiP2pDnsSdServiceInfo = (WifiP2pDnsSdServiceInfo) wifiP2pServiceInfo;
        mDnsSdServiceResponseListener = dnsSdServiceResponseListener;
        mDnsSdTxtRecordListener = dnsSdTxtRecordListener;
        mActionListener = actionListener;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.DISCOVER_SERVICES);
            oOStream.flush();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            // Send DnsSdServiceResponse
            DnsSdServiceResponse dnsSdServiceResponse = new DnsSdServiceResponse();
            dnsSdServiceResponse.instanceName = mwifiP2pDnsSdServiceInfo.getServiceName();
            dnsSdServiceResponse.registrationType = mwifiP2pDnsSdServiceInfo.getServiceType();


            String jsonDnsSdServiceResponse = gson.toJson(dnsSdServiceResponse);
            Log.d(WiDi.TAG, jsonDnsSdServiceResponse);
            oOStream.writeObject(jsonDnsSdServiceResponse);
            oOStream.flush();

            ObjectInputStream oIStream = new ObjectInputStream(socket.getInputStream());
            String carton = (String) oIStream.readObject();
            if (!Protocol.ACK.equals(carton)) {
                error(socket);
                return;
            }

            // Send DnsSdTxtRecord
            DnsSdTxtRecord dnsSdTxtRecord = new DnsSdTxtRecord();
            dnsSdTxtRecord.fullDomainName = mwifiP2pDnsSdServiceInfo.getServiceName() + "." + mwifiP2pDnsSdServiceInfo.getServiceType();
            if (mwifiP2pDnsSdServiceInfo.getTxtMap() != null)
                dnsSdTxtRecord.txtRecordMap = mwifiP2pDnsSdServiceInfo.getTxtMap();


            String jsonDnsSdTxtRecord = gson.toJson(dnsSdTxtRecord);
            Log.d(WiDi.TAG, jsonDnsSdTxtRecord);
            oOStream.writeObject(jsonDnsSdTxtRecord);
            oOStream.flush();

            carton = (String) oIStream.readObject();
            if (!Protocol.ACK.equals(carton)) {
                error(socket);
                return;
            }

            // Receive DnsSdServiceResponse

            String jsonDnsSdServiceResponses = (String) oIStream.readObject();

            Type typeDnsSdServiceResponse = new TypeToken<ArrayList<DnsSdServiceResponse>>() {}.getType();
            List<DnsSdServiceResponse> dnsSdServiceResponses = gson.fromJson(jsonDnsSdServiceResponses, typeDnsSdServiceResponse);
            for (DnsSdServiceResponse dssr : dnsSdServiceResponses) {
                WifiP2pDevice device = new WifiP2pDevice();
                device.deviceAddress = dssr.srcDevice.deviceAddress;
                device.deviceName = dssr.srcDevice.deviceName;
                mDnsSdServiceResponseListener.onDnsSdServiceAvailable(dssr.instanceName,
                        dssr.registrationType, device);
            }

            oOStream.writeObject(Protocol.ACK);
            oOStream.flush();

            // Receive DnsSdTxtRecord
            String jsonDnsSdTxtRecords = (String) oIStream.readObject();

            Type typeDnsSdTxtRecord = new TypeToken<ArrayList<DnsSdTxtRecord>>() {}.getType();
            List<DnsSdTxtRecord> dnsSdTxtRecords = gson.fromJson(jsonDnsSdTxtRecords, typeDnsSdTxtRecord);
            for (DnsSdTxtRecord dstr : dnsSdTxtRecords) {
                WifiP2pDevice device = new WifiP2pDevice();
                device.deviceAddress = dstr.srcDevice.deviceAddress;
                device.deviceName = dstr.srcDevice.deviceName;
                mDnsSdTxtRecordListener.onDnsSdTxtRecordAvailable(dstr.fullDomainName,
                        dstr.txtRecordMap, device);
            }

            oOStream.writeObject(Protocol.ACK);
            oOStream.flush();

            //socket.close();

            if (mActionListener != null)
                mActionListener.onSuccess();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            if (mActionListener != null)
                mActionListener.onFailure(WifiP2pManager.ERROR);
        }
    }

    private void error(Socket socket) throws IOException {
        //socket.close();
        if (mActionListener != null)
            mActionListener.onFailure(WifiP2pManager.ERROR);
    }
}
