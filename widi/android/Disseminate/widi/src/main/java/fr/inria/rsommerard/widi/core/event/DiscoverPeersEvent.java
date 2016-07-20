package fr.inria.rsommerard.widi.core.event;

import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;

public class DiscoverPeersEvent {

    public final WifiP2pManager.ActionListener actionListener;

    public DiscoverPeersEvent(final WifiP2pManager.ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
