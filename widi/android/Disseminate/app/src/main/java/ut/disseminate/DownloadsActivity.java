package ut.disseminate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import ut.disseminate.common.Chunk;
import ut.disseminate.common.Utility;
import ut.disseminate.mock.CellularDataContainer;
import ut.disseminate.mock.Sim3G;
import ut.disseminate.protocol.Protocol;

public class DownloadsActivity extends Activity implements ImageGridFragment.OnImageGridListener,
        DownloadButtonsFragment.OnDownloadButtonsListener,
        Protocol.DisseminationProtocolCallback,
        ImageFragment.OnImageListener {

    private static final String TAG = DownloadsActivity.class.getSimpleName();

    public static String userId;
    public static String selectedItem = null;

    Container txrxfifo;

    WiFiDirectBroadcastReceiver mReceiver;

    public static int dataSpeed = 50000; //default is 10 bytes per second.

    HashMap<String, Integer> itemToContainer = new HashMap<String, Integer>();
    HashMap<String, ArrayList<String>> itemToSquares = new HashMap<String, ArrayList<String>>();
    HashMap<String, Boolean> itemToDownload = new HashMap<String, Boolean>();
    HashMap<String, String> itemToImageName = new HashMap<String, String>();
    HashMap<String, ArrayList<Chunk>> itemToChunks = new HashMap<String, ArrayList<Chunk>>();
    HashMap<String, Sim3G> itemTo3GDownloader = new HashMap<String, Sim3G>();
    HashMap<String, TimeKeeper> itemTimeKeepers = new HashMap<String, TimeKeeper>();
    HashMap<String, MetricWriter> itemMetricWriters = new HashMap<String, MetricWriter>();
    HashMap<String, Long> itemTime = new HashMap<String, Long>();

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        //SharedPreferences.Editor ed = getApplicationContext().getSharedPreferences("ViewStreamPrefs",0).edit();
        //ed.putString("userId",userId);
        //ed.commit();
        //Log.d(TAG, "UserId at Pause: "+userId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");
        //outState.putString("userId", userId);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onSaveInstanceState(savedState);
        //userId = savedState.getString("userId");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_my_downloads);

        WifiP2pManager mManager;   //the required infrastructure for the madapp app
        WifiP2pManager.Channel mChannel;

        IntentFilter mIntentFilter;
        WifiManager nManager;
        txrxfifo = new Container();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //userId = extras.getString(Constants.USER_ID_KEY);
        } else {
            //userId = getApplicationContext().getSharedPreferences("ViewStreamPrefs", 0).getString("userId",null);
        }

        //Initalize the items needed. Copy, paste this into every madapp application
        //required by WiFi direct conventions

        //mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mManager = new WifiP2pManager();
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);

        //Assign the handlers for the intents
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver, mIntentFilter);

        // Protocol specific initialization
        Utility.init();
        ArrayList<String> desiredItems = new ArrayList<String>();
        desiredItems.add("Item0");
        desiredItems.add("Item1");
        desiredItems.add("Item2");
        desiredItems.add("Item3");
        Protocol.initialize(desiredItems, txrxfifo, this);
        itemToImageName.put(desiredItems.get(0),"australia.jpg");
        itemToImageName.put(desiredItems.get(1),"wind_turbine.jpg");
        itemToImageName.put(desiredItems.get(2),"violin.jpg");
        itemToImageName.put(desiredItems.get(3),"abudhabi.jpg");

        for (String itemId: desiredItems) {
            Log.d(TAG, "Adding desired items to itemToChunks: "+itemId);
            Log.d(TAG, "Image file name: "+itemToImageName.get(itemId));
            itemToChunks.put(itemId, makeChunks(itemToImageName.get(itemId), itemId, 0));
        }

        //ImageGridFragment mIGFragment = ImageGridFragment.newInstance(null,null,null);
        Log.d(TAG, "Adding grid fragments");
        //getFragmentManager().beginTransaction().replace(R.id.stream_grid_container, mIGFragment).commit();
        ArrayList<String> imageUrls = new ArrayList<String> ();
        //completedChunks = new ArrayList<Integer>();
        for (int i=0; i<Utility.NUM_CHUNKS; ++i) {
            imageUrls.add("assets://100px_light_blue_square.jpg");
            //completedChunks.add(0);
        }
        //Log.d(TAG, "userId: " + userId);
        for (String item: desiredItems) {
            itemToSquares.put(item, new ArrayList<String>(imageUrls));
            itemToDownload.put(item, false);
        }
        if (findViewById(R.id.image0_grid_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            String curItem = desiredItems.get(0);
            if (curItem != null) {
                itemToContainer.put(curItem, R.id.image0_grid_container);
                ImageGridFragment frag0 = ImageGridFragment.newInstance(curItem, false, null, imageUrls, null);
                getFragmentManager().beginTransaction()
                        .replace(R.id.image0_grid_container, frag0)
                                //.addToBackStack(null)
                        .commit();
            }

        }
        if (findViewById(R.id.image1_grid_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            String curItem = desiredItems.get(1);
            if (curItem != null) {
                itemToContainer.put(curItem, R.id.image1_grid_container);
                ImageGridFragment frag0 = ImageGridFragment.newInstance(curItem, false, null, imageUrls, null);
                getFragmentManager().beginTransaction()
                        .replace(R.id.image1_grid_container, frag0)
                                //.addToBackStack(null)
                        .commit();
            }

        }
        if (findViewById(R.id.image2_grid_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            String curItem = desiredItems.get(2);
            if (curItem != null) {
                itemToContainer.put(curItem, R.id.image2_grid_container);
                ImageGridFragment frag0 = ImageGridFragment.newInstance(curItem, false, null, imageUrls, null);
                getFragmentManager().beginTransaction()
                        .replace(R.id.image2_grid_container, frag0)
                                //.addToBackStack(null)
                        .commit();
            }

        }
        if (findViewById(R.id.image3_grid_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            String curItem = desiredItems.get(3);
            if (curItem != null) {
                itemToContainer.put(curItem, R.id.image3_grid_container);
                ImageGridFragment frag0 = ImageGridFragment.newInstance(curItem, false, null, imageUrls, null);
                getFragmentManager().beginTransaction()
                        .replace(R.id.image3_grid_container, frag0)
                                //.addToBackStack(null)
                        .commit();
            }

        }

        if (findViewById(R.id.download_buttons_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            View buttonsContainer = findViewById(R.id.download_buttons_container);
            buttonsContainer.setVisibility(View.INVISIBLE);
            DownloadButtonsFragment dbFragment = DownloadButtonsFragment.newInstance(false);

            Log.d(TAG, "Adding buttons fragment");
            // would set arguments from intent here, but we don't have any
            getFragmentManager().beginTransaction().replace(R.id.download_buttons_container, dbFragment).commit();
        }
        //mReceiver.clearMetrics();

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "mManager.discoverPeers::onSuccess");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "mManager.discoverPeers::onFailure");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mad_broadcast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("3G Data Rates");
            alert.setMessage("Set 3G Data Rates (bytes per second):");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    value = value.replaceAll( "[^\\d]", "" ); //remove non numeric characters
                    dataSpeed = Integer.valueOf(value);
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
            return true;
        }
        String fileName="";

        if(id==R.id.action_startmetricwatch){
            boolean writeFailed=false;
            for(String itemId: itemMetricWriters.keySet()) {
                //itemMetricWriters.get(itemId).updateMetrics("Chunks sent", mReceiver.getPacketSent());
                //itemMetricWriters.get(itemId).updateMetrics("Chunks recv", mReceiver.getPacketsRx());
                itemMetricWriters.get(itemId).updateMetrics("Time started", itemTimeKeepers.get(itemId).getStartTime());
                itemMetricWriters.get(itemId).updateMetrics("Time ended", itemTimeKeepers.get(itemId).getEndTime());
                itemMetricWriters.get(itemId).updateMetrics("Time to Completion", (long) (itemTimeKeepers.get(itemId).checkTime() / itemTimeKeepers.get(itemId).NANOS_PER_SEC));
                //itemMetricWriters.get(itemId).updateMetrics("Bytes Sent", mReceiver.getBytesSent());
                //itemMetricWriters.get(itemId).updateMetrics("Bytes Recv", mReceiver.getBytesRx());
                if(!(!itemMetricWriters.get(itemId).name.equals(fileName) && !fileName.equals(""))) {

                    itemMetricWriters.get(itemId).flushToDisk(itemId);
                }
                else{
                    writeFailed=true;
                    break;
                }
                fileName = itemMetricWriters.get(itemId).name;
                //show alert
            }

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            if(writeFailed){
                alert.setTitle("Data not written.");
                alert.setMessage("Data not written since the experiments were not started at the same time.");
            }
            else {
                alert.setTitle("Data written.");
                alert.setMessage("Data has been written to "+fileName);
            }


            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {


                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //FragmentManager fm = getFragmentManager();
        //int backStackEntries = fm.getBackStackEntryCount();
        super.onBackPressed();
        /*if (backStackEntries > 0) {
            setTitle("Top Streams");
            getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            super.onBackPressed();
        }*/
    }

    @Override
    public void imageClickHandler(String itemId) {
        Log.d(TAG, "imageClickHandler");
        Animation slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
        View buttonsContainer = findViewById(R.id.download_buttons_container);
        buttonsContainer.setVisibility(View.VISIBLE);
        buttonsContainer.startAnimation(slideUpIn);
        selectedItem = itemId;
    }

    @Override
    public void itemComplete(String itemId, ArrayList<Chunk> contents) {
        Log.d("File", "onItemComplete");
       if(itemTimeKeepers.get(itemId)!=null){
           itemTimeKeepers.get(itemId).stop();
           long timeTaken = itemTimeKeepers.get(itemId).checkTime();
           itemTime.put(itemId, timeTaken);
          // itemMetricWriters.get(itemId).updateMetrics("DownloadSpeed", mReceiver.getBytesRx());
           Log.d("File", "Write to Disk");
       }
        //setTitle("Done!");
        byte[] imageContents = chunksToByteArray(contents);
        Log.d("Image", "Displaying image!");
        ImageFragment iFragment = ImageFragment.newInstance(imageContents);
        getFragmentManager().beginTransaction()
                .replace(itemToContainer.get(itemId), iFragment)
                .commit();
    }

    @Override
    public void chunkComplete(String itemId, Chunk completedChunk) {
        //update the item time if it's the first time created.
        if(itemTimeKeepers.get(itemId) == null){  //if there is no stopwatch object for this item, then create a new one
            itemTimeKeepers.put(itemId, new TimeKeeper());
            itemTimeKeepers.get(itemId).start();  //start this since this is the first time the item chunk has been retrieved.
            itemMetricWriters.put(itemId, new MetricWriter());
        }

        ArrayList<String> urls = itemToSquares.get(itemId);
        if (urls == null) {
            return;
        }
        urls.set(completedChunk.chunkId, "assets://100px_light_blue.png");
        //urls.set(completedChunk.chunkId, "assets://100px_blue_square.png");
        ImageGridFragment newIGFragment = ImageGridFragment.newInstance(itemId, itemToDownload.get(itemId), null, urls, null);
        getFragmentManager().beginTransaction()
                .replace(itemToContainer.get(itemId), newIGFragment)
                        //.addToBackStack(null)
                .commit();
    }

    @Override
    public void cancelButtonHandler() {
        //Log.d(TAG, "cancelButtonHandler");
        Animation slideDownIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        View buttonsContainer = findViewById(R.id.download_buttons_container);
        buttonsContainer.startAnimation(slideDownIn);
        buttonsContainer.setVisibility(View.INVISIBLE);
        selectedItem = null;
    }

    public byte[] chunksToByteArray(ArrayList<Chunk> inChunks) {
        int total_length = 0;
        for (Chunk chk: inChunks) {
            total_length += chk.data.length;
        }
        byte [] result = new byte[total_length];
        int marker = 0;
        for (Chunk chk: inChunks) {
            System.arraycopy(chk.data, 0, result, marker, chk.data.length);
            marker+=chk.data.length;
        }
        return result;
    }

    @Override
    public void downloadButtonHandler() {
        Log.d(TAG, "downloadButtonHandler");
        Animation slideDownIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        View buttonsContainer = findViewById(R.id.download_buttons_container);
        buttonsContainer.startAnimation(slideDownIn);
        buttonsContainer.setVisibility(View.INVISIBLE);
        if (selectedItem != null) {

            if (itemTo3GDownloader.get(selectedItem) == null) {
                itemTo3GDownloader.put(selectedItem,
                        new Sim3G(dataSpeed, 2, new CellularDataContainer(itemToChunks.get(selectedItem), 2, txrxfifo)));
                itemTo3GDownloader.get(selectedItem).startDownload();
            }

            itemToDownload.put(selectedItem, true);
            selectedItem = null;
        }

    }

    public ArrayList<Chunk> makeChunks(String fileName, String itemId, int chunkSize){
        ArrayList<Chunk> returnList = new ArrayList<Chunk>();

        AssetManager assetManager = getAssets();
        InputStream istr = null;
        byte[] data = new byte[32768];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            istr = assetManager.open(fileName);
            int nRead;
            while ((nRead = istr.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] arrayBox = buffer.toByteArray();

        Log.d(TAG, "Num bytes in arrayBox: "+arrayBox.length);

        chunkSize = (int) Math.ceil((double)arrayBox.length/(double)Utility.NUM_CHUNKS);

        // !! Assuming !! chunkSize is the max size in bytes for each chunk
        // Create a stack of all the bytes in order (first pop is first element in array)
        Stack<Byte> bytestack = new Stack<Byte>();
        for(int i=arrayBox.length-1; i>=0; i--){
            bytestack.push(Byte.valueOf(arrayBox[i]));
        }

        // Once the stack is initialized, start constructing the array list
        int chunkidcounter=0;
        while(!bytestack.isEmpty()){
            Chunk tempchunk = new Chunk(itemId, chunkidcounter, chunkSize, "");
            chunkidcounter++;
            byte[] newbytes;
            if(bytestack.size()>=chunkSize){
                newbytes= new byte[chunkSize];  //allocate a bytearray of max size
            }
            else{
                newbytes = new byte[bytestack.size()];  //allocate a bytearray of size rem.
            }
            for(int i=0; i<chunkSize && !bytestack.isEmpty(); i++){
                newbytes[i]=bytestack.pop();
            }
            tempchunk.setData(newbytes);
            returnList.add(tempchunk);
        }
        Log.d(TAG,"Number of chunks: "+returnList.size());
        return returnList;
    }

}
