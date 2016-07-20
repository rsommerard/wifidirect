package ut.disseminate;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;


/**
 * Created by Venkat on 12/2/14.
 */
public class MetricWriter {
    String name;
    HashMap<String, Long> itemMetrics = new HashMap<String, Long>();

    public MetricWriter(){
        //Use this to find the name of the file automatically
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);  //hour of the day
        int minute = Calendar.getInstance().get(Calendar.MINUTE);  //minute of the hour
        name = hour+minute+".txt";
    }
    public MetricWriter(String itemName){
        this.name = itemName;
    }
    public void changeName(String itemName){
        this.name = itemName;
    }
    public void updateMetrics(String name, Long value){
        if(itemMetrics.get(name)==null){
           itemMetrics.put(name, value);  //creates a new value
           return;
        }
        itemMetrics.remove(name);  //remove the old value
        itemMetrics.put(name, value);
    }
    public void flushToDisk(String experimentName){   //deletes all temporary data and puts it into the data file
        //File folderToWrite = new File(Environment.getExternalStorageDirectory() + "/"
        //        + Environment.DIRECTORY_PICTURES + "/ExperimentData");
        File folderToWrite = new File(Environment.getExternalStorageDirectory() +"/"+Environment.DIRECTORY_PICTURES+ "/ExperimentData");
        if(!folderToWrite.exists()){
            boolean successDescription = folderToWrite.mkdirs();
            if (successDescription) {

            } else {
                throw new RuntimeException("File Error in writing new folder");  //Privileges not allowed
            }
        }
//        File fileToWrite = new File(
//                Environment.getExternalStorageDirectory() + "/"
//                        + Environment.DIRECTORY_DOCUMENTS + "/ExperimentData",
//                name);
                File fileToWrite = new File(Environment.getExternalStorageDirectory() +"/"+Environment.DIRECTORY_PICTURES+ "/ExperimentData",
                name);

        String dataToDisk = experimentName;
        dataToDisk+=" :: ";
        for(String activeKey: itemMetrics.keySet()){
            dataToDisk += activeKey + " : " + itemMetrics.get(activeKey);
            dataToDisk+="; ";
        }
        dataToDisk+="\n";

        //write to file
        if(!fileToWrite.exists()){
            try {
                FileWriter writeMech = new FileWriter(fileToWrite, false);  //create a new file
                writeMech.write(dataToDisk);
                writeMech.flush();
                writeMech.close();
            }
            catch(Exception e){
                throw new RuntimeException("Error writing to disc.");
            }
        }
        else{  //file already exists, just open it and append it to the file
            try {
                FileWriter writeMech = new FileWriter(fileToWrite, true);
                writeMech.write(dataToDisk);
                writeMech.flush();
                writeMech.close();
            }
            catch(Exception e){
                throw new RuntimeException("Disc write error.");
            }
        }

    }
}
