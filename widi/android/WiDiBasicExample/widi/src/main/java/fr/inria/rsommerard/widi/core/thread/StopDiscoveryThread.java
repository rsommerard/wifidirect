package fr.inria.rsommerard.widi.core.thread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;

public class StopDiscoveryThread extends Thread implements Runnable {

    private final WifiP2pManager.ActionListener mListener;

    public StopDiscoveryThread(WifiP2pManager.ActionListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.STOP_DISCOVERY);
            oOStream.flush();
            //socket.close();

            if (mListener != null)
                mListener.onSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null)
                mListener.onFailure(WifiP2pManager.ERROR);
        }
    }
}