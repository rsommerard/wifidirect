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

import fr.inria.rsommerard.widi.core.Device;
import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDeviceList;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;

public class RequestPeersThread extends Thread implements Runnable {

    private final WifiP2pManager.PeerListListener mListener;

    public RequestPeersThread(WifiP2pManager.PeerListListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.REQUEST_PEERS);
            oOStream.flush();

            ObjectInputStream oIStream = new ObjectInputStream(socket.getInputStream());
            String json = (String) oIStream.readObject();

            Log.d(WiDi.TAG, json);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            Type arrayListType = new TypeToken<ArrayList<Device>>() {}.getType();

            List<Device> deviceList = gson.fromJson(json, arrayListType);

            WifiP2pDeviceList peers = new WifiP2pDeviceList();

            for (Device device : deviceList) {
                WifiP2pDevice d = new WifiP2pDevice();
                d.deviceName = device.deviceName;
                d.deviceAddress = device.deviceAddress;
                peers.update(d);
            }

            //socket.close();

            mListener.onPeersAvailable(peers);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
