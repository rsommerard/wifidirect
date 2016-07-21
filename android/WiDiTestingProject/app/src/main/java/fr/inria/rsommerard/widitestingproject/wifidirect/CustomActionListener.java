package fr.inria.rsommerard.widitestingproject.wifidirect;

// WiDi
//import android.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
//

import android.util.Log;

import fr.inria.rsommerard.widitestingproject.WiFiDirect;

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
            Log.i(WiDi.TAG, mOnSuccessMessage);
        }
    }

    @Override
    public void onFailure(final int reason) {
        Log.e(WiDi.TAG, mOnFailureMessage +
                WiFiDirect.getActionListenerFailureName(reason));
    }
}