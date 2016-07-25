package fr.inria.rsommerard.widitestingproject.wifidirect.connection;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widitestingproject.WiFiDirect;
import fr.inria.rsommerard.widitestingproject.dao.Data;
import fr.inria.rsommerard.widitestingproject.data.DataManager;

public class Active extends Thread implements Runnable {
    private static final int SOCKET_TIMEOUT = 5000;

    private final InetAddress mGroupOwnerAddress;
    private final DataManager mDataManager;
    private final Random mRandom;

    public Active(final InetAddress groupOwnerAddress, final DataManager dataManager) {
        mGroupOwnerAddress = groupOwnerAddress;
        mDataManager = dataManager;

        mRandom = new Random();
        mRandom.setSeed(42L);
    }

    @Override
    public void run() {
        Socket socket = connect();

        if (socket == null) {
            Log.e(WiDi.TAG, "Cannot open socket with the groupOwner");
            return;
        }

        try {
            sendMessage(socket, Protocol.HELLO);
            waitAndCheck(socket, Protocol.HELLO);
            sendData(socket);
            waitData(socket);
            socket.close();
        } catch (ClassNotFoundException | IOException | SQLiteConstraintException e) {
            e.printStackTrace();
            closeSocket(socket);
        }
    }

    private void waitData(Socket socket) throws IOException, ClassNotFoundException, SQLiteConstraintException {
        sendMessage(socket, Protocol.SEND);

        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        String gsonStr = (String) objectInputStream.readObject();

        List<Data> data = DataManager.deGsonify(gsonStr);

        Log.i(WiDi.TAG, data + " received");

        for (Data d : data) {
            mDataManager.addData(d);
        }

        sendMessage(socket, Protocol.ACK);
    }

    private void sendData(final Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        List<Data> data = mDataManager.getAllData();
        Collections.shuffle(data);

        //int nb = mRandom.nextInt(data.size());
        //Log.i(WiDi.TAG, "Nb data to send: " + nb);

        List<Data> dataList = new ArrayList<Data>();
        if (mRandom.nextBoolean()) {
            Log.d(WiDi.TAG, "Sending data");
            dataList.add(new Data(null, data.get(0).getContent(), data.get(0).getIdentifier()));
        } else {
            Log.d(WiDi.TAG, "Random false");
        }
        //for (int i = 0; i < nb; i++) {
        //    dataList.add(new Data(null, data.get(i).getContent()));
        //}

        waitAndCheck(socket, Protocol.SEND);
        objectOutputStream.writeObject(DataManager.gsonify(dataList));
        objectOutputStream.flush();

        Log.d(WiDi.TAG, DataManager.gsonify(dataList) + " sent");

        waitAndCheck(socket, Protocol.ACK);

        mDataManager.removeData(data.get(0));
        //for (int i = 0; i < nb; i++) {
        //    mDataManager.removeData(data.get(i));
        //}
    }

    private void closeSocket(final Socket socket) {
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(final Socket socket, final String message) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        Log.i(WiDi.TAG, message + " sent");
    }

    private void waitAndCheck(final Socket socket, final String message) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        String received = (String) objectInputStream.readObject();

        Log.i(WiDi.TAG, received + " received");

        if (!message.equals(received)) {
            closeSocket(socket);
        }
    }

    private Socket connect() {
        InetSocketAddress inetSocketAddress =
                new InetSocketAddress(mGroupOwnerAddress, WiDi.DATA_EXCHANGE_PORT);

        try {
            Socket socket = new Socket();
            socket.connect(inetSocketAddress, SOCKET_TIMEOUT);
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
