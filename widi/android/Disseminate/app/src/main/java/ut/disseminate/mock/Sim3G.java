package ut.disseminate.mock;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Venkat on 11/26/14.
 * Class that simulates a 3G downloader
 * Completed and tested version
 * Needs the CellularDataContainer companion class
 */


public class Sim3G {

    private int speedBps = 0;
    private int downloadType = 0;
    private Timer taskSch;
    private boolean completed = false;
    private CellularDataContainer datacontain;
    private int TxPointer = 0; //pointer for the serverData
    private int RxPointer = 0; //pointer for the downloadedData

    private int bytecounter = 0; //variable used by timer thread to see byte speed


    public Sim3G(int bytesPerSecond, int type, CellularDataContainer cellData){
        //DownloadSimulator myTask = new DownloadSimulator();
        taskSch = new Timer();
        speedBps = bytesPerSecond;
        datacontain = cellData;
        downloadType = (type)%3; //accepts only 0,1,2 for ftb, btf, and random data transfer
        completed = false;
        //rxFIFOHandle=rxHandle;
    }

    public void startDownload() {
        //taskSch.schedule(new DownloadSimulator(), 10); //MAX SPEED OF 100 kBps
        int bytesOfChunk = datacontain.bytesInNextChunk();
        float interruptTime = bytesOfChunk/speedBps;   //how many seconds for the next chunk
        int intTime = (int)(interruptTime*1000);
        taskSch.schedule(new DownloadSimulator(), intTime);
    }
    public boolean downloadStatus() {   //returns boolean if the download has finished or not.
        return completed;
    }
    class DownloadSimulator extends TimerTask {
        public void run() {

            if(downloadType==0) {  //forward to back download
                  boolean complete = datacontain.forwardDownload();
                  if(complete==false) {
                        completed = true;
                        taskSch.cancel();  //task is completed
                  }
            }
            if(downloadType==1) {
                boolean complete = datacontain.backwardDownload();
                if (complete == false) {
                    completed = true;
                    taskSch.cancel();
                }
            }
            if(downloadType == 2) {
                boolean complete = datacontain.nextRandDownload();
                if(complete == false) {
                    completed = true;
                    taskSch.cancel();
                }
            }
            if(completed){
                return;
            }
            //set the next timer interval.
            int bytesOfChunk = datacontain.bytesInNextChunk();
            float interruptTime = ((float)bytesOfChunk)/((float)speedBps);   //how many seconds for the next chunk
            int intTime = (int)(interruptTime*1000);
            taskSch.schedule(new DownloadSimulator(), intTime);

        }
    }


}
