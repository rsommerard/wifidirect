package fr.inria.rsommerard.widi.core.thread;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.inria.rsommerard.widi.core.Protocol;
import fr.inria.rsommerard.widi.core.WiDi;

public class HelloThread extends Thread implements Runnable {

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(WiDi.SERVER_ADDRESS, WiDi.SERVER_PORT), WiDi.SOCKET_TIMEOUT);
            ObjectOutputStream oOStream = new ObjectOutputStream(socket.getOutputStream());
            oOStream.writeObject(Protocol.HELLO);
            oOStream.flush();
            ObjectInputStream oIStream = new ObjectInputStream(socket.getInputStream());
            String hello = oIStream.readObject().toString();
            assert Protocol.HELLO.equals(hello);
            //socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
