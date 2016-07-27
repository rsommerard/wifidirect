package fr.inria.rsommerard.widitestingproject.wifidirect.device;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import fr.inria.rsommerard.widi.core.WiDi;
import fr.inria.rsommerard.widitestingproject.WiFiDirect;
import fr.inria.rsommerard.widitestingproject.dao.DaoMaster;
import fr.inria.rsommerard.widitestingproject.dao.DaoMaster.DevOpenHelper;
import fr.inria.rsommerard.widitestingproject.dao.DaoSession;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import fr.inria.rsommerard.widitestingproject.dao.Device;
import fr.inria.rsommerard.widitestingproject.dao.DeviceDao;

public class DeviceManager {

    private static final int AVAILABILITY = 60000;

    private final Random mRandom;
    private final DeviceDao mDeviceDao;

    public DeviceManager(final Context context) {
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        SQLiteDatabase mDb = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(mDb);
        DaoSession mDaoSession = mDaoMaster.newSession();
        mDeviceDao = mDaoSession.getDeviceDao();

        mDeviceDao.deleteAll(); // TODO: Delete this line

        mRandom = new Random();
        mRandom.setSeed(42L);
    }

    public Device getDevice() {
        List<Device> devices = mDeviceDao.loadAll();
        return devices.get(mRandom.nextInt(devices.size()));
    }

    public Device getDevice(final Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        return query.unique();
    }

    public List<Device> getAllDevices() {
        return mDeviceDao.loadAll();
    }

    public boolean hasDevices() {
        long limit = System.currentTimeMillis() - AVAILABILITY;
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Timestamp.gt(limit));

        Query<Device> query = qBuilder.build();

        return query.list().size() != 0;
    }

    public boolean containsDevice(final Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        return query.unique() != null;
    }

    public void updateDevice(final Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        Device d = query.unique();
        device.setId(d.getId());

        mDeviceDao.update(device);
        Log.i(WiDi.TAG, "Update " + d.toString() + " to " + device.toString());
    }

    public void addDevice(final Device device) {
        mDeviceDao.insert(device);
        Log.i(WiDi.TAG, "Insert " + device.toString());
    }

    public void deleteAll() {
        mDeviceDao.deleteAll();
    }
}
