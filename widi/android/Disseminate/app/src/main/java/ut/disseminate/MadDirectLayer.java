package ut.disseminate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDeviceList;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
import ut.disseminate.common.Utility;

public class MadDirectLayer extends BroadcastReceiver {

    private static WifiP2pManager mManager;
    private static WifiP2pManager.Channel mChannel;
    private static Activity mActivity;
    private static WifiManager CommonManager;   //Common non-p2p version of the wireless manager
    WifiP2pManager.PeerListListener myPeerListListener;

    private static ArrayList<WifiP2pDevice> peersList = new ArrayList<WifiP2pDevice>();
    private static WifiP2pDevice groupOwner;

    private static Container mCont;

    private static PowerManager powerManager;

    private static PowerManager.WakeLock powerGrabber;

    //METRICS
    private static AtomicLong packetSentCount = new AtomicLong(0);
    private static AtomicLong byteSentCount = new AtomicLong(0);
    private static AtomicBoolean sentAccessLock = new AtomicBoolean(false);

    private static AtomicLong packetRxCount = new AtomicLong(0);
    private static AtomicLong byteRxCount = new AtomicLong(0);
    private static AtomicBoolean rxAccessLock = new AtomicBoolean(false);


    //private static int socketPort = 15270;
    public MadDirectLayer(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Activity activity, Container items) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        this.CommonManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        mCont = items;

        Broadcast_Init();
        Receiver_Init(); //initalize and start the recv thread



    }

    public static void clearMetrics(){
        packetSentCount.set(0);
        packetRxCount.set(0);
        byteRxCount.set(0);
        byteSentCount.set(0);
    }
    public static Context getAppContext(){
        return mActivity;
    }
    public static WifiP2pDevice getGroupOwner(){
        return groupOwner;
    }
    public static ArrayList<WifiP2pDevice> getPeers(){
        return peersList;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //Start the next intent.
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        //Something went wrong.
                        Toast.makeText(mActivity, "Discovery failed for some reason.", Toast.LENGTH_SHORT);
                    }
                });
            } else {
                Toast.makeText(mActivity, "Error: WiFi Direct is not enabled.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (mManager != null) {
                    mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            peersList.clear();  //Remove the previous list of peers and repopulate it
                            peersList.addAll(peers.getDeviceList());
                            Log.d("MadApp", String.format("PeerListListener: %d peers available, updating device list", peers.getDeviceList().size()));

                            for(WifiP2pDevice k: peersList){
                                if(k.isGroupOwner()){
                                    groupOwner = k;
                                    Log.d("MadApp", "Group Owner Identified: "+k.deviceName);
                                }
                            }

                            if (peersList.size() == 0) {
                                Log.d("MadApp", "No devices found");
                                return;
                            }


                        }
                    });
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }

    //Counter operations
    public static long getPacketSent(){
        //while(sentAccessLock.get()==true){}  //spin while resource is locked.
        return packetSentCount.get();
    }
    public static long getBytesSent(){
        //while(sentAccessLock.get()==true){}  //spin while resource is locked.
        return byteSentCount.get();
    }
    public static long getPacketsRx(){
        //while(rxAccessLock.get()==true){}  //spin while resource is locked.
        return packetRxCount.get();
    }
    public static long getBytesRx(){
       // while(rxAccessLock.get()==true){}  //spin while resource is locked.
        return byteRxCount.get();
    }

    public static void resetAllMetrics(){
        while(rxAccessLock.get()==true || sentAccessLock.get()==true){}  //spin while resources are locked.
        rxAccessLock.set(true);
        sentAccessLock.set(true);  //lock the resources, not needed since only accessed by main thread, but implemented for
                                    // sake of symmetry
        packetSentCount.set(0);
        byteSentCount.set(0);
        packetRxCount.set(0);
        byteRxCount.set(0);
        rxAccessLock.set(false);
        sentAccessLock.set(false); //release the resource
    }

    public static void resetSentMetrics(){
        while(sentAccessLock.get()==true){}  //spin while resources are locked.

        sentAccessLock.set(true);  //lock the resources, not needed since only accessed by main thread, but implemented for
                                        // sake of symmetry
        packetSentCount.set(0);
        byteSentCount.set(0);
        sentAccessLock.set(false); //release the resource
    }
    public static void resetRxMetrics(){
        while(rxAccessLock.get()==true){}  //spin while resources are locked.
        rxAccessLock.set(true);  //lock the resources, not needed since only accessed by main thread, but implemented for sake of symmetry

        packetRxCount.set(0);
        byteRxCount.set(0);
        rxAccessLock.set(false);
    }


    //END OF COUNTER OPERATIONS

    public static void Stay_Awake(){
        powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
        powerGrabber = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");

        powerGrabber.acquire();

    }

    public static void Go_Sleep(){
        powerGrabber.release();
    }
    public static void Broadcast_Init(){   //Initalization function for the broadcaster, call this before broadcasting anything
        Log.d("Process", "Broadcast service initiated.");
        Thread broadProcess = new Thread(new BroadcastThread());
        broadProcess.start();
    }

    public static void Receiver_Init(){   //Initialize this if you want the thread to give you items
        Thread processRecv = new Thread(new RecvProcess());
        processRecv.start();
        Log.d("Message", "Recv Process Started.");
    }


    private static class RecvProcess implements Runnable {   //constant retrieval process
        //String data;
        private byte[] recvItem;
        DatagramSocket serverSocket;
        @Override
        public void run (){
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try {
                WifiManager.MulticastLock lock = CommonManager.createMulticastLock("dk.aboaya.pingpong");
                lock.acquire();


                serverSocket = new DatagramSocket(Utility.RECEIVER_PORT);
                while(true) {
                    byte[] data = new byte[serverSocket.getReceiveBufferSize()];
                    DatagramPacket packet = new DatagramPacket(data, serverSocket.getReceiveBufferSize());
                    serverSocket.receive(packet);
                    recvItem = packet.getData();
                    if (recvItem != null) {
                        mCont.update_rx(packet);
                        recvItem = packet.getData();
                        //Log.d("Size Of Packet", Integer.toString(packet.getLength()));

                        if(packet.getLength() > 900) {

                            //Log.d("Size Of Inside Chunk", Integer.toString(packet.getLength()));
                            packetRxCount.incrementAndGet();
                            byteRxCount.getAndAdd(packet.getLength());
                        }

                        //Log.d("Receive", (new String(recvItem)).toString());
                        //Log.d("Rx Buffer Size Updated", "New Size: " + mCont.packet_count());
                    }

                }
            }
            catch(Exception e){
                //serverSocket.close();
                e.printStackTrace();
                Log.d("Error.", "Exception with the receive thread.");
            }

            //return null;
        }
    }



    //Broadcaster stuff
    private static class BroadcastThread implements Runnable {
        byte[] output;
        DatagramSocket outSocket;
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                outSocket = new DatagramSocket(Utility.BROADCASTER_PORT);
                outSocket.setReuseAddress(true);
                outSocket.setBroadcast(true);
                Log.d("BroadcastThread", "Socket Initialized.");
               // Log.d("Async", "Item Retrieved.");
                while(true) {   //constantly runs
                    //output = mCont.next_txitem();
                    //DatagramPacket packet = new DatagramPacket(output, output.length, getBroadcastAddress(), 15270);
                    DatagramPacket packet = mCont.next_txitem();
                    output = packet.getData();
                    outSocket.send(packet);
                    if(packet.getLength() > 900) {
                        packetSentCount.incrementAndGet();
                        byteSentCount.getAndAdd(packet.getLength());
                    }


                    //Log.d("BroadcastThread", "Packet sent");
                }

            } catch (Exception e) {
                e.printStackTrace();
                //outSocket.close();
                Log.d("Error", "Error initializing the push.");
            }
        }

    }
    public static int getPacketPort(){
        return Utility.RECEIVER_PORT;
    }
    public static InetAddress getBroadcastIP(){
        /*try {
            return getBroadcastAddress();
        } catch (IOException e) {
            e.printStackTrace();

        }*/
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName("10.0.2.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr;
    }
    //UTILS USED BY THE MADAPP LAYER
    /*private static InetAddress getBroadcastAddress() throws IOException {
        Log.d("MadAppASync","Getting broadcast address");
        WifiManager wifi = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        //String localIp = getDottedDecimalIP(getLocalIPAddress());
        //Log.d("LocalIp", localIp);

        DhcpInfo dhcp = wifi.getDhcpInfo();

        if (dhcp == null) {
            Log.d("MadAppASync", "DHCP is null!");
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        //Log.d("MadAppASync",InetAddress.getByAddress(quads).toString());
        InetAddress addr = InetAddress.getByName("192.168.49.255");
        return addr;
    }*/
    private static String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    private static byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }
}
