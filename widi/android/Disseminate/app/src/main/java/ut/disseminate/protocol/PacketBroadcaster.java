package ut.disseminate.protocol;

import android.util.Log;

import java.net.DatagramPacket;

import ut.disseminate.common.Chunk;
import ut.disseminate.common.Utility;

public class PacketBroadcaster implements Runnable {

	@Override
	public void run() {
		//System.out.println("Preparing subscription!");
		//Driver.subscribe();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        while (!Thread.interrupted()) {
            //if (Protocol.readyForSelect.get()) {
                Chunk chunkToSend = Protocol.selectChunk();

                if (chunkToSend != null) {

                    byte[] chunkBuf = Utility.serialize(chunkToSend, Utility.BUF_SIZE);
                    DatagramPacket chunkPacket = new DatagramPacket(chunkBuf, chunkBuf.length, Utility.broadcastAddr, Utility.RECEIVER_PORT);
                    //Log.d("Size of Chunk", Integer.toString(chunkBuf.length));
                    //Log.d("PacketBroadcaster", "Broadcasting: ( " + chunkToSend.itemId + ", "
                    //        + chunkToSend.chunkId + " ) [ "+chunkBuf.length+" ]");
                    //Log.d("PacketBroadcaster", "Length of chunkPacket: " + chunkBuf.length);
                    Protocol.mContainer.broadcast_packet(chunkPacket);
                    //MobileHost.chunkSocket.receive(recvPack);
                    //MobileHost.chunkQueue.put(recvPack);
                    //recvChunk = (Chunk) Utility.deserialize
                } else { // wait for a state change;
                    //Log.d("PacketBroadcaster", "Waiting for state change to broadcast again...");
                    synchronized(Protocol.selectMonitor) {
                        try {
                            Protocol.selectMonitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //Protocol.readyForSelect.set(false);
                }
            //}
        }
	}
	
}
