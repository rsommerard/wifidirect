package ut.disseminate.common;

import java.io.Serializable;

public class Subscription implements Serializable {

	public String requester;
	public String itemId;
	public int chunkId;
	
	public Subscription(String requester, String itemId, int chunkId) {
		//super("sub");
		this.requester = requester;
		this.itemId = itemId;
		this.chunkId = chunkId;
	}
}
