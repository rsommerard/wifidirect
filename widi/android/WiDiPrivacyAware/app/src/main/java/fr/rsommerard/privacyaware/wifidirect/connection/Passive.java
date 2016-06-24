package fr.rsommerard.privacyaware.wifidirect.connection;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;

public class Passive extends Thread implements Runnable {

    private final Socket mSocket;
    private final DataManager mDataManager;
    private final Random mRandom;

    public Passive(final Socket socket, final DataManager dataManager) {
        mSocket = socket;
        mDataManager = dataManager;
        mRandom = new Random();
    }

    @Override
    public void run() {
        try {
            waitAndCheck(Protocol.HELLO);
            sendMessage(Protocol.HELLO);
            waitData();
            sendData();
            mSocket.close();
        } catch (IOException | ClassNotFoundException | SQLiteConstraintException e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    private void sendData() throws IOException, ClassNotFoundException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());

        List<Data> data = mDataManager.getAllData();
        Collections.shuffle(data);

        int nb = mRandom.nextInt(data.size());
        Log.i(WiFiDirect.TAG, "Nb data to send: " + nb);

        List<Data> dataList = new ArrayList<Data>();
        for (int i = 0; i < nb; i++) {
            dataList.add(new Data(null, data.get(i).getContent()));
        }

        waitAndCheck(Protocol.SEND);
        objectOutputStream.writeObject(DataManager.gsonify(dataList));
        objectOutputStream.flush();

        Log.d(WiFiDirect.TAG, DataManager.gsonify(dataList) + " sent");

        waitAndCheck(Protocol.ACK);

        for (int i = 0; i < nb; i++) {
            mDataManager.removeData(data.get(i));
        }
    }

    private void waitData() throws IOException, ClassNotFoundException, SQLiteConstraintException {
        sendMessage(Protocol.SEND);

        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        String gsonStr = (String) objectInputStream.readObject();

        List<Data> data = DataManager.deGsonify(gsonStr);

        Log.i(WiFiDirect.TAG, data + " received");

        for (Data d : data) {
            mDataManager.addData(d);
        }

        sendMessage(Protocol.ACK);
    }

    private void closeSocket() {
        try {
            mSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void waitAndCheck(final String message) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        String received = (String) objectInputStream.readObject();

        Log.d(WiFiDirect.TAG, received + " received");

        if (!message.equals(received)) {
            closeSocket();
        }
    }

    private void sendMessage(final String message) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        Log.d(WiFiDirect.TAG, message + " sent");
    }
}
