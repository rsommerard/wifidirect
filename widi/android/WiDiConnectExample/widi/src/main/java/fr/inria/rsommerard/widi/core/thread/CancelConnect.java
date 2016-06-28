package fr.inria.rsommerard.widi.core.thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;

public class CancelConnect extends Thread implements Runnable {

    private final WifiP2pManager.ActionListener mActionListener;

    public CancelConnect(final WifiP2pManager.ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.CANCEL_CONNECT);
            oOStream.flush();

            ObjectInputStream oIStream = new ObjectInputStream(socket.getInputStream());
            String ack = (String) oIStream.readObject();

            if (Protocol.CARTON.equals(ack)) {
                errorProcess(socket);
            }

            //socket.close();
            if (mActionListener != null)
                mActionListener.onSuccess();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            errorProcess(socket);
        }
    }

    private void errorProcess(final Socket socket) {
        if (mActionListener != null)
            mActionListener.onFailure(WifiP2pManager.ERROR);
        /*try {
            //closeSocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mActionListener != null)
                mActionListener.onFailure(WifiP2pManager.ERROR);
        }*/
    }

    private void closeSocket(final Socket socket) throws IOException {
        if (socket != null)
            if (!socket.isClosed())
                socket.close();
    }
}