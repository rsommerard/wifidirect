package fr.inria.rsommerard.widitestingproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widitestingproject.wifidirect.WiFiDirectManager;
import fr.inria.rsommerard.widitestingproject.wifidirect.exception.WiFiException;

public class MainActivity extends AppCompatActivity {

    private WiFiDirectManager mWiFiDirectManager;
    private Random mRandom;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRandom = new Random();

        try {
            mWiFiDirectManager = new WiFiDirectManager(MainActivity.this);
        } catch (IOException | WiFiException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mWiFiDirectManager.process();
            }
        }, mRandom.nextInt(100000), 110000, TimeUnit.MILLISECONDS);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mWiFiDirectManager.printData();
            }
        }, mRandom.nextInt(100000), 110000, TimeUnit.MILLISECONDS);

        /*Button startButton = (Button) findViewById(R.id.button_start);
        assert startButton != null;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Start",
                        Toast.LENGTH_SHORT).show();

                Log.i(WiDi.TAG, "Start");

                try {
                    mWiFiDirectManager = new WiFiDirectManager(MainActivity.this);
                } catch (IOException | WiFiException e) {
                    e.printStackTrace();
                }
            }
        });

        Button stopButton = (Button) findViewById(R.id.button_stop);
        assert stopButton != null;
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Stop",
                        Toast.LENGTH_SHORT).show();

                Log.i(WiDi.TAG, "Stop");

                if (mWiFiDirectManager != null)
                    mWiFiDirectManager.stop(MainActivity.this);
            }
        });

        Button processButton = (Button) findViewById(R.id.button_process);
        assert processButton != null;
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Process",
                        Toast.LENGTH_SHORT).show();

                Log.i(WiDi.TAG, "Process");

                if (mWiFiDirectManager != null) {
                    mWiFiDirectManager.process();

                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            mWiFiDirectManager.process();
                        }
                    }, 0, 110000, TimeUnit.MILLISECONDS);
                }
            }
        });*/

        /*Button dataButton = (Button) findViewById(R.id.button_data);
        assert dataButton != null;
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Data",
                        Toast.LENGTH_SHORT).show();

                Log.i(WiDi.TAG, "Data");

                if (mWiFiDirectManager != null)
                    mWiFiDirectManager.printData();
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        Log.i(WiDi.TAG, "onDestroy()");
        if (mWiFiDirectManager != null)
            mWiFiDirectManager.stop(MainActivity.this);
        super.onDestroy();
    }


}
