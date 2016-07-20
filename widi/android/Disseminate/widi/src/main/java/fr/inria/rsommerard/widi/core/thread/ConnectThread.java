package fr.inria.rsommerard.widi.core.thread;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pConfig;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;

public class ConnectThread extends Thread implements Runnable {

    private final WifiP2pManager.ActionListener mActionListener;
    private final WifiP2pConfig mWifiP2pConfig;

    public ConnectThread(final WifiP2pConfig wifiP2pConfig, final WifiP2pManager.ActionListener actionListener) {
        mWifiP2pConfig = wifiP2pConfig;
        mActionListener = actionListener;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.CONNECT);
            oOStream.flush();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            String jsonWifiP2pConfig = gson.toJson(mWifiP2pConfig);
            Log.d(WiDi.TAG, jsonWifiP2pConfig);
            oOStream.writeObject(jsonWifiP2pConfig);
            oOStream.flush();

            ObjectInputStream oIStream = new ObjectInputStream(socket.getInputStream());
            String ack = (String) oIStream.readObject();
            if (!Protocol.ACK.equals(ack)) {
                error(socket);
                return;
            }

            //socket.close();
            if (mActionListener != null)
                mActionListener.onSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            if (mActionListener != null)
                mActionListener.onFailure(WifiP2pManager.ERROR);
        } catch (ClassNotFoundException e) {
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

