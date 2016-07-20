package ut.disseminate.mock;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ut.disseminate.Container;
import ut.disseminate.common.Chunk;
import ut.disseminate.common.Utility;

/**
 * Created by Venkat on 11/29/14.
 */
public class CellularDataContainer {
    private ArrayList<Chunk> serverData = new ArrayList<Chunk>();
    private ArrayList<Chunk> downloadedData = new ArrayList<Chunk>();

    private int RxIndex; //Index for the downloaded Data (client simulator)
    private int TxIndex; //Index for the transmitted Data (server simulator)

    private int itemsDownloaded = 0; //used only by the random download process

    HashMap<Integer, Integer> filledValues;
    Random indexFinder;
    Container RXFIFO;

    public CellularDataContainer(ArrayList<Chunk> serverContent, int dir, Container handleVar){
        RXFIFO = handleVar;
        serverData = serverContent;
        indexFinder = new Random();
        if(dir == 0){
            RxIndex = TxIndex = 0;
        }
        else if(dir == 1){
            RxIndex = 0;
            TxIndex = serverData.size()-1;
        }
        else{
            filledValues = new HashMap<Integer, Integer>(serverData.size());
            for(int i=0; i<serverData.size(); i++){
                downloadedData.add(null);
            }

            RxIndex = TxIndex = Math.abs(indexFinder.nextInt()%serverData.size());  //find the next index to "download"
            itemsDownloaded = 0;
        }


    }
    public int bytesInNextChunk(){
        return serverData.get(TxIndex).size;

    }
    public boolean forwardDownload(){

        // RXFIFO.add(serverData.get(TxIndex)); //Idea behind next few lines...
        byte[] chunkBuf = Utility.serialize(serverData.get(TxIndex), Utility.BUF_SIZE);
        DatagramPacket chunkPacket = new DatagramPacket(chunkBuf, chunkBuf.length, Utility.broadcastAddr, Utility.RECEIVER_PORT);
        RXFIFO.update_rx(chunkPacket);
        //End of logic for updating rx fifo
        downloadedData.add(serverData.get(TxIndex));
        TxIndex++;
        RxIndex++;
        if(TxIndex == serverData.size()){
            return false; //transfer failed since the server has no more new data
        }
        return true; //transfer is successful.
    }
    public boolean backwardDownload(){

        //Logic for updating RXFIFO
        byte[] chunkBuf = Utility.serialize(serverData.get(TxIndex), Utility.BUF_SIZE);
        DatagramPacket chunkPacket = new DatagramPacket(chunkBuf, chunkBuf.length, Utility.broadcastAddr, Utility.RECEIVER_PORT);
        RXFIFO.update_rx(chunkPacket);
        //End of logic
        downloadedData.add(serverData.get(TxIndex));
        TxIndex--;
        RxIndex++;
        if(TxIndex == -1){
            return false; //out of data to download
        }
        return true; //successful download
    }

    public boolean nextRandDownload(){
        if(itemsDownloaded == serverData.size()){
            return false; //transfer failed since the server has no more new data
        }
        //Logic for updating RXFIFO
        byte[] chunkBuf = Utility.serialize(serverData.get(TxIndex), Utility.BUF_SIZE);
        DatagramPacket chunkPacket = new DatagramPacket(chunkBuf, chunkBuf.length, Utility.broadcastAddr, Utility.RECEIVER_PORT);
        RXFIFO.update_rx(chunkPacket);
        //End of logic
        downloadedData.set(RxIndex, serverData.get(TxIndex));
        itemsDownloaded++;
        filledValues.put(TxIndex, TxIndex);
        while(filledValues.containsKey(TxIndex)){
            TxIndex=RxIndex=Math.abs(indexFinder.nextInt()%serverData.size());
        }
        return true;

    }

    public void reInitSystem(){
        RxIndex=0;
        TxIndex=0;
        downloadedData.clear();
    }

    public void changeServerData(ArrayList<Chunk> newServer){
        serverData = newServer;
        reInitSystem();
    }


}
