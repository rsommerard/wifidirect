package ut.disseminate.protocol;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ut.disseminate.Container;
import ut.disseminate.common.Beacon;
import ut.disseminate.common.BitVector;
import ut.disseminate.common.Chunk;
import ut.disseminate.common.Item;
import ut.disseminate.common.Utility;

public class Protocol {

    public static volatile ConcurrentHashMap<UUID, Beacon> beacons
            = new ConcurrentHashMap<UUID, Beacon> ();
    
    public static volatile ConcurrentHashMap<String, Item> items
            = new ConcurrentHashMap<String, Item> ();
    
    //public static AtomicBoolean readyForSelect = new AtomicBoolean(true);

    public static Object selectMonitor = new Object();

    public static Container mContainer;
    
    public static Beacon myBeacon;
    public static UUID myId;
    
    public static Timer beaconTimer;
    //public static String myIp;
    public static long cur_exec = 0;
    
    private static DisseminationProtocolCallback mProtocolCallback;
    public static Thread packetProcessor;
    public static Thread packetBroadcaster;

    public static ArrayList<Chunk> chunksSeen = new ArrayList<Chunk>();

    public static void populateItem(String itemId, ArrayList<Chunk> inChunks) {
        myBeacon.bvMap.put(itemId, new BitVector(-1L));
        Item newItem = new Item(itemId, Utility.NUM_CHUNKS);
        for (int i=0; i<Utility.NUM_CHUNKS; ++i) {
            newItem.chunks.put(i, inChunks.get(i));
        }
        items.put(itemId, newItem);
    }

    public static void populateDummyItem(String itemId) {
        myBeacon.bvMap.put(itemId, new BitVector(-1L));
        Item dummyItem = new Item(itemId, Utility.NUM_CHUNKS);
        for (int i=0; i<Utility.NUM_CHUNKS; ++i) {
            dummyItem.chunks.put(i, new Chunk(itemId, i, 1024*4, null));
        }
        items.put(itemId, dummyItem);
        synchronized(selectMonitor) {
            selectMonitor.notify();
        }
    }
    
    // used by UI thread to initialize protocol
    public static void initialize(ArrayList<String> desiredItems, Container packetContainer, Activity parent) {
        
        // attach to UI
        mProtocolCallback = (DisseminationProtocolCallback) parent;
        
        // get the reference to the container
        mContainer = packetContainer;
        
        // create a user id for the device
        myId = UUID.randomUUID();
        
        // intialize the beacon for the device
        myBeacon = new Beacon(myId, new HashMap<String, BitVector>(), null, 0);
        for (String itemId : desiredItems) {
            requestItem(itemId);
        }
        //
        
        packetProcessor = new Thread(new PacketProcessor());
        packetBroadcaster = new Thread(new PacketBroadcaster());
        packetProcessor.start();
        packetBroadcaster.start();
        Timer beaconTimer = new Timer();
        beaconTimer.schedule(new BeaconBroadcaster(), 0, Utility.BEACON_INTERVAL);
        
    }
    
    // used by UI thread to request items
    public static void requestItem(String itemId) {
        // Add item to beacon
        if (myBeacon.bvMap.get(itemId) == null) {
            myBeacon.bvMap.put(itemId, new BitVector(0));
        }
        // Add item to local list of items
        if (items.get(itemId) == null) {
            items.put(itemId, new Item(itemId, Utility.NUM_CHUNKS));
        }
    }
    
    
    public static Chunk selectChunk() {
        return randomAlgorithm();
    }
    
    public static Chunk randomAlgorithm() {
        long last_exec = cur_exec;
        long start = System.currentTimeMillis();
        cur_exec = start;
        long timeBw = cur_exec - last_exec;
        //Log.d("randomAlgorthm", "Time since last selection: "+timeBw);
        //Log.d("randomAlgorithm", "Selecting chunk...");
        HashMap<Chunk, Double> uniquenessMap = new HashMap<Chunk, Double>();
        
        Random rand =  new Random();
        
        // traverse all of my items
        for (Item item : items.values()) {
            
            // get my bitvector for the current item
            BitVector myBv = myBeacon.bvMap.get(item.name);
            
            // traverse all beacons
            // TODO: check to make sure beacons correspond to current neighbors
            for (Beacon beacon : beacons.values()){
                
                //TODO: check timestamp of beacon in order to prune
                // get bitvector for current neighbor
                BitVector neighborBv = beacon.bvMap.get(item.name);
                // make sure that the neighbor is actually tracking that item
                if (neighborBv != null) {
                    
                    // generate bit vector that has only values of chunks that the neighbor might want
                    BitVector intersection = myBv.oppositeIntersection(neighborBv); // myBv ^ ~(neighborBv)
                    int cntFound = 0;
                    for (int i=0; i < Utility.NUM_CHUNKS; ++i) {
                        if (intersection.testBit(i)) {
                            cntFound++;
                            Chunk potentialChunk = item.chunks.get(i);
                            if (chunksSeen.contains(potentialChunk)) {

                            } else {
                                Double uniqueness = uniquenessMap.get(potentialChunk);
                                if (uniqueness != null) {
                                    uniquenessMap.put(potentialChunk, uniqueness+1.0);
                                } else {
                                    uniquenessMap.put(potentialChunk, 1.0);
                                }
                            }
                        }
                    }
                }
            }
        }
        //Log.d("randomAlgorithm", "Potential chunks found: "+uniquenessMap.size());
        for (Chunk potChunk: uniquenessMap.keySet()) {
            double uniqueness = uniquenessMap.get(potChunk);
            if (uniqueness > 1.0) {

            }
        }
        Object[] potentialChunks = uniquenessMap.keySet().toArray();
        //Log.d("Potential Chunks", Integer.toString(potentialChunks.length));
        Chunk selectedChunk;
        if (potentialChunks != null && potentialChunks.length != 0) {
            selectedChunk = (Chunk) potentialChunks[rand.nextInt(potentialChunks.length)];
            if (uniquenessMap == null) {
                //Log.d("asdf", "um is null");
            }

            if (selectedChunk == null) {
                //Log.d("asdf", "sc is null");
            }
            if (selectedChunk != null) {
                //Log.d("randomAlgorithm", "Potential chunks found: " + uniquenessMap.size() + " >>> ( "
                //        + selectedChunk.itemId + ", " + selectedChunk.chunkId + " )");
                selectedChunk.currentBeacon = myBeacon;
                chunksSeen.add(selectedChunk);
            }
        } else {
            selectedChunk = null;
            chunksSeen.clear();
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        //Log.d("randomAlgorithm", "Selection time: "+elapsed);
        return selectedChunk;

    }
    
    public static void processChunk(Chunk newChunk) {
        if(newChunk.currentBeacon!=null) {
            processBeaconReduced(newChunk.currentBeacon);
            //Log.d("Beacon Notif", "Current Beacon from chunk.");
        }
        if (newChunk != null) {
            Item i = items.get(newChunk.itemId);
            //Log.d("ProcessChunk", "Processing new chunk...");
            
            // check if we are interested in the item
            if (i != null) {
                //Log.d("ProcessChunk", "Looking at: ("+newChunk.itemId+","+newChunk.chunkId+")");
                
                // check to make sure we don't have the chunk
                if (i.chunks.get(newChunk.chunkId) == null) {
                    
                    // add the chunk to the data store for that item
                    i.chunks.put(newChunk.chunkId, newChunk);
                    //Log.d("ProcessChunk", "Datastore updated: ( "+newChunk.itemId+", "+newChunk.chunkId+" )");
                    //Log.d("ProcessChunk", "Completed so far: "+i.chunks.size());

                    // update my beacon to indicate that I have this chunk now
                    BitVector curBv = myBeacon.bvMap.get(newChunk.itemId);
                    curBv.setBit(newChunk.chunkId);
                    myBeacon.bvMap.put(newChunk.itemId, curBv);

                    // notify allowing selection to continue
                    synchronized(selectMonitor) {
                        selectMonitor.notify();
                    }
                    
                    // update the ui
                    if (curBv.isCompleted()) {
                        //Log.d("ProcessChunk", "Done with item!");
                        mProtocolCallback.itemComplete(i.name, new ArrayList<Chunk>(i.chunks.values()));
                    } else {
                        mProtocolCallback.chunkComplete(i.name, newChunk);
                    }
                } else {
                    //Log.d("ProcessChunk", "REDUNDANT CHUNK: ( "+newChunk.itemId+", "+newChunk.chunkId+" )");
                }
            }
        }
        //TODO: might need to send subscription here
    }

    public static void processBeaconReduced(Beacon newBeacon) {
        //Log.d("ProcessBeacon", "Updating beacon map...");
        if (newBeacon != null) {
            if (!newBeacon.userId.equals(myId)) {
                beacons.put(newBeacon.userId, newBeacon);
                //Log.d("ProcessBeacon", "Updated beacon for: " + newBeacon.userId);
                //synchronized(selectMonitor) {
                //    selectMonitor.notify();
                //}
            }
        }
    }

    public static void processBeacon(Beacon newBeacon) {
        //Log.d("ProcessBeacon", "Updating beacon map...");
        if (newBeacon != null) {
            if (!newBeacon.userId.equals(myId)) {
                beacons.put(newBeacon.userId, newBeacon);
                //Log.d("ProcessBeacon", "Updated beacon for: " + newBeacon.userId);
                synchronized(selectMonitor) {
                    selectMonitor.notify();
                }
            }
        }
    }
    
    public interface DisseminationProtocolCallback {
            public void itemComplete(String itemId, ArrayList<Chunk> contents);
            public void chunkComplete(String itemId, Chunk completedChunk);
        }    
}
