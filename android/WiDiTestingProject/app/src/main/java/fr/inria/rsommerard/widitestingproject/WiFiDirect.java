package fr.inria.rsommerard.widitestingproject;

// WiDi
//import android.net.wifi.p2p.WifiP2pManager;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widi.net.wifi.p2p.WifiP2pManager;
//

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


import fr.inria.rsommerard.widitestingproject.dao.Data;
import fr.inria.rsommerard.widitestingproject.dao.Device;
import fr.inria.rsommerard.widitestingproject.data.DataManager;

public abstract class WiFiDirect {

    public static String getRandomIdentifier() {
        Random rand = new Random();

        return Integer.toString(rand.nextInt(100000));
    }

    public static void populateDataTable(final DataManager dataManager, final int nbData) {
        for (int i = 0; i < nbData; i++) {
            Data d = new Data(null, getRandomContent());
            dataManager.addData(d);
        }
    }

    public static String getRandomContent() {
        Random rand = new Random();

        List<String> words = new ArrayList<>();
        words.add("Épitaphe");
        words.add("Bistouquette");
        words.add("Rhododendron");
        words.add("Gourgandine");
        words.add("Chouette");
        words.add("Esperluette");
        words.add("Corniche");
        words.add("Irrémédiable");
        words.add("Gargantuesque");
        words.add("Opercule");
        words.add("Pissenlit");
        words.add("Pommelé");
        words.add("Cataracte");
        words.add("Libellule");
        words.add("Inexorable");
        words.add("Frangipane");
        words.add("Fracas");
        words.add("Pamplemousse");
        words.add("Époustouflant");
        words.add("Ornithorynque");
        words.add("Papouille");
        words.add("Rascasse");
        words.add("Concupiscence");
        words.add("Parapluie");
        words.add("Margoulette");
        words.add("Clapotis");
        words.add("Nuage");
        words.add("Éphémère");

        return words.get(rand.nextInt(words.size()));
    }

    public static void cleanAllGroupsRegistered(WifiP2pManager manager,
                                                WifiP2pManager.Channel channel) {
        try {
            Method deletePersistentGroupMethod =
                    WifiP2pManager.class.getMethod("deletePersistentGroup",
                            WifiP2pManager.Channel.class,
                            int.class,
                            WifiP2pManager.ActionListener.class);

            for (int netid = 0; netid < 32; netid++) {
                deletePersistentGroupMethod.invoke(manager, channel, netid, null);
            }

            Log.i(WiDi.TAG, "Groups are successfully removed");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(WiDi.TAG, "deletePersistentGroup method NOT found");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(WiDi.TAG, "deletePersistentGroup failed");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(WiDi.TAG, "deletePersistentGroup failed");
        }
    }

    public static String getActionListenerFailureName(int reason) {
        switch (reason) {
            case 0:
                return "The operation failed due to an internal error. (0, ERROR)";
            case 1:
                return "The operation failed because p2p is unsupported on the device. (1, P2P_UNSUPPORTED)";
            case 2:
                return "The operation failed because the framework is busy and unable to service the request. (2, BUSY)";
            case 3:
                return "The discoverServices failed because no service requests are added. Use addServiceRequest to add a service request. (3, NO_SERVICE_REQUESTS)";
            default:
                return "(" + reason + ", UNKNOWN)";
        }
    }

    public static String devicesListToString(List<Device> devices) {
        int size = devices.size();
        Log.i(WiDi.TAG, "Nb devices: " + size);

        StringBuilder str = new StringBuilder("[");

        if (!devices.isEmpty()) {
            str.append(devices.get(0).getName());
            str.append(" (").append(new Date(Long.parseLong(devices.get(0).getTimestamp()))).append(")");

            for (int i = 1; i < size; i++) {
                str.append(", ");
                str.append(devices.get(i).getName());
                str.append(" (").append(new Date(Long.parseLong(devices.get(i).getTimestamp()))).append(")");
            }
        }

        str.append("]");

        Log.i(WiDi.TAG, str.toString());
        return str.toString();
    }

    public static String dataListToString(List<Data> data) {
        int size = data.size();
        Log.i(WiDi.TAG, "Nb data: " + size);

        StringBuilder str = new StringBuilder("[");

        if (!data.isEmpty()) {
            str.append(data.get(0).getContent());

            for (int i = 1; i < size; i++) {
                str.append(", ");
                str.append(data.get(i).getContent());
            }
        }

        str.append("]");

        return str.toString();
    }
}
