package ut.disseminate.protocol;

import android.util.Log;

import java.net.DatagramPacket;

import ut.disseminate.common.Beacon;
import ut.disseminate.common.Chunk;
import ut.disseminate.common.Packet;
import ut.disseminate.common.Utility;

public class PacketProcessor implements Runnable {
	
	public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
		//byte [] recvBuf = new byte[Utility.BUF_SIZE];
        while (!Thread.interrupted()) {
            //Log.d("PacketProcessor", "Requesting next packet...");
            DatagramPacket receivedPacket = Protocol.mContainer.receive_packet();
            //DatagramPacket recvPack = new DatagramPacket(recvBuf, recvBuf.length);
            byte [] recvBuf = receivedPacket.getData();
            Integer len = receivedPacket.getLength();
            //Log.d("PacketProcessor", "Length of received packet: "+len.toString());
            Packet newData = (Packet) Utility.deserialize(recvBuf, receivedPacket.getLength());
            if (newData.identifier == Packet.Type.BEACON) {
                Protocol.processBeacon((Beacon) newData);
                //Protocol.readyForSelect.set(true);
            } else if (newData.identifier == Packet.Type.CHUNK) {
                Protocol.processChunk((Chunk) newData);
                //Protocol.readyForSelect.set(true);
            } else {
                //Log.d("PacketProcessor", "Packet does not match BEACON or CHUNK types!");
            }
            //MobileHost.chunkSocket.receive(recvPack);
            //MobileHost.chunkQueue.put(recvPack);
            //recvChunk = (Chunk) Utility.deserialize
        }
	}
	
}
