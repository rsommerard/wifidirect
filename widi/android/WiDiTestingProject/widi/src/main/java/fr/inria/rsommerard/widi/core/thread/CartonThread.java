package fr.inria.rsommerard.widi.core.thread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;

public class CartonThread extends Thread implements Runnable {

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.CARTON);
            oOStream.flush();
            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}