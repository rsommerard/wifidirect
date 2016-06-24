package fr.rsommerard.privacyaware.wifidirect;

// WiDi
//import android.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
//

import android.util.Log;

import fr.rsommerard.privacyaware.WiFiDirect;

public class CustomActionListener implements WifiP2pManager.ActionListener {

    private String mOnSuccessMessage;
    private String mOnFailureMessage;

    public CustomActionListener(final String onSuccessMessage, final String onFailureMessage) {
        mOnSuccessMessage = onSuccessMessage;
        mOnFailureMessage = onFailureMessage;
    }

    @Override
    public void onSuccess() {
        // Nothing
        if (mOnSuccessMessage != null) {
            Log.i(WiFiDirect.TAG, mOnSuccessMessage);
        }
    }

    @Override
    public void onFailure(final int reason) {
        Log.e(WiFiDirect.TAG, mOnFailureMessage +
                WiFiDirect.getActionListenerFailureName(reason));
    }
}