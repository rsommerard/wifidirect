package ut.disseminate.common;

import java.util.HashMap;
import java.util.UUID;

public class Beacon extends Packet {

	public UUID userId;
	public int signalStrength = 0;
	public HashMap<String, BitVector> bvMap; // map of itemId to bit vector
    public Subscription optionalSubscription = null;
    long timestamp;
	
	public Beacon(UUID userId, HashMap<String, BitVector> bvMap, Subscription optional, long timestamp) {
		this.userId = userId;
		this.bvMap = bvMap;
        this.optionalSubscription = optional;
        this.timestamp = timestamp;
        this.identifier = Type.BEACON;
	}
}
