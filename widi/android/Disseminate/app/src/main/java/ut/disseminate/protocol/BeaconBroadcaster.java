package ut.disseminate.protocol;

import android.util.Log;

import java.net.DatagramPacket;
import java.util.TimerTask;

import ut.disseminate.common.Utility;

public class BeaconBroadcaster extends TimerTask {
	
	@Override
	public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
		// Send beacon
		byte [] beaconBuf = Utility.serialize(Protocol.myBeacon, Utility.BUF_SIZE);
        //Log.d("Size of Beacon", Integer.toString(beaconBuf.length));
		DatagramPacket updatedBeacon = new DatagramPacket(beaconBuf, beaconBuf.length, Utility.broadcastAddr, Utility.RECEIVER_PORT);
		//Log.d("BeaconBroadcaster", "Periodic beacon broadcast...");
        Protocol.mContainer.broadcast_packet(updatedBeacon);

		//System.out.println("Publication Send Rate: "+Driver.getRate(Driver.beaconBytesSent, System.currentTimeMillis()));
	}
	
}

