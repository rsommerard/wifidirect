package fr.rsommerard.privacyaware;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import fr.rsommerard.privacyaware.wifidirect.WiFiDirectManager;
import fr.rsommerard.privacyaware.wifidirect.exception.WiFiException;

public class MainActivity extends AppCompatActivity {

    private WiFiDirectManager mWiFiDirectManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = (Button) findViewById(R.id.button_start);
        assert startButton != null;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Start",
                        Toast.LENGTH_SHORT).show();

                Log.i(WiFiDirect.TAG, "Start");

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

                Log.i(WiFiDirect.TAG, "Stop");

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

                Log.i(WiFiDirect.TAG, "Process");

                if (mWiFiDirectManager != null)
                    mWiFiDirectManager.process();
            }
        });

        Button dataButton = (Button) findViewById(R.id.button_data);
        assert dataButton != null;
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Data",
                        Toast.LENGTH_SHORT).show();

                Log.i(WiFiDirect.TAG, "Data");

                if (mWiFiDirectManager != null)
                    mWiFiDirectManager.printData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(WiFiDirect.TAG, "onDestroy()");
        if (mWiFiDirectManager != null)
            mWiFiDirectManager.stop(MainActivity.this);
        super.onDestroy();
    }


}
