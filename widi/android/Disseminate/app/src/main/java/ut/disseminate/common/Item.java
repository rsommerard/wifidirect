package ut.disseminate.common;

import java.io.Serializable;
import java.util.TreeMap;

public class Item implements Serializable {
	public String name;
	//public int size;
	public int chunkSize;
    public int numChunks;
	public BitVector bv;
    public TreeMap<Integer, Chunk> chunks;

	public long startTime = 0;
	public long completedTime = 0;
	
	public long receivedBroadcast = 0;
	public long discardedBroadcast = 0;
	
	public long receivedVirtual = 0;
	public long discardedVirtual = 0;
	
	public Item (String name, int numChunks) {
		this.name = name;
        this.chunks = new TreeMap<Integer, Chunk>();
		//this.chunkSize = chunkSize;
        this.numChunks = numChunks;
		//this.size = size;
		//this.bv = bv;
	}

	public static String getMessageType(String pack) {
		String [] tokens = pack.split(",");
		return tokens[0];
	}
	
	public static String getItemId(String pack) {
		String [] tokens = pack.split(",");
		return tokens[1];
	}
	
	public static int getChunkId(String pack) {
		String [] tokens = pack.split(",");
		return Integer.parseInt(tokens[2]);
	}
}
