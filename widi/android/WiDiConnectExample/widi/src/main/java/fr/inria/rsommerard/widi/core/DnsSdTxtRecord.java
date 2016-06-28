package fr.inria.rsommerard.widi.core;

import java.util.HashMap;
import java.util.Map;

import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pDevice;

public class DnsSdTxtRecord {

    public String fullDomainName = "";
    public Map<String, String> txtRecordMap = new HashMap<>();
    public Device srcDevice = new Device();
}
