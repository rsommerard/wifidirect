package ut.disseminate.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ReceiveWrapper extends ObjectInputStream {

    InputStream is;

    public ReceiveWrapper(InputStream is) throws IOException {
        super(is);
        this.is = is;
    }

    public ReceiveWrapper(Socket s) throws IOException {
        this(s.getInputStream());
    }

    public Object receive() {
        try {
            return super.readObject();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            return null;
        }
    }

    public void close() throws IOException {
        super.close();
    }
}