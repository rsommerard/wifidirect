package fr.inria.rsommerard.widitestingproject.wifidirect.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widitestingproject.data.DataManager;

public class Server extends Thread implements Runnable {

    private final DataManager mDataManager;

    public Server(final DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = initializeServerSocket();

        assert serverSocket != null;

        while (checkThreadStatus(serverSocket)) {
            process(serverSocket);
        }
    }

    private void process(final ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();
            new Passive(socket, mDataManager).start();
        } catch (IOException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private ServerSocket initializeServerSocket() {
        try {
            return new ServerSocket(WiDi.DATA_EXCHANGE_PORT);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean checkThreadStatus(final ServerSocket serverSocket) {
        if (Thread.currentThread().isInterrupted()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                return false;
            }

            return false;
        }

        return true;
    }
}
